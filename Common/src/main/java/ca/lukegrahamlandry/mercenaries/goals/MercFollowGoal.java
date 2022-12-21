package ca.lukegrahamlandry.mercenaries.goals;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.EnumSet;

public class MercFollowGoal extends Goal {
    private final MercenaryEntity merc;
    private LivingEntity owner;
    private final IWorldReader level;
    private final double speedModifier;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public MercFollowGoal(MercenaryEntity p_i225711_1_) {
        this.merc = p_i225711_1_;
        this.level = p_i225711_1_.level;
        this.speedModifier = 0.8;
        this.stopDistance = 4;
        this.canFly = false;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        if (!(p_i225711_1_.getNavigation() instanceof GroundPathNavigator) && !(p_i225711_1_.getNavigation() instanceof FlyingPathNavigator)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean canUse() {
        LivingEntity livingentity = this.merc.getOwner();
        if (livingentity == null) {
            return false;
        } else if (livingentity.isSpectator()) {
            return false;
        } else if (!this.merc.isFollowMode()) {
            return false;
        }

        double distSq = this.merc.distanceToSqr(livingentity);


        if (distSq < MercConfig.getIdleFollowDistance()) {
            return false;
        } else {
            if (this.merc.hasFindableTarget() && distSq < MercConfig.getFightingFollowDistance()){
                return false;
            }
            this.owner = livingentity;
            return true;
        }
    }

    public boolean canContinueToUse() {
        if (this.merc.getNavigation().isDone()) {
            return false;
        } else if (!this.merc.isFollowMode()) {
            return false;
        } else {
            boolean wantsToAttack = this.merc.hasFindableTarget() && this.merc.getAttackType() != MercenaryEntity.AttackType.NONE;
            return this.merc.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance) && !wantsToAttack;
        }
    }

    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.merc.getPathfindingMalus(PathNodeType.WATER);
        this.merc.setPathfindingMalus(PathNodeType.WATER, 0.0F);
    }

    public void stop() {
        this.owner = null;
        this.merc.getNavigation().stop();
        this.merc.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);
    }

    public void tick() {
        this.merc.getLookControl().setLookAt(this.owner, 10.0F, (float)this.merc.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.merc.isLeashed()) {
                if (this.merc.distanceToSqr(this.owner) >= MercConfig.getTeleportDistance()) {
                    this.teleportToOwner();
                } else {
                    this.merc.getNavigation().moveTo(this.owner, this.speedModifier);
                }

            }
        }
    }

    private void teleportToOwner() {
        BlockPos blockpos = this.owner.blockPosition();

        for(int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int p_226328_1_, int p_226328_2_, int p_226328_3_) {
        if (Math.abs((double)p_226328_1_ - this.owner.getX()) < 2.0D && Math.abs((double)p_226328_3_ - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(p_226328_1_, p_226328_2_, p_226328_3_))) {
            return false;
        } else {
            this.merc.moveTo((double)p_226328_1_ + 0.5D, (double)p_226328_2_, (double)p_226328_3_ + 0.5D, this.merc.yRot, this.merc.xRot);
            this.merc.getNavigation().stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos p_226329_1_) {
        PathNodeType pathnodetype = WalkNodeProcessor.getBlockPathTypeStatic(this.level, p_226329_1_.mutable());
        if (pathnodetype != PathNodeType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.level.getBlockState(p_226329_1_.below());
            if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = p_226329_1_.subtract(this.merc.blockPosition());
                return this.level.noCollision(this.merc, this.merc.getBoundingBox().move(blockpos));
            }
        }
    }

    private int randomIntInclusive(int p_226327_1_, int p_226327_2_) {
        return this.merc.getRandom().nextInt(p_226327_2_ - p_226327_1_ + 1) + p_226327_1_;
    }
}