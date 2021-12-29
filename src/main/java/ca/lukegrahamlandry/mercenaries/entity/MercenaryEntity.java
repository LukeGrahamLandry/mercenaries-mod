package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.SaveMercData;
import ca.lukegrahamlandry.mercenaries.client.MercTextureList;
import ca.lukegrahamlandry.mercenaries.client.gui.MerceneryContainer;
import ca.lukegrahamlandry.mercenaries.goals.MercFollowGoal;
import ca.lukegrahamlandry.mercenaries.goals.MercMeleeAttackGoal;
import ca.lukegrahamlandry.mercenaries.goals.MercRangeAttackGoal;
import ca.lukegrahamlandry.mercenaries.goals.MercShieldGoal;
import ca.lukegrahamlandry.mercenaries.init.EntityInit;
import ca.lukegrahamlandry.mercenaries.init.NetworkInit;
import ca.lukegrahamlandry.mercenaries.integration.FakePlayerThatRedirects;
import ca.lukegrahamlandry.mercenaries.network.OpenMercRehireScreenPacket;
import ca.lukegrahamlandry.mercenaries.network.OpenMercenaryInventoryPacket;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class MercenaryEntity extends CreatureEntity implements IRangedAttackMob {
    public static final DataParameter<Integer> TEXTURE_TYPE = EntityDataManager.defineId(MercenaryEntity.class, DataSerializers.INT);
    public static final DataParameter<Integer> FOOD = EntityDataManager.defineId(MercenaryEntity.class, DataSerializers.INT);
    public static final DataParameter<Integer> MONEY = EntityDataManager.defineId(MercenaryEntity.class, DataSerializers.INT);

    public static final DataParameter<Integer> ATTACK_STANCE = EntityDataManager.defineId(MercenaryEntity.class, DataSerializers.INT);
    public static final DataParameter<Integer> MOVE_STANCE = EntityDataManager.defineId(MercenaryEntity.class, DataSerializers.INT);

    public static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.defineId(MercenaryEntity.class, DataSerializers.OPTIONAL_UUID);
    public static final DataParameter<Optional<BlockPos>> CAMP = EntityDataManager.defineId(MercenaryEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    public BlockPos villageLocation = BlockPos.ZERO;
    public RegistryKey<World> campDimension = World.OVERWORLD;

    // > 0 when cant use, < 0 when using
    public int shieldCoolDown = 0;

    int foodTimer = 0;
    int moneyTimer = 0;


    // >0 when can't use
    // <0 while using
    int sharedArtifactCooldown = 0;
    CooldownTracker cooldowns = new CooldownTracker();

    private AttackType attackType = AttackType.NONE;
    public Inventory inventory;
    public MercenaryEntity(EntityType<MercenaryEntity> p_i48576_1_, World p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
        this.inventory = new Inventory(24);
        if (!this.level.isClientSide()) {
            this.entityData.set(TEXTURE_TYPE, MercTextureList.getRandom());
            this.entityData.set(MONEY, 20);
            this.entityData.set(FOOD, 20);
            this.entityData.set(ATTACK_STANCE, 0);
            this.entityData.set(MOVE_STANCE, 0);

            if (!this.hasCustomName() && !MercConfig.names.get().isEmpty()){
                String myName = MercConfig.names.get().get(this.getRandom().nextInt(MercConfig.names.get().size()));
                this.setCustomName(new TranslationTextComponent(myName));
            }
        }
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TEXTURE_TYPE, 0);
        this.entityData.define(MONEY, 20);
        this.entityData.define(FOOD, 20);
        this.entityData.define(ATTACK_STANCE, 0);
        this.entityData.define(MOVE_STANCE, 0);
        this.entityData.define(OWNER, Optional.empty());
        this.entityData.define(CAMP, Optional.empty());
    }

    public static AttributeModifierMap.MutableAttribute makeAttributes() {
        return MonsterEntity.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 32.0D).add(Attributes.MOVEMENT_SPEED, (double)0.33F).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.ARMOR, 0.0D).add(ForgeMod.REACH_DISTANCE.get(), 4D);
    }

    public double getReachDist(){
        return this.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new SwimGoal(this));

        this.goalSelector.addGoal(1, new MercShieldGoal(this));
        this.goalSelector.addGoal(1, new MercMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(1, new MercRangeAttackGoal(this, 1.0D, 20, 10));
        // melee attack goal should do this on its ownx
        // this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
         this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MobEntity.class, 5, false, false, this::canTarget));
         this.goalSelector.addGoal(2, new MercFollowGoal(this));
         this.goalSelector.addGoal(4, new RandomWalkingGoal(this, 0.8D, 100, false){
            @Override
            public boolean canContinueToUse() {
                return MercenaryEntity.this.isIdleMode() && super.canContinueToUse() && !MercenaryEntity.this.hasFindableTarget();
            }

            @Override
            public boolean canUse() {
                return MercenaryEntity.this.isIdleMode() && super.canUse() && !MercenaryEntity.this.hasFindableTarget();
            }
        });
         /*
         if (MercConfig.artifactsInstalled()){
             this.goalSelector.addGoal(1, new UseArtifactGoal(this));
         }
         */
    }

    private boolean canTarget(LivingEntity target) {
        if (!(target instanceof IMob)) return false;
        if (target instanceof CreeperEntity && this.getAttackType() == AttackType.MELEE) return false;

        if (this.isAttackStace()) {
            return !this.isStayMode() || this.distanceToSqr(target) < this.getReachDistSq();
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSwingTime();

        if (!this.level.isClientSide()){
            this.cooldowns.tick();
            this.sharedArtifactCooldown -= Math.signum(this.sharedArtifactCooldown);
            this.shieldCoolDown -= Math.signum(this.shieldCoolDown);

            this.moneyTimer++;
            if (this.moneyTimer > MercConfig.getMoneyDecayRate()){
                this.entityData.set(MONEY, this.getMoney() - 1);

                // consume from inventory
                int needed = 20 - this.getMoney();
                for (int i=0;i<(this.inventory.getContainerSize() - 4);i++){
                    ItemStack stack = this.inventory.getItem(i);
                    int value = MercConfig.getMoneyValue(stack.getItem());
                    if (value > 0 && value <= needed){
                        this.entityData.set(MONEY, this.getMoney() + value);
                        stack.shrink(1);
                        break;
                    }
                }

                this.moneyTimer -= MercConfig.getMoneyDecayRate(); // set to 0 but might have overshot

                if (this.getMoney() <= 8 && this.alertLevel < 1) {
                    if (this.getOwner() != null){
                        this.getOwner().displayClientMessage(new StringTextComponent("One of your mercenaries wants to be paid"), true);
                        this.alertLevel = 1;
                    }
                }

                if (this.getMoney() <= 4 && this.alertLevel < 2) {
                    if (this.getOwner() != null){
                        this.getOwner().displayClientMessage(new StringTextComponent("One of your mercenaries urgently needs to be paid!"), true);
                        this.alertLevel = 2;
                    }
                }

                if (this.getMoney() <= 0){
                    this.leaveOwner();
                }
            }

            this.foodTimer++;
            if (this.foodTimer > MercConfig.getFoodDecayRate()){
                this.entityData.set(FOOD, this.getFood() - 1);

                // consume from inventory
                int needed = 20 - this.getFood();
                for (int i=0;i<(this.inventory.getContainerSize() - 4);i++){
                    ItemStack stack = this.inventory.getItem(i);
                    int value = stack.getItem().isEdible() ? stack.getItem().getFoodProperties().getNutrition() : 0;
                    if (value > 0 && value <= needed){
                        this.entityData.set(FOOD, this.getFood() + value);
                        this.eat(this.level, stack); // calls stack.shrink and addEatEffects
                        break;
                    }
                }

                this.foodTimer -= MercConfig.getFoodDecayRate(); // set to 0 but might have overshot

                if (this.getFood() <= 8 && this.alertLevel < 1) {
                    if (this.getOwner() != null){
                        this.getOwner().displayClientMessage(new StringTextComponent("One of your mercenaries wants food"), true);
                        this.alertLevel = 1;
                    }
                }

                if (this.getFood() <= 4 && this.alertLevel < 2) {
                    if (this.getOwner() != null){
                        this.getOwner().displayClientMessage(new StringTextComponent("One of your mercenaries urgently needs food!"), true);
                        this.alertLevel = 2;
                    }
                }

                if (this.getFood() <= 0){
                    this.leaveOwner();
                }
            }

            if (this.getOwner() != null && MercConfig.createHorseToRide.get()){
                boolean isRiding = this.getVehicle() != null;
                boolean mountablesCompat = this.getOwner().getVehicle() != null && this.getOwner().getVehicle().getType().getRegistryName() != null && "mountables".equals(this.getOwner().getVehicle().getType().getRegistryName().getNamespace());
                boolean shouldRide = this.getOwner().getVehicle() instanceof AbstractHorseEntity || mountablesCompat;


                if (shouldRide && !isRiding && MercConfig.createHorseToRide.get()){
                    if (horseTimer < 0){
                        MercMountEntity horse = new MercMountEntity(EntityInit.MOUNT.get(), this.level);
                        horse.setPos(this.getX(), this.getY(), this.getZ());
                        this.startRiding(horse);
                        this.level.addFreshEntity(horse);
                        horseTimer = 15;
                    } else {
                        horseTimer--;
                    }


                } else if (!shouldRide && isRiding){
                    Entity horse = this.getVehicle();
                    if (horse instanceof MercMountEntity){
                        horseTimer = 0;
                        this.stopRiding();
                        horse.remove();
                    }
                }
            }
        }
    }
    int horseTimer = 0;  // this really shouldnt be nessisary but it breaks and spawns a bunch
    // for a bit before actually riding one for some reason. hacky fix but makes it slightly better. not perfect
    // the mount is somehow kicking it off and then removing itself ?

    int alertLevel = 0;

    @Override
    public boolean hurt(DamageSource source, float p_70097_2_) {
        if (source.getEntity() instanceof LivingEntity){
            if (this.getOwner() == null || !this.getOwner().getUUID().equals(source.getEntity().getUUID())){
                this.setTarget((LivingEntity) source.getEntity());
            }
        }

        return super.hurt(source, p_70097_2_);
    }

    // called when it runs out of food or money
    private void leaveOwner() {
        if (this.villageLocation != BlockPos.ZERO){
            this.getOwner().displayClientMessage(new StringTextComponent("One of your mercenaries returned to the village at " + this.villageLocation), true);

            ServerWorld world = ((ServerWorld) this.level).getServer().getLevel(World.OVERWORLD);  // TODO: should not assume the village is in the overworld just incase, RS compat etc
            if (world == null) world = (ServerWorld) this.level; // should never happen;

            this.changeDimension(world);
            this.teleportToWithTicket(this.villageLocation.getX(), this.villageLocation.getY(), this.villageLocation.getZ());
        } else {
            this.getOwner().displayClientMessage(new StringTextComponent("One of your mercenaries left"), true);
        }

        removeFromOwnerData();
        this.setMoveStance(MercenaryEntity.MovementStance.IDLE);
        this.setAttackStance(MercenaryEntity.AttackStance.DEFEND);
        this.setTarget(null);
        this.getNavigation().stop();
        this.setOwner(null);
    }

    private void removeFromOwnerData(){
        if (!this.level.isClientSide() && this.getOwner() != null) {
            SaveMercData.get().removeMerc((ServerPlayerEntity) this.getOwner(), this);
            this.setOwner(null);
        }
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand p_184230_2_) {
        if (!this.level.isClientSide()) {
            if (this.getOwner() == null) {
                NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new OpenMercRehireScreenPacket((ServerPlayerEntity) player, this));
            } else if (this.getOwner().getUUID().equals(player.getUUID())) {
                player.closeContainer();
                ((ServerPlayerEntity) player).nextContainerCounter();
                NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new OpenMercenaryInventoryPacket(((ServerPlayerEntity) player).containerCounter, this.getId()));
                player.containerMenu = new MerceneryContainer(((ServerPlayerEntity) player).containerCounter, player.inventory, this.inventory, this);
                player.containerMenu.addSlotListener(((ServerPlayerEntity) player));
            } else {
                player.displayClientMessage(new StringTextComponent("This is not your mercenary"), true);
            }
        }

        return ActionResultType.SUCCESS;
    }


    @Override
    public void performRangedAttack(LivingEntity p_82196_1_, float p_82196_2_) {
        ItemStack ammoStack = this.getProjectile(this.getItemInHand(Hand.MAIN_HAND)); //ProjectileHelper.getWeaponHoldingHand(this, Items.BOW)));
        if (ammoStack.isEmpty()) return;
        ammoStack.shrink(1);

        AbstractArrowEntity abstractarrowentity = ProjectileHelper.getMobArrow(this, ammoStack, p_82196_2_);
        if (this.getMainHandItem().getItem() instanceof net.minecraft.item.BowItem)
            abstractarrowentity = ((net.minecraft.item.BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrowentity);
        double xDir = p_82196_1_.getX() - this.getX();
        double yDir = p_82196_1_.getY(0.3333333333333333D) - abstractarrowentity.getY();
        double zDir = p_82196_1_.getZ() - this.getZ();
        double horizontalDistance = MathHelper.sqrt(xDir * xDir + zDir * zDir);
        abstractarrowentity.shoot(xDir, yDir + horizontalDistance * (double)0.2F, zDir, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));  // might want to change the inaccuracy here
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(abstractarrowentity);
    }

    public ItemStack getProjectile(ItemStack bowStack) {
        if (bowStack.getItem() instanceof ShootableItem) {
            Predicate<ItemStack> predicate = ((ShootableItem)bowStack.getItem()).getSupportedHeldProjectiles();
            ItemStack itemstack = ShootableItem.getHeldProjectile(this, predicate);
            if (!itemstack.isEmpty()) return itemstack;

            for (int i=2;i<20;i++){
                ItemStack checkAmmo = this.inventory.getItem(i);
                if (predicate.test(checkAmmo)) return checkAmmo;
            }
        }

        return ItemStack.EMPTY;
    }

    public int getFood() {
        return this.entityData.get(FOOD);
    }
    public int getMoney() {
        return this.entityData.get(MONEY);
    }

    public void jumpTime(long ticks) {
        this.moneyTimer += ticks;
        this.foodTimer += ticks;
    }

    public void setAttackStance(int stance) {
        this.entityData.set(ATTACK_STANCE, stance);
        if (this.isPassiveStace()){
            this.setTarget(null);
        }
    }
    public boolean isAttackStace(){
        return this.getAttackStance() == AttackStance.ATTACK;
    }
    public boolean isDefendStace(){
        return this.getAttackStance() == AttackStance.DEFEND;
    }
    public boolean isPassiveStace(){
        return this.getAttackStance() == AttackStance.PASSIVE;
    }
    public int getAttackStance() {
        return this.entityData.get(ATTACK_STANCE);
    }

    public void setMoveStance(int stance) {
        this.entityData.set(MOVE_STANCE, stance);
    }
    public int getMoveStance() {
        return this.entityData.get(MOVE_STANCE);
    }
    public boolean isFollowMode(){
        return this.getMoveStance() == MovementStance.FOLLOW;
    }
    public boolean isIdleMode(){
        return this.getMoveStance() == MovementStance.IDLE;
    }
    public boolean isStayMode(){
        return this.getMoveStance() == MovementStance.STAY;
    }

    FakePlayerThatRedirects fakePlayer;
    public PlayerEntity getFakePlayer() {
        if (this.fakePlayer == null) this.fakePlayer = new FakePlayerThatRedirects(this);
        return this.fakePlayer;
    }

    public void setOwner(PlayerEntity player) {
        if (player == null) {
            this.entityData.set(OWNER, Optional.empty());
        }
        else {
            this.entityData.set(OWNER, Optional.of(player.getUUID()));
        }
    }

    public PlayerEntity getOwner(){
        Optional<UUID> id = this.entityData.get(OWNER);
        if (id.isPresent()){
            return this.level.getPlayerByUUID(id.get());
        } else {
            return null;
        }
    }

    public boolean hasFindableTarget() {
        return this.getTarget() != null && this.getTarget().isAlive() && !this.isPassiveStace() && this.distanceToSqr(this.getTarget()) < 32*32;
    }

    public void setCamp(BlockPos pos) {
        this.entityData.set(CAMP, Optional.of(pos));
    }

    public BlockPos getCamp(){
        if (this.entityData.get(CAMP).isPresent()){
            return this.entityData.get(CAMP).get();
        } else {
            return null;
        }
    }

    public double getReachDistSq() {
        return Math.pow(getReachDist(), 2);
    }


    public enum AttackType{
        NONE,
        MELEE,
        RANGE,
        SHIELD,
        ARTIFACT
    }

    public AttackType getAttackType() {
        if (this.shieldCoolDown < 0) return AttackType.SHIELD;
        if (this.getArtifactCooldown() < 0) return AttackType.ARTIFACT;

        if (this.getMainHandItem().getItem().getAttributeModifiers(EquipmentSlotType.MAINHAND, this.getMainHandItem()).containsKey(Attributes.ATTACK_DAMAGE)){
            this.attackType = AttackType.MELEE;
        } else if (this.getMainHandItem().getItem() instanceof ShootableItem){
            this.attackType = AttackType.RANGE;
        } else {
            this.attackType = AttackType.NONE;
        }

        return this.attackType;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);

        ListNBT listnbt = new ListNBT();
        for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putByte("Slot", (byte)i);
                itemstack.save(compoundnbt);
                listnbt.add(compoundnbt);
            }
        }
        tag.put("Items", listnbt);

        tag.putInt("texture", this.entityData.get(TEXTURE_TYPE));
        tag.putInt("money", this.entityData.get(MONEY));
        tag.putInt("food", this.entityData.get(FOOD));
        tag.putInt("moneyTimer",  this.moneyTimer);
        tag.putInt("foodTimer",  this.foodTimer);
        tag.putInt("stance",  this.getAttackStance());
        tag.putInt("movestance",  this.getMoveStance());
        this.entityData.get(OWNER).ifPresent(owner -> {
            tag.putUUID("owner", owner);
        });
        this.entityData.get(CAMP).ifPresent(camp -> {
            tag.putInt("campx", camp.getX());
            tag.putInt("campy", camp.getY());
            tag.putInt("campz", camp.getZ());

            tag.putString("campDimension", this.campDimension.location().toString());
        });

        tag.putInt("villagex", this.villageLocation.getX());
        tag.putInt("villagey", this.villageLocation.getY());
        tag.putInt("villagez", this.villageLocation.getZ());

        tag.putInt("sharedArtifactCooldown",  this.sharedArtifactCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);

        ListNBT listnbt = tag.getList("Items", 10);

        for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 0 && j < this.inventory.getContainerSize()) {
                this.inventory.setItem(j, ItemStack.of(compoundnbt));
            }
        }

        this.entityData.set(TEXTURE_TYPE, tag.getInt("texture"));
        if (tag.contains("money")) this.entityData.set(MONEY, tag.getInt("money"));
        if (tag.contains("food")) this.entityData.set(FOOD, tag.getInt("food"));
        this.moneyTimer = tag.getInt("moneyTimer");
        this.foodTimer = tag.getInt("foodTimer");
        this.setAttackStance(tag.getInt("stance"));
        this.setMoveStance(tag.getInt("movestance"));
        if (tag.contains("owner")){
            this.entityData.set(OWNER, Optional.of(tag.getUUID("owner")));
        }

        if (tag.contains("campx")){
            this.entityData.set(CAMP, Optional.of(new BlockPos(tag.getInt("campx"), tag.getInt("campy"), tag.getInt("campz"))));
            this.campDimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("campDimension")));
        }

        this.villageLocation = new BlockPos(tag.getInt("villagex"), tag.getInt("villagey"), tag.getInt("villagez"));

        this.sharedArtifactCooldown = tag.getInt("sharedArtifactCooldown");
    }


    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean wasPlayer) {
        if (MercConfig.dropItemsOnDeath.get()){
            for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
                ItemStack itemstack = this.getItemBySlot(equipmentslottype);
                if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
                    this.spawnAtLocation(itemstack);
                    this.setItemSlot(equipmentslottype, ItemStack.EMPTY);
                }
            }

            for (int i=2;i<20;i++){
                this.spawnAtLocation(this.inventory.getItem(i));
            }
            this.inventory.clearContent();
        }
    }

    @Override
    public void die(DamageSource source) {
        System.out.println("die");
        super.die(source);
        removeFromOwnerData();
    }

    public ResourceLocation getTexture() {
        return MercTextureList.getMercTexture(this.getEntityData().get(TEXTURE_TYPE));
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset();
    }

    @Override
    public double getMyRidingOffset() {
        return super.getMyRidingOffset() - 0.4;
    }

    // artifact integration

    public CooldownTracker getCooldowns() {
        return this.cooldowns;
    }

    public void onStartUseArtifact(){
        this.sharedArtifactCooldown = -MercConfig.getTimeToUseArtifact();
    }

    public void onActuallyUseArtifact(){

    }

    public void onEndUseArtifact(){
        this.sharedArtifactCooldown = MercConfig.getSharedArtifactCooldown();
    }

    public int getArtifactCooldown() {
        return this.sharedArtifactCooldown;
    }

    public static class MovementStance {
        public static int FOLLOW = 0;
        public static int IDLE = 1;
        public static int STAY = 2;
    }

    public static class AttackStance {
        public static int ATTACK = 0;
        public static int DEFEND = 1;
        public static int PASSIVE = 2;
    }
}
