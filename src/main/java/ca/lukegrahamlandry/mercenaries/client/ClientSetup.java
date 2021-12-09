package ca.lukegrahamlandry.mercenaries.client;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.client.render.LeaderRenderer;
import ca.lukegrahamlandry.mercenaries.client.render.MercMountRenderer;
import ca.lukegrahamlandry.mercenaries.client.render.MercenaryRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import ca.lukegrahamlandry.mercenaries.init.EntityInit;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(modid = MercenariesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    public static final KeyBinding STOP = new KeyBinding("key.merc_stop", GLFW.GLFW_KEY_U, "key.categories.mercenaries");
    public static final KeyBinding ATTACK = new KeyBinding("key.merc_attack", GLFW.GLFW_KEY_T, "key.categories.mercenaries");
    public static final KeyBinding DEFENSE = new KeyBinding("key.merc_defense", GLFW.GLFW_KEY_Y, "key.categories.mercenaries");

    @SubscribeEvent
    public static void doSetup(FMLClientSetupEvent event) {
        MercTextureList.clientInit();

        RenderingRegistry.registerEntityRenderingHandler(EntityInit.MERCENARY.get(), MercenaryRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.LEADER.get(), LeaderRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.MOUNT.get(), MercMountRenderer::new);

        ClientRegistry.registerKeyBinding(STOP);
        ClientRegistry.registerKeyBinding(ATTACK);
        ClientRegistry.registerKeyBinding(DEFENSE);
    }
}