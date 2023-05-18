package ca.lukegrahamlandry.mercenaries.entity

import ca.lukegrahamlandry.mercenaries.MercenariesMod
import ca.lukegrahamlandry.mercenaries.ServerData
import ca.lukegrahamlandry.mercenaries.SyncedData
import ca.lukegrahamlandry.mercenaries.client.gui.MerceneryContainer
import ca.lukegrahamlandry.mercenaries.data.MercServerData
import ca.lukegrahamlandry.mercenaries.data.MercSyncedData
import ca.lukegrahamlandry.mercenaries.entity.behaviour.InvalidateHurtByEntity
import ca.lukegrahamlandry.mercenaries.entity.behaviour.MyFollowEntity
import ca.lukegrahamlandry.mercenaries.network.OpenMercRehireScreenPacket
import ca.lukegrahamlandry.mercenaries.network.OpenMercenaryInventoryPacket
import net.minecraft.core.Registry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleContainer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.monster.RangedAttackMob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ProjectileWeaponItem
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.tslat.smartbrainlib.api.SmartBrainOwner
import net.tslat.smartbrainlib.api.core.BrainActivityGroup
import net.tslat.smartbrainlib.api.core.SmartBrainProvider
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.BowAttack
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRetaliateTarget
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor
import kotlin.math.pow

class MercenaryEntity(entityType: EntityType<MercenaryEntity>, level: Level) :
    PathfinderMob(entityType, level), RangedAttackMob, SmartBrainOwner<MercenaryEntity>, SyncedData<MercSyncedData>, ServerData<MercServerData> {
    val inventory = SimpleContainer(24)
    
    override var syncedData = defaultSyncedData.invoke()
    override val defaultSyncedData 
        get() =  { MercSyncedData() }

    override var serverData = defaultServerData.invoke()
    override val defaultServerData
        get() =  { MercServerData() }

    init {
        if (!this.level.isClientSide()&& !hasCustomName() && MercenariesMod.CONFIG.get().names.isNotEmpty()) {
            val myName =
                MercenariesMod.CONFIG.get().names[getRandom().nextInt(MercenariesMod.CONFIG.get().names.size)]
            this.customName = Component.translatable(myName)
        }
        setPersistenceRequired()
    }

    val reachDist = 4.0 //this.getAttributeValue(ForgeMod.REACH_DISTANCE.get());

    private fun moneyValue(stack: ItemStack) = MercenariesMod.CONFIG.get().itemMoneyValue.getOrDefault(Registry.ITEM.getKey(stack.item), 0)

    private fun foodValue(stack: ItemStack) = if (stack.item.isEdible) stack.item.foodProperties!!.nutrition else 0

    private fun sendOwnerMessage(msg: Component) = owner?.displayClientMessage(msg, true)

    override fun tick() {
        super.tick()
        updateSwingTime()
    }

    override fun customServerAiStep() {
        tickBrain(this)
        this.serverData.shieldCoolDown -= Math.signum(this.serverData.shieldCoolDown.toFloat()).toInt()
        this.serverData.foodTimer++
        if (this.serverData.foodTimer++ > MercenariesMod.CONFIG.get().foodDecayRate) {
            if (this.syncedData.food.consume(inventory, ::foodValue, ::sendOwnerMessage)) leaveOwner()
            this.serverData.foodTimer -= MercenariesMod.CONFIG.get().foodDecayRate
            setDirty()
        }
        this.serverData.moneyTimer++
        if (this.serverData.moneyTimer++ > MercenariesMod.CONFIG.get().moneyDecayRate) {
            if (this.syncedData.money.consume(inventory, ::moneyValue, ::sendOwnerMessage)) leaveOwner()
            this.serverData.moneyTimer -= MercenariesMod.CONFIG.get().moneyDecayRate
            setDirty()
        }
    }

    // called when it runs out of food or money
    private fun leaveOwner() {
        val village = this.serverData.village
        if (village != null) {
            // TODO: translatable
            sendOwnerMessage(Component.literal("One of your mercenaries returned to the village at " + this.serverData.village))
            var world =
                (level as ServerLevel).server.getLevel(ServerLevel.OVERWORLD) // TODO: should not assume the village is in the overworld just in case, RS compat etc
            if (world == null) world = level as ServerLevel // should never happen;
            changeDimension(world)
            teleportToWithTicket(
                village.x.toDouble(),
                village.y.toDouble(),
                village.z.toDouble()
            )
        } else {
            sendOwnerMessage(Component.literal("One of your mercenaries left"))
        }
        removeFromOwnerData()
        moveStance = MovementStance.IDLE
        attackStance = AttackStance.DEFEND
        target = null
        getNavigation().stop()
        owner = null
    }

    private fun removeFromOwnerData() {
        if (!level.isClientSide() && hasOwner()) {
            MercenariesMod.MERC_LIST.get().removeMerc(this.serverData.owner!!, this)
            owner = null
        }
    }

    public override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        if (!level.isClientSide()) {
            val p = player as ServerPlayer
            if (!hasOwner()) {
                OpenMercRehireScreenPacket.create(p, this).sendToClient(p)
            } else if (isOwner(player)) {
                player.closeContainer()
                p.nextContainerCounter()
                OpenMercenaryInventoryPacket(player.containerCounter, id).sendToClient(p)
                player.containerMenu = MerceneryContainer(p.containerCounter, player.getInventory(), inventory, this)
                p.initMenu(player.containerMenu)
            } else {
                player.displayClientMessage(Component.literal("This is not your mercenary"), true)
            }
        }
        return InteractionResult.SUCCESS
    }

    override fun performRangedAttack(target: LivingEntity, power: Float) {
        val ammoStack = getProjectile(getItemInHand(InteractionHand.MAIN_HAND))
        if (ammoStack.isEmpty) return
        ammoStack.shrink(1)
        val arrow = ProjectileUtil.getMobArrow(this, ammoStack, power)
        val xDir = target.x - this.x
        val yDir = target.getY(0.333) - arrow.y
        val zDir = target.z - this.z
        val horizontalDistance = Mth.sqrt((xDir * xDir + zDir * zDir).toFloat()).toDouble()
        arrow.shoot(
            xDir,
            yDir + horizontalDistance * 0.2f,
            zDir,
            1.6f,
            (14 - level.difficulty.id * 4).toFloat()
        ) // might want to change the inaccuracy here
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f))
        level.addFreshEntity(arrow)
    }

    override fun getProjectile(bowStack: ItemStack): ItemStack {
        if (bowStack.item is ProjectileWeaponItem) {
            val predicate = (bowStack.item as ProjectileWeaponItem).supportedHeldProjectiles
            val itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate)
            if (!itemstack.isEmpty) return itemstack
            for (i in 2..19) {
                val checkAmmo = inventory.getItem(i)
                if (predicate.test(checkAmmo)) return checkAmmo
            }
        }
        return ItemStack.EMPTY
    }

    val food: Int
        get() = this.syncedData.food.value
    val money: Int
        get() = this.syncedData.money.value

    fun skipTime(ticks: Long) {
        this.serverData.moneyTimer += ticks.toInt()
        this.serverData.foodTimer += ticks.toInt()
    }

    val isAttackStace
        get() = attackStance == AttackStance.ATTACK
    val isDefendStace
        get() = attackStance == AttackStance.DEFEND
    val isPassiveStace
        get() = attackStance == AttackStance.PASSIVE
    var attackStance
        get() = this.syncedData.attackStance
        set(stance) {
            this.syncedData.attackStance = stance
            setDirty()
            if (isPassiveStace) {
                target = null
            }
        }

    var moveStance
        get() = this.syncedData.moveStance
        set(stance) {
            this.syncedData.moveStance = stance
            setDirty()
        }
    val isFollowMode
        get() = moveStance == MovementStance.FOLLOW
    val isIdleMode
        get() = moveStance == MovementStance.IDLE
    val isStayMode
        get() = moveStance == MovementStance.STAY
    var owner: Player?
        // doing a null check on this is not the same as hasOwner because the owner might be offline
        get() = this.serverData.owner?.let { level.getPlayerByUUID(it) }
        set(player) {
            if (player == null) this.serverData.owner = null else this.serverData.owner = player.uuid
        }

    // the owner might be offline so you still need a null check if you actually get the player
    fun hasOwner() = this.serverData.owner != null

    var camp
        get() = this.syncedData.camp
        set(pos) {
            this.syncedData.camp = pos
            setDirty()
        }
    val reachDistSq
        get() = reachDist.pow(2.0)

    override fun addAdditionalSaveData(tag: CompoundTag) {
        super.addAdditionalSaveData(tag)
        this.saveSyncedData(tag)
        this.saveServerData(tag)

        val listnbt = ListTag()
        for (i in 0 until inventory.containerSize) {
            val itemstack = inventory.getItem(i)
            if (!itemstack.isEmpty) {
                val compoundnbt = CompoundTag()
                compoundnbt.putByte("Slot", i.toByte())
                itemstack.save(compoundnbt)
                listnbt.add(compoundnbt)
            }
        }
        tag.put("Items", listnbt)
    }

    override fun readAdditionalSaveData(tag: CompoundTag) {
        super.readAdditionalSaveData(tag)
        this.loadSyncedData(tag)
        this.loadServerData(tag)

        setDirty()
        val listnbt = tag.getList("Items", 10)
        for (i in listnbt.indices) {
            val compoundnbt = listnbt.getCompound(i)
            val j = compoundnbt.getByte("Slot").toInt() and 255
            if (j < inventory.containerSize) {
                inventory.setItem(j, ItemStack.of(compoundnbt))
            }
        }
    }

    override fun dropCustomDeathLoot(source: DamageSource, looting: Int, wasPlayer: Boolean) {
        if (MercenariesMod.CONFIG.get().dropItemsOnDeath) {
            for (slot in EquipmentSlot.values()) {
                val stack = getItemBySlot(slot)
                if (!stack.isEmpty && !EnchantmentHelper.hasVanishingCurse(stack)) {
                    this.spawnAtLocation(stack)
                    setItemSlot(slot, ItemStack.EMPTY)
                }
            }
            for (i in 2..19) {
                this.spawnAtLocation(inventory.getItem(i))
            }
            inventory.clearContent()
        }
    }

    override fun die(source: DamageSource) {
        super.die(source)
        sendOwnerMessage(Component.literal("Your mercenary died."))
        removeFromOwnerData()
    }

    val texture: ResourceLocation
        get() = this.syncedData.texture

    fun setCampHere() {
        camp = blockPosition()
        this.syncedData.campDimension = level.dimension().location()
    }

    enum class MovementStance {
        FOLLOW, IDLE, STAY
    }

    enum class AttackStance {
        ATTACK, DEFEND, PASSIVE
    }

    override fun setTarget(livingEntity: LivingEntity?) {
        super.setTarget(livingEntity)
        if (livingEntity != null) {
            getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity)
        } else {
            getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET)
        }
    }

    // AI Helpers
    fun isOwner(player: Player): Boolean {
        return player.uuid == this.serverData.owner
    }

    private fun canTarget(target: LivingEntity) =
        target is Enemy
                && (target !is Creeper || hasRanged())
                && isAttackStace
                && (!isStayMode || this.distanceToSqr(target) < reachDistSq || hasRanged())

    private fun hasMelee() =
        this.mainHandItem.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)

    private fun hasRanged() =
        this.mainHandItem.item is ProjectileWeaponItem && !getProjectile(getItemInHand(InteractionHand.MAIN_HAND)).isEmpty

    // AI
    override fun brainProvider() = SmartBrainProvider(this) as Brain.Provider<*>

    override fun getSensors(): List<ExtendedSensor<MercenaryEntity>> {
        return listOf<ExtendedSensor<MercenaryEntity>>(
            NearbyLivingEntitySensor(),
            HurtBySensor()
        )
    }

    override fun getCoreTasks(): BrainActivityGroup<MercenaryEntity> {
        return BrainActivityGroup.coreTasks(
            LookAtTarget(),
            FloatToSurfaceOfFluid(),
            MoveToWalkTarget<MercenaryEntity>()
                .startCondition { self: MercenaryEntity -> self.syncedData.moveStance != MovementStance.STAY },
            FirstApplicableBehaviour(
                StrafeTarget<MercenaryEntity>()
                    .startCondition { obj: MercenaryEntity -> obj.hasRanged() },
                SetWalkTargetToAttackTarget<MercenaryEntity>()
                    .startCondition { obj: MercenaryEntity -> obj.hasMelee() }
            )
        )
    }

    override fun getIdleTasks(): BrainActivityGroup<MercenaryEntity> {
        return BrainActivityGroup.idleTasks(
            FirstApplicableBehaviour(
                SetRetaliateTarget<LivingEntity>().attackablePredicate { e: LivingEntity ->
                    e.isAlive && (e !is Player || !isOwner(
                        e
                    ))
                },
                TargetOrRetaliate<Mob>().attackablePredicate { e: LivingEntity -> e.isAlive && canTarget(e) },
                SetPlayerLookTarget()
            ),
            FirstApplicableBehaviour(
                MyFollowEntity<MercenaryEntity, Player?>()
                    .startFollowingAt {_, _ -> MercenariesMod.CONFIG.get().followDistance.idle.toDouble() }
                    .following { obj: MercenaryEntity -> obj.owner }
                    .stopFollowingWithin { _, _ -> 4.0 }
                    .teleportToTargetAfter { _, _ -> MercenariesMod.CONFIG.get().followDistance.teleport.toDouble() },
                OneRandomBehaviour(
                    SetRandomWalkTarget(),
                    Idle<MercenaryEntity>()
                        .runFor { entity: MercenaryEntity -> entity.getRandom().nextInt(30, 60) }
                )
            )
        )
    }

    override fun getFightTasks(): BrainActivityGroup<MercenaryEntity> {
        return BrainActivityGroup.fightTasks(
            StopAttackingIfTargetInvalid { target-> target is Player && target.isCreative },
            InvalidateHurtByEntity(),
            FirstApplicableBehaviour(
                MyFollowEntity<MercenaryEntity, Player>()
                    .startFollowingAt { _, _ -> MercenariesMod.CONFIG.get().followDistance.fighting.toDouble() }
                    .following { it.owner }
                    .stopFollowingWithin { _, _ -> 4.0 }
                    .teleportToTargetAfter { _, _ -> MercenariesMod.CONFIG.get().followDistance.teleport.toDouble() },
                BowAttack<MercenaryEntity>(20)
                    .startCondition { it.hasRanged() },
                AnimatableMeleeAttack<MercenaryEntity>(0)
                    .startCondition { it.hasMelee() }
            )
        )
    }

    companion object {
        fun makeAttributes(): AttributeSupplier.Builder {
            return createMobAttributes().add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.MOVEMENT_SPEED, 0.33).add(
                Attributes.ATTACK_DAMAGE, 1.0
            ).add(Attributes.ARMOR, 0.0)
        }
    }
}