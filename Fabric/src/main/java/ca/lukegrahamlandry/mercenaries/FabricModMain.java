package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.mercenaries.events.AddVillagerHouse;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        MercenariesMod.init();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (MercenariesMod.MERC_LIST.get() == null) MercenariesMod.MERC_LIST.clear();  // temp fix
            AddVillagerHouse.addNewVillageBuilding(server);
        });
    }
}
