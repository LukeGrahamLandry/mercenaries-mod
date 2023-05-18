package ca.lukegrahamlandry.mercenaries.entity.behaviour

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.level.pathfinder.Path
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity
import java.util.function.BiFunction

open class MyFollowEntity<E : PathfinderMob?, T : Entity?> : FollowEntity<E, T>() {
    protected var previousNavigation: Path? = null
    private var startFollowDistMin: BiFunction<E, T, Double>? = null

    init {
        runFor { 100 } // give it a bit longer before it stops, so it looks less indecisive
    }

    // if stopFollowingWithin is lower than startFollowingAt it will just immediately stop so probably not what you want
    // if you don't set a startFollowingAt it will default to using the stopFollowingWithin like normal
    fun startFollowingAt(distanceProvider: BiFunction<E, T, Double>?): MyFollowEntity<E, T> {
        startFollowDistMin = distanceProvider
        return this
    }

    fun startFollowingAt(distance: Double): MyFollowEntity<E, T> {
        return startFollowingAt { entity: E, target: T -> distance }
    }

    // the normal one has the same start and stop distances.
    // this one allows you to have it only start when A away but stop following when within B
    override fun checkExtraStartConditions(level: ServerLevel, entity: E): Boolean {
        val target = followingEntityProvider.apply(entity)
        if (target == null || target.isSpectator) return false
        val distFunction = if (startFollowDistMin != null) startFollowDistMin else followDistMin
        val minDist = distFunction!!.apply(entity, target)
        return entity!!.distanceToSqr(target) > minDist * minDist
    }

    // using the normal one it just immediately stops following and never moves.
    // this gets checked on the same tick as it starts (tested by printing Level#getGameTime on #start and #stop),
    // before the entity's navigation has been set to actually go to the WALK_TARGET.
    // so then the old navigation could be done (like if it was idle before) and it would think it already was done following.
    // so this checks to make sure the navigation that's done is going to the correct place before stopping
    override fun shouldKeepRunning(entity: E): Boolean {
        if (entity!!.navigation.path !== previousNavigation && entity!!.navigation.isDone) return false
        val target = followingEntityProvider.apply(entity) ?: return false
        val dist = entity!!.distanceToSqr(target)
        val minDist = followDistMin.apply(entity, target)
        return dist > minDist * minDist
    }

    override fun start(entity: E) {
        super.start(entity)
        previousNavigation = entity!!.navigation.path
    }
}