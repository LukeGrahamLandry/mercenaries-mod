package ca.lukegrahamlandry.mercenaries.entity.behaviour;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

// can be used with ItemTemptingSensor
public class FollowTemptingPlayer<E extends PathfinderMob> extends MyFollowEntity<E, Player> {
    public FollowTemptingPlayer(){
        this.following((self) -> self.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER).orElse(null));
    }
}
