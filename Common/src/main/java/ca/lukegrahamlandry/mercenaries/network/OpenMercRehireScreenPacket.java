package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.SaveMercData;
import ca.lukegrahamlandry.mercenaries.client.gui.MercenaryLeaderScreen;
import ca.lukegrahamlandry.mercenaries.client.gui.RehireMercScreen;
import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
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
public class OpenMercRehireScreenPacket {
    private final int entityId;
    private final int price;

    public OpenMercRehireScreenPacket(int entityId, int price) {
        this.entityId = entityId;
        this.price = price;
    }

    public OpenMercRehireScreenPacket(ServerPlayerEntity player, MercenaryEntity merc) {
        this.entityId = merc.getId();
        int owned = SaveMercData.get().getMercs(player).size();
        if (owned >= MercConfig.maxMercs()){
            this.price = Integer.MAX_VALUE;
        } else {
            this.price = MercConfig.rehirePrice.get();
        }
    }

    public OpenMercRehireScreenPacket(PacketBuffer buf) {
        this(buf.readInt(), buf.readInt());
    }

    public static void toBytes(OpenMercRehireScreenPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.price);
    }

    public static void handle(OpenMercRehireScreenPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> OpenMercRehireScreenPacket.openGUI(msg));
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openGUI(OpenMercRehireScreenPacket packet) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level.getEntity(packet.entityId);
            if (entity instanceof MercenaryEntity) {
                MercenaryEntity merc = (MercenaryEntity) entity;
                Minecraft.getInstance().setScreen(new RehireMercScreen(player, merc, packet.price));
            } else {
                System.out.println("ERROR: entity " + packet.entityId + " is not a MercenaryEntity");
            }
        }
    }
}

