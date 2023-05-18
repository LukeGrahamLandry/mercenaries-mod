package ca.lukegrahamlandry.mercenaries

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

object HireMercenary {
    fun hireNew(player: ServerPlayer) {
        val price = MercenariesMod.CONFIG.get().caclualteCurrentPrice(player)
        if (payIfPossible(player, price, ::isPayment)) {
            val merc = MercRegistry.MERCENARY.get().create(player.level)
            merc!!.owner = player
            merc.setPos(player.x, player.y, player.z)
            merc.data.server.village = player.blockPosition()
            MercenariesMod.MERC_LIST.get().addMerc(player, merc)
            player.level.addFreshEntity(merc)

            // TODO: translatable
            player.displayClientMessage(Component.literal("Hired new mercenary!"), true)
        } else {
            player.displayClientMessage(Component.literal("You could not afford to hire a new mercenary"), true)
        }
    }

    fun rehire(player: ServerPlayer, merc: MercenaryEntity) {
        if (merc.hasOwner()) {
            MercenariesMod.LOGGER.error("RehireMercPacket: " + player.scoreboardName + " trying to rehire but " + merc.uuid + " already has owner")
            return
        }
        val price = MercenariesMod.CONFIG.get().rehirePrice
        if (payIfPossible(player, price, ::isPayment)) {
            merc.owner = player
            MercenariesMod.MERC_LIST.get().addMerc(player, merc)

            // TODO: translatable
            player.displayClientMessage(Component.literal("Rehired mercenary!"), true)
        } else {
            player.displayClientMessage(Component.literal("You could not afford to rehire the mercenary"), true)
        }
    }
}

fun isPayment(stack: ItemStack): Boolean {
    return Registry.ITEM.getKey(stack.item) == MercenariesMod.CONFIG.get().hirePaymentItem
}

/**
 * @param player the player to take payment items from
 * @param amount the number of items to pay
 * @param valid a predicate that returns true for valid payment item stacks
 * @return true if the player successfully paid
 */
fun payIfPossible(player: Player, amount: Int, valid: Predicate<ItemStack>): Boolean {
    if (player.isCreative) return true
    val inventory = player.inventory

    // check if they can afford
    var cashHeld = 0
    for (i in 0 until inventory.containerSize) {
        val stack = inventory.getItem(i)
        if (valid.test(stack)) cashHeld += stack.count
    }
    if (cashHeld < amount) return false

    // pay
    var remaining = amount
    for (i in 0 until inventory.containerSize) {
        val stack = inventory.getItem(i)
        if (valid.test(stack)) {
            if (stack.count > amount) {
                stack.shrink(amount)
                inventory.setItem(i, stack)
                remaining = 0
            } else {
                remaining -= stack.count
                inventory.setItem(i, ItemStack.EMPTY)
            }
            if (remaining <= 0) {
                // consumed enough money
                break
            }
        }
    }

    expect(remaining == 0, "payIfPossible should terminate without owing more money. $remaining")
    return true
}
