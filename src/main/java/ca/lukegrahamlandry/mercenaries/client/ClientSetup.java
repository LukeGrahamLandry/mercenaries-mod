package ca.lukegrahamlandry.mercenaries.client;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.client.render.LeaderRenderer;
import ca.lukegrahamlandry.mercenaries.client.render.MercenaryRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import ca.lukegrahamlandry.mercenaries.init.EntityInit;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = MercenariesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void doSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.MERCENARY.get(), MercenaryRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.LEADER.get(), LeaderRenderer::new);

    }
}