package ca.lukegrahamlandry.mercenaries

import ca.lukegrahamlandry.lib.base.GenericHolder
import ca.lukegrahamlandry.lib.base.json.JsonHelper
import ca.lukegrahamlandry.lib.data.DataWrapper
import ca.lukegrahamlandry.lib.data.impl.GlobalDataWrapper
import ca.lukegrahamlandry.lib.network.ClientSideHandler
import ca.lukegrahamlandry.mercenaries.client.ClientHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import kotlin.reflect.KClass

// TODO: same for unsynced server data
interface SyncedData<T : Any> {
    var syncedData: T
    val defaultSyncedData: () -> T

    val syncedDataKey: String
        get() = "WrapperLibSyncedData" + this.javaClass.name

    fun loadSyncedData(tag: CompoundTag) {
        tag.putString(this.syncedDataKey, JsonHelper.get().toJson(this.syncedData))
    }

    fun saveSyncedData(tag: CompoundTag) {
        val default = this.defaultSyncedData()
        this.syncedData = if (tag.contains(this.syncedDataKey)) JsonHelper.get()
            .fromJson(tag.getString(this.syncedDataKey), default::class.java) else default
    }

    fun setDirty() {
        SyncedDataUpdateMsg(self.id, GenericHolder(this.syncedData)).sendToTrackingClients(self)
    }
}

private val <T : Any> SyncedData<T>.self
    get() = (this as Entity)

private fun <T : Any> SyncedData<T>.unsafeSetSyncedData(data: Any) {
    require(this.self.level.isClientSide())
    this.syncedData = data as T
}

fun <T : Any> globalSyncSaveDataWrapper(clazz: KClass<T>) =
    DataWrapper.global(clazz.java).saved<GlobalDataWrapper<T>>()
        .synced<GlobalDataWrapper<T>>()!!

data class SyncedDataUpdateMsg<T : Any>(val networkId: Int, val data: GenericHolder<T>) : ClientSideHandler {
    override fun handle() {
        val self = ClientHelper.getEntity(this.networkId)
        if (self is SyncedData<*>) self.unsafeSetSyncedData(this.data.get())
    }
}

fun expect(flag: Boolean, msg: String){
    if (!flag) {
        throw RuntimeException(msg)
    }
}
