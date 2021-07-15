package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.client.container.MercenaryScreen;
import ca.lukegrahamlandry.mercenaries.client.container.MerceneryContainer;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// server -> client
public class OpenMercenaryInventoryPacket {
    private final int containerId;
    private final int entityId;

    public OpenMercenaryInventoryPacket(int containerId, int entityId) {
        this.containerId = containerId;
        this.entityId = entityId;
    }

    public OpenMercenaryInventoryPacket(PacketBuffer buf) {
        this(buf.readInt(), buf.readInt());
    }

    public static void toBytes(OpenMercenaryInventoryPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.containerId);
        buf.writeInt(msg.entityId);
    }

    public static void handle(OpenMercenaryInventoryPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> OpenMercenaryInventoryPacket.openGUI(msg));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openGUI(OpenMercenaryInventoryPacket packet) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level.getEntity(packet.entityId);
            if (entity instanceof MercenaryEntity) {
                MercenaryEntity merc = (MercenaryEntity) entity;
                MerceneryContainer container = new MerceneryContainer(packet.containerId, player.inventory, merc.inventory, merc);
                player.containerMenu = container;
                Minecraft.getInstance().setScreen(new MercenaryScreen(container, player.inventory, merc));
            } else {
                System.out.println("ERROR: entity " + packet.entityId + " is not a MercenaryEntity");
            }
        }
    }
}

