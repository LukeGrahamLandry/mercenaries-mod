package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.SaveMercData;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.init.EntityInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// client -> server
public class RehireMercPacket {
    int id;
    public RehireMercPacket(int id) {
        this.id = id;
    }

    public RehireMercPacket(PacketBuffer buf) {
        this(buf.readInt());
    }

    public static void toBytes(RehireMercPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.id);
    }

    public static void handle(RehireMercPacket msg, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> {
            Entity entity = player.level.getEntity(msg.id);
            if (entity instanceof MercenaryEntity) {
                MercenaryEntity merc = (MercenaryEntity) entity;

                // check if they can afford
                int cashHeld = 0;
                for (int i=0;i<player.inventory.getContainerSize();i++){
                    ItemStack stack = player.inventory.getItem(i);
                    if (stack.getItem() == MercConfig.buyMercItem()){
                        cashHeld += stack.getCount();
                    }
                }

                int price = MercConfig.rehirePrice.get();
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

                    merc.setOwner(player);
                    SaveMercData.get().addMerc(player, merc);
                    // alert
                    player.displayClientMessage(new StringTextComponent("Rehired mercenary!"), true);
                } else {
                    player.displayClientMessage(new StringTextComponent("You could not afford to rehire the mercenary"), true);
                }
            }
        });
        context.get().setPacketHandled(true);

    }
}
