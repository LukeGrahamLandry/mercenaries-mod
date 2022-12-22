package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.client.gui.MercenaryLeaderScreen;
import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class OpenLeaderScreenPacket implements ClientSideHandler {
    int entityId;
    int price;

    public OpenLeaderScreenPacket(Player player, LeaderEntity leader) {
        this.entityId = leader.getId();
        this.price = MercenariesMod.CONFIG.get().caclualteCurrentPrice(player);
    }

    @Override
    public void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity instanceof LeaderEntity) {
            LeaderEntity merc = (LeaderEntity) entity;
            Minecraft.getInstance().setScreen(new MercenaryLeaderScreen(merc, this.price));
        } else {
            System.out.println("ERROR: entity " + this.entityId + " is not a LeaderEntity");
        }
    }
}

