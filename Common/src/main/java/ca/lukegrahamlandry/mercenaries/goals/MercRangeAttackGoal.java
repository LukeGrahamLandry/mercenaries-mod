package ca.lukegrahamlandry.mercenaries.goals;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;


public class MercRangeAttackGoal extends SimpleRangedAttackGoal {
    MercenaryEntity owner;
    public MercRangeAttackGoal(MercenaryEntity mob, double speedMultiplier, int attackInterval, float range) {
        super(mob, speedMultiplier, attackInterval, range);
        this.owner = mob;
    }

    @Override
    public boolean canUse() {
        return owner.getAttackType() == MercenaryEntity.AttackType.RANGE && super.canUse() && !this.owner.getProjectile(this.owner.getItemInHand(Hand.MAIN_HAND)).isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return owner.getAttackType() == MercenaryEntity.AttackType.RANGE && super.canContinueToUse() && !this.owner.getProjectile(this.owner.getItemInHand(Hand.MAIN_HAND)).isEmpty();
    }
}
