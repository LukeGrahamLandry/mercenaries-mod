package ca.lukegrahamlandry.mercenaries.goals;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;

public class MercRangeAttackGoal extends SimpleRangedAttackGoal {
    MercenaryEntity owner;
    public MercRangeAttackGoal(MercenaryEntity mob, double speedMultiplier, int attackInterval, float range) {
        super(mob, speedMultiplier, attackInterval, range);
        this.owner = mob;
    }

    @Override
    public boolean canUse() {
        return owner.getAttackType() == MercenaryEntity.AttackType.RANGE && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return owner.getAttackType() == MercenaryEntity.AttackType.RANGE && super.canContinueToUse();
    }
}
