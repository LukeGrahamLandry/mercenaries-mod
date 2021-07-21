package ca.lukegrahamlandry.mercenaries;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class MercConfig {
    public static int getFoodDecayRate(){
        return 60;  // 24000 / 20 * 4
    }

    public static int getMoneyDecayRate(){
        return 60;  // 24000 / 20 * 4
    }

    public static int getMoneyValue(Item item){
        if (item == Items.EMERALD) return 4;
        if (item == Items.DIAMOND) return 19;

        return 0;
    }
}
