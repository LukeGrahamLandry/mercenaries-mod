package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import ca.lukegrahamlandry.mercenaries.entity.MercMountEntity;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.init.NetworkInit;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
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

        modEventBus.addListener(MercenariesMain::mobAttributes);
        MercConfig.init();
    }

    public static void mobAttributes(EntityAttributeCreationEvent event){
        event.put(EntityInit.MERCENARY.get(), MercenaryEntity.makeAttributes().build());
        event.put(EntityInit.LEADER.get(), LeaderEntity.makeAttributes().build());
        event.put(EntityInit.MOUNT.get(), MercMountEntity.makeAttributes().build());
    }
}
