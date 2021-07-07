package ca.lukegrahamlandry.mercenaries.init;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import net.minecraft.item.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MercenariesMain.MOD_ID);

    private static Item.Properties props(){
        return new Item.Properties().tab(ModCreativeTab.instance);
    }

    public static class ModCreativeTab extends ItemGroup {
        public static final ModCreativeTab instance = new ModCreativeTab(ItemGroup.TABS.length, "mercenaries");
        private ModCreativeTab(int index, String label) {
            super(index, label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.EMERALD_ORE);
        }
    }
}
