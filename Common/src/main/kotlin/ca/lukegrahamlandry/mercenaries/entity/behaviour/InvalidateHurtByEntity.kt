package ca.lukegrahamlandry.mercenaries.entity.behaviour

import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.player.Player
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour
import net.tslat.smartbrainlib.api.util.BrainUtils
import org.apache.commons.lang3.function.ToBooleanBiFunction

// Based on https://github.com/Tslat/SmartBrainLib/blob/1.19/Common/src/main/java/net/tslat/smartbrainlib/api/core/behaviour/custom/target/InvalidateAttackTarget.java
// This file is distributed under the MPL 2.0
// I feel like i shouldn't need this because HurtBySensor clears it but it doesn't seem to be working, idk
class InvalidateHurtByEntity<E : Mob?> : ExtendedBehaviour<E>() {
    var customPredicate =
        ToBooleanBiFunction { _: E, target: LivingEntity -> target is Player && (target.isCreative || target.isSpectator()) }

    override fun getMemoryRequirements(): List<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return MEMORY_REQUIREMENTS
    }

    /**
     * Sets a custom predicate to invalidate the HURT_BY_ENTITY if none of the previous checks invalidate it first.<br></br>
     * Overrides the default player gamemode check
     */
    fun invalidateIf(predicate: ToBooleanBiFunction<E, LivingEntity>): InvalidateHurtByEntity<E> {
        customPredicate = predicate
        return this
    }

    override fun start(entity: E) {
        val target = entity!!.brain.getMemory(MemoryModuleType.HURT_BY_ENTITY)
            .orElse(null)
            ?: return
        if (isTargetInvalid(entity, target) || customPredicate.applyAsBoolean(entity, target)) {
            BrainUtils.clearMemory(entity, MemoryModuleType.HURT_BY_ENTITY)
        }
    }

    protected fun isTargetInvalid(entity: E, target: LivingEntity): Boolean {
        return if (entity!!.level !== target.level) true else target.isDeadOrDying || target.isRemoved
    }

    companion object {
        private val MEMORY_REQUIREMENTS: List<Pair<MemoryModuleType<*>, MemoryStatus>> = ObjectArrayList.of(
            Pair.of(
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT
            )
        )
    }
}