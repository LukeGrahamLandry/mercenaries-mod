package ca.lukegrahamlandry.mercenaries;

import net.fabricmc.api.ModInitializer;

public class FabricModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        MercenariesMod.init();
    }
}
