package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.wrapped.MercEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class SyncMercDataPacket implements ClientSideHandler {
    private int id;
    private MercEntityData data;

    public SyncMercDataPacket(MercenaryEntity merc){
        this.id = merc.getId();
        this.data = merc.data;
    }

    @Override
    public void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(this.id);
        if (entity instanceof MercenaryEntity) {
            ((MercenaryEntity) entity).data = this.data;
        }
    }
}
