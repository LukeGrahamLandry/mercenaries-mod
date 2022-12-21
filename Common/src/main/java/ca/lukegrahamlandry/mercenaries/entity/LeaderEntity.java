package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.mercenaries.client.MercTextureList;
import ca.lukegrahamlandry.mercenaries.goals.LeaderMeleeAttackGoal;
import ca.lukegrahamlandry.mercenaries.network.OpenLeaderScreenPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LeaderEntity extends Mob {
    private static final DataParameter<Integer> TEXTURE_TYPE = EntityDataManager.defineId(LeaderEntity.class, DataSerializers.INT);

    public LeaderEntity(EntityType<LeaderEntity> p_i48576_1_, Level p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
        if (!this.level.isClientSide()) this.entityData.set(TEXTURE_TYPE, MercTextureList.getRandom());
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new LeaderMeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MobEntity.class, 5, false, false, this::canTarget));
    }

    private boolean canTarget(LivingEntity target) {
        if (!(target instanceof IMob)) return false;
        if (target instanceof CreeperEntity || target instanceof VillagerEntity) return false;

        double distSq = this.distanceToSqr(target);
        return distSq < 9;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSwingTime();
    }

    @Override
    public boolean hurt(DamageSource source, float p_70097_2_) {
        if (source.getEntity() instanceof LivingEntity && source.getEntity().isAlive() && EntityPredicates.NO_CREATIVE_OR_SPECTATOR.test(source.getEntity())){
            this.setTarget((LivingEntity) source.getEntity());
        }

        return super.hurt(source, p_70097_2_);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TEXTURE_TYPE, 0);
    }

    public static AttributeSupplier.Builder makeAttributes() {
        return IronGolemEntity.createAttributes().add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    protected ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (!this.level.isClientSide()){
            NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new OpenLeaderScreenPacket((ServerPlayerEntity) player, this));
            this.playerInteractions.add(player.getUUID());
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("texture", this.entityData.get(TEXTURE_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(TEXTURE_TYPE, tag.getInt("texture"));
    }

    public ResourceLocation getTexture() {
        return MercTextureList.getLeaderTexture(this.getEntityData().get(TEXTURE_TYPE));
    }

    public final Set<UUID> playerInteractions = new HashSet<>();

    @Override
    public boolean save(CompoundNBT tag) {
        ListNBT interactions = new ListNBT();
        for (UUID id : this.playerInteractions){
            interactions.add(NBTUtil.createUUID(id));
        }
        tag.put("interactions", interactions);
        return super.save(tag);
    }

    @Override
    public void load(CompoundNBT tag) {
        ListNBT interactions = tag.getList("interactions", 11);
        for (int j=0;j<interactions.size();j++){
            this.playerInteractions.add(NBTUtil.loadUUID(interactions.get(j)));
        }

        super.load(tag);
    }
}
