package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SetMercCampPacket implements ServerSideHandler {
    private int id;

    public SetMercCampPacket(int entityId) {
        this.id = entityId;
    }

    @Override
    public void handle(ServerPlayer player) {
        Entity entity = player.level.getEntity(this.id);
        if (entity instanceof MercenaryEntity) {
            MercenaryEntity merc = (MercenaryEntity) entity;
            if (merc.isOwner(player)){
                merc.setCamp(merc.blockPosition());
                merc.data.synced.campDimension = merc.level.dimension().location();
            } else {
                MercenariesMod.LOGGER.error("SetMercCampPacket: " + player.getScoreboardName() + " does not own " + merc.getUUID());
            }
        }
    }
}

