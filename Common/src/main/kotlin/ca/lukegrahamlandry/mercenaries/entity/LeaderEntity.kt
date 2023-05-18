package ca.lukegrahamlandry.mercenaries.entity

import ca.lukegrahamlandry.mercenaries.MercenariesMod
import ca.lukegrahamlandry.mercenaries.entity.behaviour.DrinkPotionBehaviour
import ca.lukegrahamlandry.mercenaries.entity.behaviour.FollowTemptingPlayer
import ca.lukegrahamlandry.mercenaries.network.OpenLeaderScreenPacket
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.tslat.smartbrainlib.api.SmartBrainOwner
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.SmartBrainProvider
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.HoldItem
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRetaliateTarget
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor
import kotlin.math.floor

class LeaderEntity(entityType: EntityType<LeaderEntity>, level: Level) :
    PathfinderMob(entityType, level), SmartBrainOwner<LeaderEntity> {

    private fun canTarget(target: LivingEntity): Boolean {
        if (target !is Enemy) return false
        if (target is Creeper || target is Villager) return false
        val distSq = this.distanceToSqr(target)
        return distSq < 9
    }

    var inCombat = false
    override fun tick() {
        super.tick()
        updateSwingTime()
    }

    private val sword: ItemStack
        get() = ItemStack(Items.WOODEN_SWORD)

    override fun hurt(source: DamageSource, damage: Float): Boolean {
        val entity = source.entity
        if (entity is LivingEntity) {
            entity.hurt(
                DamageSource.thorns(this),
                (Math.floorDiv(floor(damage.toDouble()).toInt(), 5) + 1).toFloat()
            )
        }
        return super.hurt(source, damage)
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        if (!level.isClientSide()) {
            OpenLeaderScreenPacket.create(player, this).sendToClient(player as ServerPlayer)
        }
        return InteractionResult.SUCCESS
    }

    init {
        setPersistenceRequired()
        xpReward = 20
    }

    // TODO: use loot table
    override fun dropCustomDeathLoot(damageSource: DamageSource, i: Int, bl: Boolean) {
        super.dropCustomDeathLoot(damageSource, i, bl)
        this.spawnAtLocation(ItemStack(Items.DIAMOND, getRandom().nextInt(8)))
        this.spawnAtLocation(ItemStack(Items.EMERALD_BLOCK, getRandom().nextInt(16)))
        this.spawnAtLocation(ItemStack(Items.GOLD_INGOT, getRandom().nextInt(32)))
    }

    override fun customServerAiStep() {
        tickBrain(this)
    }

    override fun brainProvider() = SmartBrainProvider(this) as Brain.Provider<*>

    override fun getSensors(): List<ExtendedSensor<LeaderEntity>> {
        return ObjectArrayList.of<ExtendedSensor<LeaderEntity>>(
            NearbyLivingEntitySensor(),
            HurtBySensor(),
            ItemTemptingSensor<LeaderEntity>()
                .setTemptingItems(Ingredient.of(Items.EMERALD_BLOCK, Items.EMERALD))
        )
    }

    override fun getCoreTasks(): BrainActivityGroup<LeaderEntity> {
        return BrainActivityGroup.coreTasks(
            LookAtTarget(),
            FloatToSurfaceOfFluid(),
            MoveToWalkTarget()
        )
    }

    override fun getIdleTasks(): BrainActivityGroup<LeaderEntity> {
        return BrainActivityGroup.idleTasks(
            HoldItem<LivingEntity>().startCondition { self: LivingEntity -> !self.mainHandItem.`is`(Items.POTION) },
            FirstApplicableBehaviour(
                SetRetaliateTarget(),
                SetPlayerLookTarget(),
                SetRandomLookTarget()
            ),
            FirstApplicableBehaviour(
                FollowTemptingPlayer(),
                OneRandomBehaviour(
                    SetRandomWalkTarget(),
                    Idle<LivingEntity>().runFor { entity: LivingEntity -> entity.random.nextInt(30, 60) },
                    DrinkPotionBehaviour<LivingEntity>(Potions.REGENERATION).startCondition { self: LivingEntity -> self.health < self.maxHealth * 0.9 }
                )
            )
        )
    }

    override fun getFightTasks(): BrainActivityGroup<LeaderEntity> {
        return BrainActivityGroup.fightTasks(
            StopAttackingIfTargetInvalid { target: LivingEntity -> !target.isAlive || target is Player && target.isCreative },
            FirstApplicableBehaviour(
                DrinkPotionBehaviour<LivingEntity>(Potions.FIRE_RESISTANCE).startCondition { obj: LivingEntity -> obj.isOnFire },
                DrinkPotionBehaviour(Potions.STRONG_STRENGTH),
                DrinkPotionBehaviour<LivingEntity>(Potions.STRONG_REGENERATION)
                    .cooldownFor { 5 * 60 * 20 }
                    .startCondition { self: LivingEntity -> self.health < self.maxHealth * 0.25 },
                AnimatableMeleeAttack(0),
                SetWalkTargetToAttackTarget()
            ),
            HoldItem<LeaderEntity>()
                .withStack { obj: LeaderEntity -> obj.sword }
                .startCondition { self: LeaderEntity -> !self.mainHandItem.`is`(Items.POTION) }
        )
    }

    companion object {
        fun makeAttributes() =
            createMobAttributes().add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ARMOR, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0).add(
                Attributes.MOVEMENT_SPEED, 0.35
            )

        val texture = ResourceLocation(MercenariesMod.MOD_ID, "textures/entity/leader/default.png")
    }
}