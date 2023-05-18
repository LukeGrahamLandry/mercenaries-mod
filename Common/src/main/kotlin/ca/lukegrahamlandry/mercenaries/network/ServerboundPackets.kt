package ca.lukegrahamlandry.mercenaries.network

import ca.lukegrahamlandry.lib.network.ServerSideHandler
import ca.lukegrahamlandry.mercenaries.HireMercenary
import ca.lukegrahamlandry.mercenaries.MercenariesMod
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity.AttackStance
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity.MovementStance
import net.minecraft.server.level.ServerPlayer

class RehireMercPacket(var id: Int) : ServerSideHandler {
    override fun handle(player: ServerPlayer) {
        val entity = player.level.getEntity(id)
        if (entity is MercenaryEntity) HireMercenary.rehire(player, entity)
    }
}

class BuyNewMercPacket: ServerSideHandler {
    override fun handle(player: ServerPlayer) =
        HireMercenary.hireNew(player)
}

data class SetMercCampPacket(val id: Int): ServerSideHandler {
    override fun handle(player: ServerPlayer) =
        ifMercOwned(player, id, MercenaryEntity::setCampHere)
}

data class SetMercStancePacket(val id: Int, val attack: AttackStance, val move: MovementStance) : ServerSideHandler {
    override fun handle(player: ServerPlayer) =
        ifMercOwned(player, id) {
            it.attackStance = attack
            it.moveStance = move
        }
}

fun ifMercOwned(player: ServerPlayer, mercNetworkId: Int, action: (MercenaryEntity) -> Unit){
    val entity = player.level.getEntity(mercNetworkId)
    if (entity is MercenaryEntity && entity.isOwner(player)) action(entity)
    else MercenariesMod.LOGGER.error("Failed to run ifMercOwned for player=${player.scoreboardName} merc=${entity} id=$mercNetworkId")
}
