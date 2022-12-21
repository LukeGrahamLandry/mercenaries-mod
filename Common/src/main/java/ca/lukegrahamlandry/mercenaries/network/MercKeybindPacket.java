package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.SaveMercData;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

// client -> server
public class MercKeybindPacket {
    private int action;

    public MercKeybindPacket(int action) {
        this.action = action;
    }

    public MercKeybindPacket(PacketBuffer buf) {
        this(buf.readInt());
    }

    public static void toBytes(MercKeybindPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.action);
    }

    public static void handle(MercKeybindPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();

            if (msg.action == 0) stopAction(player);
            if (msg.action == 1) attackAction(player);
            if (msg.action == 2) defendAction(player);
        });
        context.get().setPacketHandled(true);
    }

    private static void defendAction(ServerPlayerEntity player) {
        AtomicInteger count = new AtomicInteger();
        SaveMercData.get().forLoadedMercBelongingTo(player, (merc) -> {
            merc.setAttackStance(MercenaryEntity.AttackStance.DEFEND);
            merc.setMoveStance(MercenaryEntity.MovementStance.FOLLOW);
            count.getAndIncrement();
        });

        int total = SaveMercData.get().getMercs(player).size();
        player.displayClientMessage(new StringTextComponent(count.get() + "/" + total + " mercenaries set to defend-follow"), true);
    }

    private static void attackAction(ServerPlayerEntity player) {
        AtomicInteger count = new AtomicInteger();
        SaveMercData.get().forLoadedMercBelongingTo(player, (merc) -> {
            merc.setAttackStance(MercenaryEntity.AttackStance.ATTACK);
            merc.setMoveStance(MercenaryEntity.MovementStance.FOLLOW);
            count.getAndIncrement();
        });

        int total = SaveMercData.get().getMercs(player).size();
        player.displayClientMessage(new StringTextComponent(count.get() + "/" + total + " mercenaries set to attack-follow"), true);
    }

    private static void stopAction(ServerPlayerEntity player) {
        AtomicInteger count = new AtomicInteger();
        SaveMercData.get().forLoadedMercBelongingTo(player, (merc) -> {
            merc.setAttackStance(MercenaryEntity.AttackStance.PASSIVE);
            merc.setMoveStance(MercenaryEntity.MovementStance.FOLLOW);
            count.getAndIncrement();
        });

        int total = SaveMercData.get().getMercs(player).size();
        player.displayClientMessage(new StringTextComponent(count.get() + "/" + total + " mercenaries set to passive-follow"), true);
    }
}
