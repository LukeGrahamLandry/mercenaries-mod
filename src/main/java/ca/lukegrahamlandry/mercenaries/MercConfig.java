package ca.lukegrahamlandry.mercenaries;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fml.ModList;

public class MercConfig {
    public static int getFoodDecayRate(){
        return 24000 / 20 * 4;
    }

    public static int getMoneyDecayRate(){
        return 24000 / 20 * 4;
    }

    public static int getMoneyValue(Item item){
        if (item == Items.EMERALD) return 4;
        if (item == Items.DIAMOND) return 19;

        return 0;
    }

    public static boolean artifactsInstalled(){
        return isModLoaded("dungeons_gear");
    }

    private static boolean isModLoaded(String modID) {
        return ModList.get() != null && ModList.get().getModContainerById(modID).isPresent();
    }

    // ticks between using any artifacts (in addition to their cool down)
    public static int getSharedArtifactCooldown() {
        return 100;
    }

    // ticks it takes the merc to use an artifact
    public static int getTimeToUseArtifact() {
        return 30;
    }
}
