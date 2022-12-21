package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.behaviour.DrinkPotionBehaviour;
import ca.lukegrahamlandry.mercenaries.network.OpenLeaderScreenPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRetaliateTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;

import java.util.HashMap;
import java.util.List;

public class LeaderEntity extends PathfinderMob implements SmartBrainOwner<LeaderEntity> {
    public LeaderEntity(EntityType<LeaderEntity> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    private boolean canTarget(LivingEntity target) {
        if (!(target instanceof Enemy)) return false;
        if (target instanceof Creeper || target instanceof Villager) return false;

        double distSq = this.distanceToSqr(target);
        return distSq < 9;
    }

    boolean inCombat = false;

    @Override
    public void tick() {
        super.tick();
        this.updateSwingTime();

        if (this.inCombat){
            if (this.getTarget() == null || !this.getTarget().isAlive()){
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 2));
                this.setTarget(null);
                this.inCombat = false;
            }
        } else {
            if (this.getTarget() != null) this.inCombat = true;
        }
    }

    public ItemStack getSword(){
        ItemStack sword = new ItemStack(Items.STONE_SWORD);
        HashMap<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantments.SHARPNESS, 5);
        EnchantmentHelper.setEnchantments(enchants, sword);
        return sword;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity && source.getEntity().isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(source.getEntity())){
            this.setTarget((LivingEntity) source.getEntity());
            source.getEntity().hurt(DamageSource.thorns(this), Math.floorDiv((int) Math.floor(damage), 5) + 1);
        }

        return super.hurt(source, damage);
    }

    public static AttributeSupplier.Builder makeAttributes() {
        return IronGolem.createAttributes().add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level.isClientSide()){
            new OpenLeaderScreenPacket(player, this).sendToClient((ServerPlayer) player);
        }
        return InteractionResult.SUCCESS;
    }

    static final ResourceLocation TEXTURE = new ResourceLocation(MercenariesMod.MOD_ID, "textures/entity/leader/default.png");
    public ResourceLocation getTexture() {
        return TEXTURE;
    }



    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public List<ExtendedSensor<LeaderEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyLivingEntitySensor<>(),
                new HurtBySensor<>()
        );
    }

    @Override
    public BrainActivityGroup<LeaderEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new FloatToSurfaceOfFluid<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public BrainActivityGroup<LeaderEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<LeaderEntity>(
                        new SetRetaliateTarget<>(),
                        new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>()
                ),
                new OneRandomBehaviour<LeaderEntity>(
                        new SetRandomWalkTarget<>(),
                        new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60)),
                        new DrinkPotionBehaviour<>(Potions.STRONG_REGENERATION).startCondition((self) -> self.getHealth() < self.getHealth() * 0.9)
                )
        );
    }

    @Override
    public BrainActivityGroup<LeaderEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new StopAttackingIfTargetInvalid<>(target -> !target.isAlive() || target instanceof Player && ((Player)target).isCreative()),
                new FirstApplicableBehaviour<LeaderEntity>(
                        new DrinkPotionBehaviour<>(Potions.FIRE_RESISTANCE).startCondition(Entity::isOnFire),
                        new AnimatableMeleeAttack<LeaderEntity>(0).whenStarting((self) -> self.setItemInHand(InteractionHand.MAIN_HAND, self.getSword())),
                        new SetWalkTargetToAttackTarget<>()
                )
        );
    }
}
