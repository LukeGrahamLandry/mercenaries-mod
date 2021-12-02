package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.client.ClientSetup;
import ca.lukegrahamlandry.mercenaries.init.NetworkInit;
import ca.lukegrahamlandry.mercenaries.network.MercKeybindPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MercenariesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyboardEvents {
    @SubscribeEvent
    public static void onPress(InputEvent.KeyInputEvent event){
        if (Minecraft.getInstance().player == null) return;

        if (ClientSetup.STOP.isDown()) {
            NetworkInit.INSTANCE.sendToServer(new MercKeybindPacket(0));
        }

        if (ClientSetup.ATTACK.isDown()) {
            NetworkInit.INSTANCE.sendToServer(new MercKeybindPacket(1));
        }

        if (ClientSetup.DEFENSE.isDown()) {
            NetworkInit.INSTANCE.sendToServer(new MercKeybindPacket(2));
        }
    }
}