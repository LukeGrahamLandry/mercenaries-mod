package ca.lukegrahamlandry.mercenaries.data

import ca.lukegrahamlandry.mercenaries.MercenariesMod
import ca.lukegrahamlandry.mercenaries.VersionImpl
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity.AttackStance
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity.MovementStance
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

data class MercServerData (
    var foodTimer: Int = 0,
    var moneyTimer: Int = 0,
    var owner: UUID? = null,
    var village: BlockPos? = null,

    // > 0 when cant use, < 0 when using
    var shieldCoolDown: Int = 0
)

data class MercSyncedData (
    var texture: ResourceLocation = ResourceLocation(MercenariesMod.MOD_ID, "textures/entity/merc/default_1.png"),
    var food: InvResource = InvResource("food"),
    var money: InvResource = InvResource("money"),
    var attackStance: AttackStance = AttackStance.ATTACK,
    var moveStance: MovementStance = MovementStance.FOLLOW,
    var camp: BlockPos? = null,
    var campDimension: ResourceLocation = Level.OVERWORLD.location()
)

data class InvResource(private val name: String, var value: Int = total) {
    fun consume(
        inventory: SimpleContainer,
        points: Function<ItemStack, Int>,
        message: Consumer<Component>
    ): Boolean {
        value--
        val needed = total - value
        for (i in 0 until inventory.containerSize - 4) {
            val stack = inventory.getItem(i)
            if (points.apply(stack) in 1..needed) {
                stack.shrink(1)
                value += points.apply(stack)
                break
            }
        }

        if (value <= 4) {
            message.accept(VersionImpl.translatableText(MercenariesMod.MOD_ID + ".alert.2." + name))
        }
        else if (value <= 8) {
            message.accept(VersionImpl.translatableText(MercenariesMod.MOD_ID + ".alert.1." + name))
        }

        return value <= 0
    }

    companion object {
        var total = 20
    }
}