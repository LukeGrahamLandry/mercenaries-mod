package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// server -> client
public class SetMercStancePacket {
    private final int stance;
    private final int entityId;

    public SetMercStancePacket(int stance, int entityId) {
        this.stance = stance;
        this.entityId = entityId;
    }

    public SetMercStancePacket(PacketBuffer buf) {
        this(buf.readInt(), buf.readInt());
    }

    public static void toBytes(SetMercStancePacket msg, PacketBuffer buf) {
        buf.writeInt(msg.stance);
        buf.writeInt(msg.entityId);
    }

    public static void handle(SetMercStancePacket msg, Supplier<NetworkEvent.Context> context) {
        PlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> {
            Entity entity = player.level.getEntity(msg.entityId);
            if (entity instanceof MercenaryEntity) {
                MercenaryEntity merc = (MercenaryEntity) entity;
                merc.setStance(msg.stance);
            }
        });
        context.get().setPacketHandled(true);
    }
}

