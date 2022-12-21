package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SetMercStancePacket implements ServerSideHandler {
    private MercenaryEntity.AttackStance attackStance;
    private MercenaryEntity.MovementStance moveStace;
    private int entityId;

    public SetMercStancePacket(MercenaryEntity.AttackStance attackStance, MercenaryEntity.MovementStance moveStace, int entityId) {
        this.attackStance = attackStance;
        this.moveStace = moveStace;
        this.entityId = entityId;
    }

    @Override
    public void handle(ServerPlayer player) {
        Entity entity = player.level.getEntity(this.entityId);
        if (entity instanceof MercenaryEntity) {
            MercenaryEntity merc = (MercenaryEntity) entity;
            if (merc.isOwner(player)){
                merc.setAttackStance(this.attackStance);
                merc.setMoveStance(this.moveStace);
                MercenariesMod.LOGGER.error("SetMercStancePacket: " + player.getScoreboardName() + " does not own " + this.entityId);
            }
        }
    }
}

