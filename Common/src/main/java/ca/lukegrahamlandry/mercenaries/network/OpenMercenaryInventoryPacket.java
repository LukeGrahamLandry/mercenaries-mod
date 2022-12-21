package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import ca.lukegrahamlandry.mercenaries.client.gui.MercenaryScreen;
import ca.lukegrahamlandry.mercenaries.client.gui.MerceneryContainer;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class OpenMercenaryInventoryPacket implements ClientSideHandler {
    int containerId;
    int entityId;

    public OpenMercenaryInventoryPacket(int containerId, int entityId) {
        this.containerId = containerId;
        this.entityId = entityId;
    }

    @Override
    public void handle() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level.getEntity(this.entityId);
            if (entity instanceof MercenaryEntity) {
                MercenaryEntity merc = (MercenaryEntity) entity;
                MerceneryContainer container = new MerceneryContainer(this.containerId, player.getInventory(), merc.inventory, merc);
                player.containerMenu = container;
                Minecraft.getInstance().setScreen(new MercenaryScreen(container, player.getInventory(), merc));
            } else {
                System.out.println("ERROR: entity " + this.entityId + " is not a MercenaryEntity");
            }
        }
    }
}

