package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import ca.lukegrahamlandry.mercenaries.MercRegistry;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class BuyNewMercPacket implements ServerSideHandler {
    @Override
    public void handle(ServerPlayer player) {
        int price = MercenariesMod.CONFIG.get().caclualteCurrentPrice(player);
        if (payIfPossible(player, price, BuyNewMercPacket::isPayment)){
            MercenaryEntity merc = MercRegistry.MERCENARY.get().create(player.level);
            merc.setOwner(player);
            merc.setPos(player.getX(), player.getY(), player.getZ());
            merc.data.server.village = player.blockPosition();
            MercenariesMod.MERC_LIST.get().addMerc(player, merc);
            player.level.addFreshEntity(merc);

            // TODO: translatable
            player.displayClientMessage(Component.literal("Hired new mercenary!"), true);
        } else {
            player.displayClientMessage(Component.literal("You could not afford to hire a new mercenary"), true);
        }
    }

    public static boolean isPayment(ItemStack stack){
        return Registry.ITEM.getKey(stack.getItem()).equals(MercenariesMod.CONFIG.get().hirePaymentItem);
    }

    /**
     * @param player the player to take payment items from
     * @param amount the number of items to pay
     * @param valid a predicate that returns true for valid payment item stacks
     * @return true if the player successfully paid
     */
    public static boolean payIfPossible(Player player, int amount, Predicate<ItemStack> valid){
        if (player.isCreative()) return true;

        Inventory inventory = player.getInventory();
        // check if they can afford
        int cashHeld = 0;
        for (int i=0;i<inventory.getContainerSize();i++){
            ItemStack stack = inventory.getItem(i);
            if (valid.test(stack)) cashHeld += stack.getCount();
        }

        if (cashHeld < amount) return false;
        
        // pay
        for (int i=0;i<inventory.getContainerSize();i++){
            ItemStack stack = inventory.getItem(i);
            if (valid.test(stack)){
                if (stack.getCount() > amount){
                    stack.shrink(amount);
                    inventory.setItem(i, stack);
                    amount = 0;
                } else {
                    amount -= stack.getCount();
                    inventory.setItem(i, ItemStack.EMPTY);
                }

                if (amount <= 0){
                    // consumed enough money
                    break;
                }
            }
        }
        
        return true;
    }
}
