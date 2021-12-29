package ca.lukegrahamlandry.mercenaries.goals;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import com.mojang.datafixers.util.Pair;
import javafx.util.StringConverter;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.UUID;

public class MercShieldGoal extends Goal {
    public final MercenaryEntity merc;
    private static final AttributeModifier USE_SHIELD_PENALTY = new AttributeModifier(UUID.fromString("bf4eae77-0d1e-4ff5-a6fa-1692687cd035"), "Use shield penalty", -0.25D, AttributeModifier.Operation.ADDITION);


    public MercShieldGoal(MercenaryEntity merc) {
        this.merc = merc;
    }

    public Hand getShieldHand(){
        ItemStack held = merc.getItemInHand(Hand.OFF_HAND);
        if (held.getItem().isShield(held, merc)){
            return Hand.OFF_HAND;
        }

        merc.getItemInHand(Hand.MAIN_HAND);
        if (held.getItem().isShield(held, merc)){
            return Hand.MAIN_HAND;
        }

        return null;
    }

    Reason reason;
    enum Reason{
        CREEPER, SKELETON;
    }

    @Override
    public boolean canUse() {
        boolean available = !CrossbowItem.isCharged(merc.getItemInHand(Hand.MAIN_HAND)) && getShieldHand() != null && merc.shieldCoolDown == 0;
        if (!available) return false;

        List<CreeperEntity> creepers = this.merc.level.getEntitiesOfClass(CreeperEntity.class, this.merc.getBoundingBox().inflate(4, 2, 4), (c) -> c.getSwellDir() > 0);
        if (creepers.size() > 0){
            this.reason = Reason.CREEPER;
            this.merc.setTarget(creepers.get(0));
            return true;
        }

        LivingEntity target = this.merc.getTarget();
        if (target instanceof IRangedAttackMob && target.isAlive() && this.merc.getAttackType() == MercenaryEntity.AttackType.MELEE && this.merc.distanceToSqr(target) > this.merc.getReachDistSq()) {
            this.reason = Reason.SKELETON;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !this.done && this.merc.shieldCoolDown < 0;
    }


    @Override
    public void start() {
        Hand shieldHand = getShieldHand();
        if (shieldHand != null){
            this.done = false;
            merc.startUsingItem(shieldHand);

            ModifiableAttributeInstance speedAtrr = this.merc.getAttribute(Attributes.MOVEMENT_SPEED);
            speedAtrr.removeModifier(USE_SHIELD_PENALTY);
            speedAtrr.addTransientModifier(USE_SHIELD_PENALTY);

            this.merc.shieldCoolDown = -80;
        }
    }

    boolean done = false;

    @Override
    public void tick() {
        super.tick();

        if (this.merc.getTarget() == null || !this.merc.getTarget().isAlive() || !(this.merc.getTarget() instanceof CreeperEntity || this.merc.getTarget() instanceof IRangedAttackMob)) {
            this.done = true;
            return;
        }

        this.merc.lookAt(EntityAnchorArgument.Type.EYES, this.merc.getTarget().getEyePosition(0));
        if (this.reason == Reason.SKELETON){
            Path path = this.merc.getNavigation().createPath(this.merc.getTarget(), 1);
            this.merc.getNavigation().moveTo(path, 1);

            if (this.merc.distanceToSqr(this.merc.getTarget()) < this.merc.getReachDistSq()){
                this.done = true;
                this.merc.getNavigation().stop();
            }
        } else if (this.reason == Reason.CREEPER){
            this.merc.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        merc.stopUsingItem();
        this.merc.shieldCoolDown = 150;
        ModifiableAttributeInstance speedAtrr = this.merc.getAttribute(Attributes.MOVEMENT_SPEED);
        speedAtrr.removeModifier(USE_SHIELD_PENALTY);
    }

}