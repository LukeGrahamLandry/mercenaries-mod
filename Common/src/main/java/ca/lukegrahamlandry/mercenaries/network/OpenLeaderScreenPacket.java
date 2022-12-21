package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.client.gui.MercenaryLeaderScreen;
import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// server -> client
public class OpenLeaderScreenPacket {
    private final int entityId;
    private final int price;
    private int diologueCount;
    private boolean firstInteraction;

    public OpenLeaderScreenPacket(int entityId, int price, int diologueCount, boolean firstInteraction) {
        this.entityId = entityId;
        this.price = price;
        this.diologueCount = diologueCount;
        this.firstInteraction = firstInteraction;
    }

    public OpenLeaderScreenPacket(ServerPlayerEntity player, LeaderEntity leader) {
        this.entityId = leader.getId();
        this.price = MercConfig.caclualteCurrentPrice(player);
        this.firstInteraction = !leader.playerInteractions.contains(player.getUUID());
        this.diologueCount = MercConfig.dialogueLinesCount.get().get(firstInteraction ? 0 : 1);
    }

    public OpenLeaderScreenPacket(PacketBuffer buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public static void toBytes(OpenLeaderScreenPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.price);
        buf.writeInt(msg.diologueCount);
        buf.writeBoolean(msg.firstInteraction);
    }

    public static void handle(OpenLeaderScreenPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> OpenLeaderScreenPacket.openGUI(msg));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openGUI(OpenLeaderScreenPacket packet) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level.getEntity(packet.entityId);
            if (entity instanceof LeaderEntity) {
                LeaderEntity merc = (LeaderEntity) entity;
                Minecraft.getInstance().setScreen(new MercenaryLeaderScreen(player, merc, packet.price, packet.diologueCount, packet.firstInteraction));
            } else {
                System.out.println("ERROR: entity " + packet.entityId + " is not a LeaderEntity");
            }
        }
    }
}

