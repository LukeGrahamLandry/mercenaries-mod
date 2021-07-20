package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.mercenaries.client.container.MerceneryContainer;
import ca.lukegrahamlandry.mercenaries.goals.MercMeleeAttackGoal;
import ca.lukegrahamlandry.mercenaries.goals.MercRangeAttackGoal;
import ca.lukegrahamlandry.mercenaries.init.NetworkInit;
import ca.lukegrahamlandry.mercenaries.network.OpenMercenaryInventoryPacket;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Predicate;

public class MercenaryEntity extends CreatureEntity implements IRangedAttackMob {
    private AttackType attackType = AttackType.NONE;
    public Inventory inventory;
    public MercenaryEntity(EntityType<MercenaryEntity> p_i48576_1_, World p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
        this.inventory = new Inventory(24);
    }

    public static AttributeModifierMap.MutableAttribute makeAttributes() {
        return MonsterEntity.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.MOVEMENT_SPEED, (double)0.23F).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 2.0D).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        // have to redo all the goals because it's a player now

        // this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        // this.goalSelector.addGoal(8, new LookRandomlyGoal(this));

        this.goalSelector.addGoal(1, new MercMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(1, new MercRangeAttackGoal(this, 1.0D, 20, 15));
        // melee attack goal should do this on its own
        // this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
         this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MobEntity.class, 5, false, false, (p_234199_0_) -> {
            return p_234199_0_ instanceof IMob && !(p_234199_0_ instanceof CreeperEntity && this.getAttackType() == AttackType.MELEE);
        }));

        // this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand p_184230_2_) {
        if (!this.level.isClientSide()){
            player.closeContainer();

            ((ServerPlayerEntity)player).nextContainerCounter();
            NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new OpenMercenaryInventoryPacket(((ServerPlayerEntity)player).containerCounter, this.getId()));
            player.containerMenu = new MerceneryContainer(((ServerPlayerEntity)player).containerCounter, player.inventory, this.inventory, this);
            player.containerMenu.addSlotListener(((ServerPlayerEntity)player));
        }
        return ActionResultType.SUCCESS;
    }

    /*
    // todo: equipment should be done in gui instead
    @Override
    protected ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == Hand.MAIN_HAND && this.getAttackType() == AttackType.NONE){
            if (stack.getItem() instanceof SwordItem){
                this.attackType = AttackType.MELEE;
            } else if (stack.getItem() instanceof BowItem){
                this.attackType = AttackType.RANGE;
            } else if (stack.getItem() == Items.BOOK){
                this.attackType = AttackType.CLERIC;
            } else if (stack.getItem() == Items.STICK){
                this.attackType = AttackType.MAGE;
            }

            if (this.getAttackType() != AttackType.NONE){
                player.setItemInHand(hand, ItemStack.EMPTY);
                this.setItemInHand(Hand.MAIN_HAND, stack);
                return ActionResultType.CONSUME;
            }

            if (stack.getItem() instanceof ArmorItem){
                EquipmentSlotType slot = ((ArmorItem) stack.getItem()).getSlot();
                player.setItemInHand(hand, this.getItemBySlot(slot));
                this.setItemSlot(slot, stack);
                return ActionResultType.CONSUME;
            }
        }

        return super.mobInteract(player, hand);
    }
     */

    public ResourceLocation getTexture() {
        return new ResourceLocation("textures/null");
    }

    @Override
    public void performRangedAttack(LivingEntity p_82196_1_, float p_82196_2_) {
        ItemStack ammoStack = this.getProjectile(this.getItemInHand(ProjectileHelper.getWeaponHoldingHand(this, Items.BOW)));
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
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    // maybe instead of storing the attack type it should be done based on main hand item
    // it would still be good to have the get method so goals can reference this clearly
    public enum AttackType{
        NONE,
        MELEE,
        RANGE,
        CLERIC,
        MAGE
    }

    public AttackType getAttackType() {
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
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean wasPlayer) {
        super.dropCustomDeathLoot(source, looting, wasPlayer);
        for (int i=2;i<20;i++){
            this.spawnAtLocation(this.inventory.getItem(i));
        }
        this.inventory.clearContent();
    }
}