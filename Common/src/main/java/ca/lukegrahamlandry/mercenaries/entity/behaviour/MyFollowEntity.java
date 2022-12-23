package ca.lukegrahamlandry.mercenaries.entity.behaviour;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;

import java.util.function.BiFunction;

public class MyFollowEntity<E extends PathfinderMob, T extends Entity> extends FollowEntity<E, T> {
    protected Path previousNavigation;
    private BiFunction<E, T, Double> startFollowDistMin;

    public MyFollowEntity(){
        super();
        this.runFor((self) -> 100);  // give it a bit longer before it stops, so it looks less indecisive
    }

    // if stopFollowingWithin is lower than startFollowingAt it will just immediately stop so probably not what you want
    // if you don't set a startFollowingAt it will default to using the stopFollowingWithin like normal
    public MyFollowEntity<E, T> startFollowingAt(BiFunction<E, T, Double> distanceProvider) {
        this.startFollowDistMin = distanceProvider;
        return this;
    }

    public MyFollowEntity<E, T> startFollowingAt(double distance) {
        return startFollowingAt((entity, target) -> distance);
    }

    // the normal one has the same start and stop distances.
    // this one allows you to have it only start when A away but stop following when within B
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        T target = this.followingEntityProvider.apply(entity);

        if (target == null || target.isSpectator())
            return false;

        BiFunction<E, T, Double> distFunction = this.startFollowDistMin != null ? this.startFollowDistMin : this.followDistMin;
        double minDist = distFunction.apply(entity, target);

        return entity.distanceToSqr(target) > minDist * minDist;
    }

    // using the normal one it just immediately stops following and never moves.
    // this gets checked on the same tick as it starts (tested by printing Level#getGameTime on #start and #stop),
    // before the entity's navigation has been set to actually go to the WALK_TARGET.
    // so then the old navigation could be done (like if it was idle before) and it would think it already was done following.
    // so this checks to make sure the navigation that's done is going to the correct place before stopping
    @Override
    protected boolean shouldKeepRunning(E entity) {
        if (entity.getNavigation().getPath() != previousNavigation && entity.getNavigation().isDone()) return false;

        T target = this.followingEntityProvider.apply(entity);

        if (target == null) return false;

        double dist = entity.distanceToSqr(target);
        double minDist = this.followDistMin.apply(entity, target);

        return dist > minDist * minDist;
    }

    @Override
    protected void start(E entity) {
        super.start(entity);
        this.previousNavigation = entity.getNavigation().getPath();
    }
}