package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.lib.keybind.KeybindWrapper;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicInteger;

public class MercKeybinds {
    public static void init() {
        KeybindWrapper.of("merc_stop", MercenariesMod.MOD_ID, GLFW.GLFW_KEY_U).synced().onPress(MercKeybinds::stopAction);
        KeybindWrapper.of("merc_attack", MercenariesMod.MOD_ID, GLFW.GLFW_KEY_T).synced().onPress(MercKeybinds::attackAction);
        KeybindWrapper.of("merc_defense", MercenariesMod.MOD_ID, GLFW.GLFW_KEY_Y).synced().onPress(MercKeybinds::defendAction);
    }

    private static void defendAction(Player player) {
        if (player.level.isClientSide()) return;

        AtomicInteger count = new AtomicInteger();
        MercenariesMod.MERC_LIST.get().forLoadedMercBelongingTo(player, (merc) -> {
            merc.setAttackStance(MercenaryEntity.AttackStance.DEFEND);
            merc.setMoveStance(MercenaryEntity.MovementStance.FOLLOW);
            count.getAndIncrement();
        });

        int total = MercenariesMod.MERC_LIST.get().getMercs(player).size();
        player.displayClientMessage(Component.literal(count.get() + "/" + total + " mercenaries set to defend-follow"), true);
    }

    private static void attackAction(Player player) {
        if (player.level.isClientSide()) return;

        AtomicInteger count = new AtomicInteger();
        MercenariesMod.MERC_LIST.get().forLoadedMercBelongingTo(player, (merc) -> {
            merc.setAttackStance(MercenaryEntity.AttackStance.ATTACK);
            merc.setMoveStance(MercenaryEntity.MovementStance.FOLLOW);
            count.getAndIncrement();
        });

        int total = MercenariesMod.MERC_LIST.get().getMercs(player).size();
        player.displayClientMessage(Component.literal(count.get() + "/" + total + " mercenaries set to attack-follow"), true);
    }

    private static void stopAction(Player player) {
        if (player.level.isClientSide()) return;

        AtomicInteger count = new AtomicInteger();
        MercenariesMod.MERC_LIST.get().forLoadedMercBelongingTo(player, (merc) -> {
            merc.setAttackStance(MercenaryEntity.AttackStance.PASSIVE);
            merc.setMoveStance(MercenaryEntity.MovementStance.FOLLOW);
            count.getAndIncrement();
        });

        int total = MercenariesMod.MERC_LIST.get().getMercs(player).size();
        player.displayClientMessage(Component.literal(count.get() + "/" + total + " mercenaries set to passive-follow"), true);
    }
}
