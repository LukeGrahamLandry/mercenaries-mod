package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.SaveMercData;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.init.EntityInit;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// client -> server
public class BuyNewMercPacket {
    public BuyNewMercPacket() {
    }

    public BuyNewMercPacket(PacketBuffer buf) {
        this();
    }

    public static void toBytes(BuyNewMercPacket msg, PacketBuffer buf) {

    }

    public static void handle(BuyNewMercPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();

            // check if they can afford
            int cashHeld = 0;
            for (int i=0;i<player.inventory.getContainerSize();i++){
                ItemStack stack = player.inventory.getItem(i);
                if (stack.getItem() == MercConfig.buyMercItem()){
                    cashHeld += stack.getCount();
                }
            }

            int price = MercConfig.caclualteCurrentPrice(player);
            if (cashHeld >= price){
                // pay
                for (int i=0;i<player.inventory.getContainerSize();i++){
                    ItemStack stack = player.inventory.getItem(i);
                    if (stack.getItem() == MercConfig.buyMercItem()){
                        if (stack.getCount() > price){
                            stack.shrink(price);
                            player.inventory.setItem(i, stack);
                            price = 0;
                        } else {
                            price -= stack.getCount();
                            player.inventory.setItem(i, ItemStack.EMPTY);
                        }

                        if (price <= 0){
                            // consumed enough money
                            break;
                        }
                    }
                }

                // create merc
                MercenaryEntity merc = EntityInit.MERCENARY.get().create(player.level);
                merc.setOwner(player);
                merc.setPos(player.getX(), player.getY(), player.getZ());
                merc.villageLocation = player.blockPosition();
                SaveMercData.get().addMerc(player, merc);
                player.level.addFreshEntity(merc);

                // alert
                player.displayClientMessage(new StringTextComponent("Hired new mercenary!"), true);
            } else {
                player.displayClientMessage(new StringTextComponent("You could not afford to hire a new mercenary"), true);
            }
        });
        context.get().setPacketHandled(true);
    }
}
