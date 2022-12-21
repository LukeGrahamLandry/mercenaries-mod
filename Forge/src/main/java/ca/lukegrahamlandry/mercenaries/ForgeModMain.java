package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.lib.base.event.EventWrapper;
import ca.lukegrahamlandry.mercenaries.block.RenderTravelAnchor;
import ca.lukegrahamlandry.mercenaries.render.TravelAnchorRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(TravelStaffMain.MOD_ID)
public class ForgeModMain {
    
    public ForgeModMain() {
        EventWrapper.init();
        TravelStaffMain.init();
        ForgeNetworkHandler.registerPackets();

        MinecraftForge.EVENT_BUS.register(new ForgeEventListener());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(this::renderAnchors));
    }

    public void renderAnchors(RenderLevelLastEvent event) {
        TravelAnchorRenderer.renderAnchors(event.getPoseStack(), event.getPartialTick());
    }

    @Mod.EventBusSubscriber(modid = TravelStaffMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvent {
        @SubscribeEvent
        public static void registerRender(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(TravelAnchorRegistry.TRAVEL_ANCHOR_TILE.get(), (ctx) -> new RenderTravelAnchor());
        }
    }
}