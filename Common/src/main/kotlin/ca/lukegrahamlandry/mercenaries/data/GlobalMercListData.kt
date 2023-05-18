package ca.lukegrahamlandry.mercenaries.data

import ca.lukegrahamlandry.mercenaries.MercenariesMod
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import java.util.*
import java.util.function.Consumer

data class GlobalMercListData(private var mercs: HashMap<UUID, ArrayList<UUID>> = HashMap<UUID, ArrayList<UUID>>()) {
    fun addMerc(player: Player, merc: MercenaryEntity) {
        val owned = mercs.getOrDefault(player.uuid, ArrayList())
        owned.add(merc.uuid)
        mercs[player.uuid] = owned
        MercenariesMod.MERC_LIST.setDirty()
    }

    fun removeMerc(player: UUID, merc: MercenaryEntity) {
        val owned = mercs.getOrDefault(player, ArrayList())
        owned.remove(merc.uuid)
        MercenariesMod.MERC_LIST.setDirty()
    }

    fun getMercs(player: Player): ArrayList<UUID> {
        return mercs.getOrDefault(player.uuid, ArrayList())
    }

    fun forLoadedMercBelongingTo(player: Player, action: Consumer<MercenaryEntity?>) {
        if (player.level.isClientSide()) throw RuntimeException("Cannot call GlobalMercListData#forLoadedMercBelongingTo on the client")
        for (mID in getMercs(player)) {
            val maybeMerc = (player.level as ServerLevel).getEntity(mID)
            if (maybeMerc is MercenaryEntity) {
                action.accept(maybeMerc as MercenaryEntity?)
            }
        }
    }
}