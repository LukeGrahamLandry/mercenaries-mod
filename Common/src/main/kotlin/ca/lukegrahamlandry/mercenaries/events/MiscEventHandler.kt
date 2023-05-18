package ca.lukegrahamlandry.mercenaries.events

import ca.lukegrahamlandry.mercenaries.MercenariesMod
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Enemy
import java.util.function.Consumer

object MiscEventHandler {
    fun onSleep(level: ServerLevel, newTime: Long) {
        val oldTime = level.dayTime()
        val difference = 24000 - oldTime + newTime
        level.allEntities.forEach(Consumer { entity: Entity? ->
            if (entity is MercenaryEntity) {
                entity.skipTime(difference)
                entity.heal(20f)
            }
        })
    }

    fun onAttack(target: Enemy?, source: DamageSource) {
        if (target !is ServerPlayer) return
        if (source.entity !is LivingEntity) return
        MercenariesMod.MERC_LIST.get().forLoadedMercBelongingTo((target as ServerPlayer?)!!) { merc: MercenaryEntity? ->
            if (merc!!.isDefendStace) merc.target = source.entity as LivingEntity?
        }
    }

    fun onPlayerDeath(player: ServerPlayer?) {
        MercenariesMod.MERC_LIST.get().forLoadedMercBelongingTo(player!!) { merc: MercenaryEntity? ->
            val pos = merc!!.camp
            if (pos != null) {
                merc.moveStance = MercenaryEntity.MovementStance.IDLE
                merc.attackStance = MercenaryEntity.AttackStance.DEFEND
                var level = (merc.level as ServerLevel).server.getLevel(
                    ResourceKey.create(
                        Registry.DIMENSION_REGISTRY,
                        merc.syncedData.campDimension
                    )
                )
                if (level == null) level = merc.level as ServerLevel // should never happen;
                merc.target = null
                merc.navigation.stop()
                merc.changeDimension(level)
                merc.teleportToWithTicket(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            }
        }
    }
}