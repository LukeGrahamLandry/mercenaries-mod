package ca.lukegrahamlandry.mercenaries.init;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.items.MercEggItem;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MercenariesMain.MOD_ID);

    public static final RegistryObject<Item> MERC_EGG = ITEMS.register("merc_spawn_egg", () -> new MercEggItem(new Item.Properties().tab(ItemGroup.TAB_MISC), false));
    public static final RegistryObject<Item> LEADER_EGG = ITEMS.register("leader_spawn_egg", () -> new MercEggItem(new Item.Properties().tab(ItemGroup.TAB_MISC), true));
}
