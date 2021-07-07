package ca.lukegrahamlandry.mercenaries.goals;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class MercMeleeAttackGoal extends MeleeAttackGoal {
    MercenaryEntity owner;
    public MercMeleeAttackGoal(MercenaryEntity mob, double speedMultiplier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedMultiplier, followingTargetEvenIfNotSeen);
        this.owner = mob;
    }

    @Override
    public boolean canUse() {
        return owner.getAttackType() == MercenaryEntity.AttackType.MELEE && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return owner.getAttackType() == MercenaryEntity.AttackType.MELEE && super.canContinueToUse();
    }
}
