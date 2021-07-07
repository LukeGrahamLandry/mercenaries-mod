package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.mercenaries.network.NetworkInit;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ca.lukegrahamlandry.mercenaries.init.EntityInit;
import ca.lukegrahamlandry.mercenaries.init.ItemInit;

@Mod(MercenariesMain.MOD_ID)
public class MercenariesMain {
    public static final String MOD_ID = "mercenaries";

    public MercenariesMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ItemInit.ITEMS.register(modEventBus);
        EntityInit.ENTITY.register(modEventBus);
        NetworkInit.registerPackets();
    }
}
