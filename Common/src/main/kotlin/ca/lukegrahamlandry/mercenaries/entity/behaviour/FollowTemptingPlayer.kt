package ca.lukegrahamlandry.mercenaries.entity.behaviour

import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.player.Player

// can be used with ItemTemptingSensor
class FollowTemptingPlayer<E : PathfinderMob?> : MyFollowEntity<E, Player?>() {
    init {
        following { self: E -> self!!.brain.getMemory(MemoryModuleType.TEMPTING_PLAYER).orElse(null) }
    }
}