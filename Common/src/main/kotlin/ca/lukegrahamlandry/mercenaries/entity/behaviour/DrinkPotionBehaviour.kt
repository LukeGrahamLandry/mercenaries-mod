package ca.lukegrahamlandry.mercenaries.entity.behaviour

import com.mojang.datafixers.util.Pair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionUtils
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour
import java.util.function.Function

/**
 * Causes the entity to drink a potion (like witches do).
 * A potion item will automatically be created and placed in the entity's main hand (deleting whatever was there before).
 * It will not start if the entity already has all the potion's effects.
 * - If you want to keep the old item, you could use .whenStarting to save it and .whenStopping to put it back.
 * - You could have potionFinder select one from its inventory and use .whenStopping to remove it from the inventory.
 */
class DrinkPotionBehaviour<E : LivingEntity?>(private val potionFinder: Function<E, Potion?>) : ExtendedBehaviour<E>() {
    constructor(potion: Potion?) : this(Function<E, Potion?> { potion })

    override fun shouldKeepRunning(entity: E): Boolean {
        return entity!!.isUsingItem
    }

    override fun start(self: E) {
        self!!.setItemSlot(
            EquipmentSlot.MAINHAND,
            PotionUtils.setPotion(ItemStack(Items.POTION), potionFinder.apply(self))
        )
        self.startUsingItem(InteractionHand.MAIN_HAND)
        self.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
    }

    override fun stop(self: E) {
        self!!.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY)
    }

    /**
     * If there is no potion to drink, can't start, obviously.
     * If you already have all the effects the potion will give (with equal or greater amplifier), don't bother drinking it.
     */
    override fun checkExtraStartConditions(serverLevel: ServerLevel, self: E): Boolean {
        val potion = potionFinder.apply(self) ?: return false
        for (fromPotion in potion.effects) {
            val current = self!!.getEffect(fromPotion.effect)
            if (current == null || current.amplifier < fromPotion.amplifier) return true
        }
        return false
    }

    override fun getMemoryRequirements(): List<Pair<MemoryModuleType<*>, MemoryStatus>> {
        return listOf()
    }
}