package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class RehireMercPacket implements ServerSideHandler {
    int id;
    public RehireMercPacket(int id) {
        this.id = id;
    }

    @Override
    public void handle(ServerPlayer player) {
        Entity entity = player.level.getEntity(this.id);
        if (entity instanceof MercenaryEntity) {
            MercenaryEntity merc = (MercenaryEntity) entity;

            int price = MercenariesMod.CONFIG.get().rehirePrice;
            if (BuyNewMercPacket.payIfPossible(player, price, BuyNewMercPacket::isPayment)){
                merc.setOwner(player);
                MercenariesMod.MERC_LIST.get().addMerc(player, merc);

                // TODO: translatable
                player.displayClientMessage(Component.literal("Rehired mercenary!"), true);
            } else {
                player.displayClientMessage(Component.literal("You could not afford to rehire the mercenary"), true);
            }
        }
    }
}
