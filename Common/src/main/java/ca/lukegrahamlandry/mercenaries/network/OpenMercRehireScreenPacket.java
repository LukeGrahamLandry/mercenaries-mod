package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.client.gui.RehireMercScreen;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class OpenMercRehireScreenPacket implements ClientSideHandler {
    private int entityId;
    private int price;

    public OpenMercRehireScreenPacket(ServerPlayer player, MercenaryEntity merc) {
        this.entityId = merc.getId();
        int owned = MercenariesMod.MERC_LIST.get().getMercs(player).size();
        if (owned >= MercenariesMod.CONFIG.get().maxMercs){
            this.price = Integer.MAX_VALUE;
        } else {
            this.price = MercenariesMod.CONFIG.get().rehirePrice;
        }
    }

    @Override
    public void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity instanceof MercenaryEntity) {
            MercenaryEntity merc = (MercenaryEntity) entity;
            Minecraft.getInstance().setScreen(new RehireMercScreen(merc, this.price));
        } else {
            MercenariesMod.LOGGER.error("entity " + this.entityId + " is not a MercenaryEntity");
        }
    }
}

