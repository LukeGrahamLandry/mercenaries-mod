package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.client.gui.MerceneryContainer;
import ca.lukegrahamlandry.mercenaries.entity.behaviour.InvalidateHurtByEntity;
import ca.lukegrahamlandry.mercenaries.entity.behaviour.MyFollowEntity;
import ca.lukegrahamlandry.mercenaries.network.OpenMercRehireScreenPacket;
import ca.lukegrahamlandry.mercenaries.network.OpenMercenaryInventoryPacket;
import ca.lukegrahamlandry.mercenaries.network.SyncMercDataPacket;
import ca.lukegrahamlandry.mercenaries.wrapped.MercEntityData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.BowAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.*;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class MercenaryEntity extends PathfinderMob implements RangedAttackMob, SmartBrainOwner<MercenaryEntity> {
    public MercEntityData data = new MercEntityData();

    private SimpleContainer inventory = new SimpleContainer(24);
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
        return Mob.createMobAttributes().add(Attributes.FOLLOW_RANGE, 32).add(Attributes.MOVEMENT_SPEED, 0.33F).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.ARMOR, 0);
    }

    public double getReachDist(){
        return 4; //this.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
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
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);

        this.data.server.shieldCoolDown -= Math.signum(this.data.server.shieldCoolDown);

        this.data.server.foodTimer++;
        if (this.data.server.foodTimer++ > MercenariesMod.CONFIG.get().foodDecayRate){
            if (this.data.synced.food.consume(this.getInventory(), this::foodValue, this::sendOwnerMessage)) this.leaveOwner();
            this.data.server.foodTimer -= MercenariesMod.CONFIG.get().foodDecayRate;
            this.syncData();
        }

        this.data.server.moneyTimer++;
        if (this.data.server.moneyTimer++ > MercenariesMod.CONFIG.get().moneyDecayRate){
            if (this.data.synced.money.consume(this.getInventory(), this::moneyValue, this::sendOwnerMessage)) this.leaveOwner();
            this.data.server.moneyTimer -= MercenariesMod.CONFIG.get().moneyDecayRate;
            this.syncData();
        }
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
        if (!this.level.isClientSide() && this.hasOwner()) {
            MercenariesMod.MERC_LIST.get().removeMerc(this.data.server.owner, this);
            this.setOwner(null);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level.isClientSide()) {
            ServerPlayer p = (ServerPlayer) player;
            if (!this.hasOwner()) {
                new OpenMercRehireScreenPacket(p, this).sendToClient(p);
            } else if (this.isOwner(player)) {
                player.closeContainer();
                p.nextContainerCounter();
                new OpenMercenaryInventoryPacket(((ServerPlayer)player).containerCounter, this.getId()).sendToClient(p);
                player.containerMenu = new MerceneryContainer(p.containerCounter, player.getInventory(), this.getInventory(), this);
                p.initMenu(player.containerMenu);
            } else {
                player.displayClientMessage(Component.literal("This is not your mercenary"), true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.hasRanged()){
            ItemStack ammoStack = this.getProjectile(this.getItemInHand(InteractionHand.MAIN_HAND));
            if (ammoStack.isEmpty()) return;
            ammoStack.shrink(1);

            AbstractArrow arrow = ProjectileUtil.getMobArrow(this, ammoStack, power);
            double xDir = target.getX() - this.getX();
            double yDir = target.getY(0.333D) - arrow.getY();
            double zDir = target.getZ() - this.getZ();
            double horizontalDistance = Mth.sqrt((float) (xDir * xDir + zDir * zDir));
            arrow.shoot(xDir, yDir + horizontalDistance * 0.2F, zDir, 1.6F, 14 - this.level.getDifficulty().getId() * 4);  // might want to change the inaccuracy here
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level.addFreshEntity(arrow);
        } else if (this.hasTnt()) {
            this.getOffhandItem().shrink(1);
            this.getMainHandItem().setDamageValue(this.getMainHandItem().getDamageValue() + 1);

            PrimedTnt tnt = EntityType.TNT.create(this.level);
            tnt.setPos(this.getBoundingBox().getCenter());
            double xDir = target.getX() - this.getX();
            double yDir = target.getY(0.333D) - tnt.getY();
            double zDir = target.getZ() - this.getZ();
            double horizontalDistance = Mth.sqrt((float) (xDir * xDir + zDir * zDir));

            Vec3 vec3 = (new Vec3(xDir, yDir + horizontalDistance * 0.2F, zDir)).normalize();
            tnt.setDeltaMovement(vec3);
            this.level.addFreshEntity(tnt);
        }
    }

    public ItemStack getProjectile(ItemStack bowStack) {
        if (bowStack.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem)bowStack.getItem()).getSupportedHeldProjectiles();
            ItemStack itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
            if (!itemstack.isEmpty()) return itemstack;

            for (int i=2;i<20;i++){
                ItemStack checkAmmo = this.getInventory().getItem(i);
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

    public void skipTime(long ticks) {
        this.data.server.moneyTimer += ticks;
        this.data.server.foodTimer += ticks;
    }

    public void setAttackStance(AttackStance stance) {
        this.data.synced.attackStance = stance;
        this.syncData();
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
    public AttackStance getAttackStance() {
        return this.data.synced.attackStance;
    }

    public void setMoveStance(MovementStance stance) {
        this.data.synced.moveStance = stance;
        this.syncData();
    }

    private void syncData() {
        new SyncMercDataPacket(this).sendToAllClients();
    }

    public MovementStance getMoveStance() {
        return this.data.synced.moveStance;
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

    // doing a null check on this is not the same as hasOwner because the owner might be offline
    public Player getOwner(){
        if (this.data.server.owner != null){
            return this.level.getPlayerByUUID(this.data.server.owner);
        } else {
            return null;
        }
    }

    public boolean hasOwner(){
        return this.data.server.owner != null;
    }


    public void setCamp(BlockPos pos) {
        this.data.synced.camp = pos;
        this.syncData();
    }

    public BlockPos getCamp(){
        return this.data.synced.camp;
    }

    public double getReachDistSq() {
        return Math.pow(getReachDist(), 2);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("mercdata", JsonHelper.get().toJson(this.data));

        ListTag listnbt = new ListTag();
        for(int i = 0; i < this.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = this.getInventory().getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundnbt = new CompoundTag();
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
        this.syncData();

        ListTag listnbt = tag.getList("Items", 10);

        for(int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 0 && j < this.getInventory().getContainerSize()) {
                this.getInventory().setItem(j, ItemStack.of(compoundnbt));
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean wasPlayer) {
        if (MercenariesMod.CONFIG.get().dropItemsOnDeath){
            for(EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = this.getItemBySlot(slot);
                if (!stack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(stack)) {
                    this.spawnAtLocation(stack);
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }

            for (int i=2;i<20;i++){
                this.spawnAtLocation(this.getInventory().getItem(i));
            }
            this.getInventory().clearContent();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.sendOwnerMessage(Component.literal("Your mercenary died."));
        removeFromOwnerData();
    }

    public ResourceLocation getTexture() {
        return this.data.synced.texture;
    }

    public SimpleContainer getInventory() {
        return inventory;
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

    @Override
    public void setTarget(@Nullable LivingEntity livingEntity) {
        super.setTarget(livingEntity);
        if (livingEntity != null){
            this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity);
        } else {
            this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }

    }

    // AI Helpers

    public boolean isOwner(Player player) {
        return Objects.equals(player.getUUID(), this.data.server.owner);
    }

    private boolean canTarget(LivingEntity target) {
        if (!(target instanceof Enemy)) return false;
        if (target instanceof Creeper && !this.hasRanged()) return false;

        if (this.isAttackStace()) {
            return !this.isStayMode() || this.distanceToSqr(target) < this.getReachDistSq() || this.hasRanged();
        }

        return false;
    }

    private boolean hasMelee(){
        return this.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE);
    }

    private boolean hasRanged(){
        return this.getMainHandItem().getItem() instanceof ProjectileWeaponItem && !this.getProjectile(this.getItemInHand(InteractionHand.MAIN_HAND)).isEmpty();
    }

    private boolean hasTnt(){
        return this.getMainHandItem().getItem() instanceof FlintAndSteelItem && this.getOffhandItem().is(Items.TNT);
    }

    // AI

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public List<ExtendedSensor<MercenaryEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyLivingEntitySensor<>(),
                new HurtBySensor<>()
        );
    }

    @Override
    public BrainActivityGroup<MercenaryEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new FloatToSurfaceOfFluid<>(),
                new MoveToWalkTarget<MercenaryEntity>()
                        .startCondition((self) -> self.data.synced.moveStance != MovementStance.STAY),
                new FirstApplicableBehaviour<MercenaryEntity>(
                        new StrafeTarget<MercenaryEntity>()
                                .startCondition(MercenaryEntity::hasRanged),
                        new SetWalkTargetToAttackTarget<MercenaryEntity>()
                                .startCondition(MercenaryEntity::hasMelee),
                        new AvoidEntity<MercenaryEntity>()
                                .avoiding((entity) -> Objects.equals(entity, this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)))
                                .startCondition((self) -> self.hasTnt() && self.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)),
                        new MyFollowEntity<MercenaryEntity, LivingEntity>()
                                .startFollowingAt((s, t) -> 10D)
                                .following((self) -> self.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null))
                                .startCondition((self) -> self.hasTnt() && self.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))
                )
        );
    }

    @Override
    public BrainActivityGroup<MercenaryEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<MercenaryEntity>(
                        new SetRetaliateTarget<>().attackablePredicate((e) -> e.isAlive() && (!(e instanceof Player) || !this.isOwner((Player) e))),
                        new TargetOrRetaliate<>().attackablePredicate((e) -> e.isAlive() && this.canTarget(e)),
                        new SetPlayerLookTarget<>()
                ),
                new FirstApplicableBehaviour<MercenaryEntity>(
                        new MyFollowEntity<MercenaryEntity, Player>()
                                .startFollowingAt((s, t) -> (double) MercenariesMod.CONFIG.get().followDistance.idle)
                                .following(MercenaryEntity::getOwner)
                                .stopFollowingWithin((s, t) -> 4D)
                                .teleportToTargetAfter((s, t) -> (double) MercenariesMod.CONFIG.get().followDistance.teleport)
                        ,
                        new OneRandomBehaviour<MercenaryEntity>(
                                new SetRandomWalkTarget<MercenaryEntity>(),
                                new Idle<MercenaryEntity>()
                                        .runFor(entity -> entity.getRandom().nextInt(30, 60))
                        )
                )
        );
    }

    @Override
    public BrainActivityGroup<MercenaryEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new StopAttackingIfTargetInvalid<>(target -> target instanceof Player && ((Player)target).isCreative()),
                new InvalidateHurtByEntity<>(),
                new FirstApplicableBehaviour<MercenaryEntity>(
                        new MyFollowEntity<MercenaryEntity, Player>()
                                .startFollowingAt((s, t) -> (double) MercenariesMod.CONFIG.get().followDistance.fighting)
                                .following(MercenaryEntity::getOwner)
                                .stopFollowingWithin((s, t) -> 4D)
                                .teleportToTargetAfter((s, t) -> (double) MercenariesMod.CONFIG.get().followDistance.teleport),
                        new BowAttack<MercenaryEntity>(20)
                                .startCondition(MercenaryEntity::hasRanged),
                        new AnimatableMeleeAttack<MercenaryEntity>(0)
                                .startCondition(MercenaryEntity::hasMelee),
                        new AnimatableRangedAttack<MercenaryEntity>(0)
                                .attackRadius(10)
                                .startCondition(MercenaryEntity::hasTnt)
                )
        );
    }
}
