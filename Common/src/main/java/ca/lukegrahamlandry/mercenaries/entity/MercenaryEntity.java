package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.client.MercTextureList;
import ca.lukegrahamlandry.mercenaries.client.gui.MerceneryContainer;
import ca.lukegrahamlandry.mercenaries.goals.MercFollowGoal;
import ca.lukegrahamlandry.mercenaries.goals.MercMeleeAttackGoal;
import ca.lukegrahamlandry.mercenaries.goals.MercRangeAttackGoal;
import ca.lukegrahamlandry.mercenaries.goals.MercShieldGoal;
import ca.lukegrahamlandry.mercenaries.network.OpenMercRehireScreenPacket;
import ca.lukegrahamlandry.mercenaries.network.OpenMercenaryInventoryPacket;
import ca.lukegrahamlandry.mercenaries.wrapped.MercEntityData;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class MercenaryEntity extends Mob implements IRangedAttackMob {
    MercEntityData data = new MercEntityData();

    public SimpleContainer inventory = new SimpleContainer(24);
    public MercenaryEntity(EntityType<MercenaryEntity> p_i48576_1_, Level p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
        if (!this.level.isClientSide()) {
            if (!this.hasCustomName() && !MercenariesMod.CONFIG.get().names.isEmpty()){
                String myName = MercenariesMod.CONFIG.get().names.get(this.getRandom().nextInt(MercenariesMod.CONFIG.get().names.size()));
                this.setCustomName(Component.translatable(myName));
            }
        }
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder makeAttributes() {
        return Mob.createMobAttributes().add(Attributes.FOLLOW_RANGE, 32.0D).add(Attributes.MOVEMENT_SPEED, (double)0.33F).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.ARMOR, 0.0D);
    }

    public double getReachDist(){
        return 4; //this.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
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
    }

    private boolean canTarget(LivingEntity target) {
        if (!(target instanceof Enemy)) return false;
        if (target instanceof Enemy && this.getAttackType() == AttackType.MELEE) return false;

        if (this.isAttackStace()) {
            return !this.isStayMode() || this.distanceToSqr(target) < this.getReachDistSq();
        }

        return false;
    }

    private int moneyValue(ItemStack stack){
        return MercenariesMod.CONFIG.get().itemMoneyValue.getOrDefault(Registry.ITEM.getKey(stack.getItem()), 0);
    }

    private int foodValue(ItemStack stack){
        return stack.getItem().isEdible() ? stack.getItem().getFoodProperties().getNutrition() : 0;
    }

    private void sendOwnerMessage(Component msg){
        if (this.getOwner() == null) return;
        this.getOwner().displayClientMessage(msg, true);
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSwingTime();

        if (!this.level.isClientSide()){
            this.data.server.shieldCoolDown -= Math.signum(this.data.server.shieldCoolDown);

            this.data.server.foodTimer++;
            if (this.data.server.foodTimer++ > MercenariesMod.CONFIG.get().foodDecayRate){
                if (this.data.synced.food.consume(this.inventory, this::foodValue, this::sendOwnerMessage)) this.leaveOwner();
                this.data.server.foodTimer -= MercenariesMod.CONFIG.get().foodDecayRate;
            }

            this.data.server.moneyTimer++;
            if (this.data.server.moneyTimer++ > MercenariesMod.CONFIG.get().moneyDecayRate){
                if (this.data.synced.money.consume(this.inventory, this::moneyValue, this::sendOwnerMessage)) this.leaveOwner();
                this.data.server.moneyTimer -= MercenariesMod.CONFIG.get().moneyDecayRate;
            }
        }
    }

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
        if (this.data.server.village != null){
            // TODO: translatable
            this.sendOwnerMessage(Component.literal("One of your mercenaries returned to the village at " + this.data.server.village));

            ServerLevel world = ((ServerLevel) this.level).getServer().getLevel(ServerLevel.OVERWORLD);  // TODO: should not assume the village is in the overworld just in case, RS compat etc
            if (world == null) world = (ServerLevel) this.level; // should never happen;

            this.changeDimension(world);
            this.teleportToWithTicket(this.data.server.village.getX(), this.data.server.village.getY(), this.data.server.village.getZ());
        } else {
            this.sendOwnerMessage(Component.literal("One of your mercenaries left"));
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
        return this.data.synced.food.value;
    }
    public int getMoney() {
        return this.data.synced.money.value;
    }

    public void jumpTime(long ticks) {
        this.data.server.moneyTimer += ticks;
        this.data.server.foodTimer += ticks;
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

    public void setOwner(Player player) {
        if (player == null) this.data.server.owner = null;
        else this.data.server.owner = player.getUUID();
    }

    public Player getOwner(){
        if (this.data.server.owner != null){
            return this.level.getPlayerByUUID(this.data.server.owner);
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("mercdata", JsonHelper.get().toJson(this.data));

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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("mercdata")) this.data = JsonHelper.get().fromJson(tag.getString("mercdata"), MercEntityData.class);

        ListNBT listnbt = tag.getList("Items", 10);

        for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 0 && j < this.inventory.getContainerSize()) {
                this.inventory.setItem(j, ItemStack.of(compoundnbt));
            }
        }
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

    public enum MovementStance {
        FOLLOW,
        IDLE,
        STAY
    }

    public enum AttackStance {
        ATTACK,
        DEFEND,
        PASSIVE
    }
}
