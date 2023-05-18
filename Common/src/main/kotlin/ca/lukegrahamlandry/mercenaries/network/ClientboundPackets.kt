package ca.lukegrahamlandry.mercenaries.network

import ca.lukegrahamlandry.lib.network.ClientSideHandler
import ca.lukegrahamlandry.mercenaries.MercenariesMod
import ca.lukegrahamlandry.mercenaries.client.ClientHelper
import ca.lukegrahamlandry.mercenaries.client.gui.MercenaryLeaderScreen
import ca.lukegrahamlandry.mercenaries.client.gui.MercenaryScreen
import ca.lukegrahamlandry.mercenaries.client.gui.MerceneryContainer
import ca.lukegrahamlandry.mercenaries.client.gui.RehireMercScreen
import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

data class OpenMercRehireScreenPacket(val entityId: Int, val price: Int) : ClientSideHandler {
    override fun handle() =
        ifIsMercenary(entityId) {
            ClientHelper.setScreen {
                RehireMercScreen(it, price)
            }
        }

    companion object {
        fun create(player: ServerPlayer, merc: MercenaryEntity): OpenMercRehireScreenPacket {
            return OpenMercRehireScreenPacket(merc.id, MercenariesMod.CONFIG.get().calculateRehirePrice(player))
        }
    }
}

data class OpenMercenaryInventoryPacket(var containerId: Int, var entityId: Int) : ClientSideHandler {
    override fun handle() =
        ifIsMercenary(entityId) {
            val player = ClientHelper.player ?: return@ifIsMercenary
            val container = MerceneryContainer(containerId, player.inventory, it.inventory, it)
            player.containerMenu = container
            ClientHelper.setScreen {
                MercenaryScreen(container, player.inventory, it)
            }
        }
}

data class OpenLeaderScreenPacket(val entityId: Int, val price: Int) : ClientSideHandler {
    override fun handle() {
        val entity = Minecraft.getInstance().level!!.getEntity(entityId)
        if (entity is LeaderEntity) ClientHelper.setScreen {
            MercenaryLeaderScreen(entity, price)
        } else MercenariesMod.LOGGER.error("entity $entityId is not a LeaderEntity")
    }

    companion object {
        fun create(player: Player, leader: LeaderEntity) = OpenLeaderScreenPacket(leader.id, MercenariesMod.CONFIG.get().calculateCurrentPrice(player))
    }
}

fun ifIsMercenary(mercNetworkId: Int, action: (MercenaryEntity) -> Unit){
    val entity = ClientHelper.getEntity(mercNetworkId)
    if (entity is MercenaryEntity) action(entity)
    else MercenariesMod.LOGGER.error("Failed to run ifMercOwned for id=$mercNetworkId merc=${entity}")
}
