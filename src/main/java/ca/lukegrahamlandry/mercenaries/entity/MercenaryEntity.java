package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.mercenaries.goals.MercMeleeAttackGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.BusBuilder;

public class MercenaryEntity extends CreatureEntity {
    private AttackType attackType = AttackType.NONE;
    public MercenaryEntity(EntityType<MercenaryEntity> p_i48576_1_, World p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
    }

    public static AttributeModifierMap.MutableAttribute makeAttributes() {
        return MonsterEntity.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.MOVEMENT_SPEED, (double)0.23F).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 2.0D).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));

        this.goalSelector.addGoal(1, new MercMeleeAttackGoal(this, 1.0D, true));
        // TODO: dont make ranged people charge into combat
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MobEntity.class, 5, false, false, (p_234199_0_) -> {
            return p_234199_0_ instanceof IMob && !(p_234199_0_ instanceof CreeperEntity && this.getAttackType() == AttackType.MELEE);
        }));

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

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

    public ResourceLocation getTexture() {
        return null;
    }

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
}
