package ca.lukegrahamlandry.mercenaries.entity;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.client.MercTextureList;
import ca.lukegrahamlandry.mercenaries.goals.LeaderMeleeAttackGoal;
import ca.lukegrahamlandry.mercenaries.network.OpenLeaderScreenPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LeaderEntity extends Mob {
    public LeaderEntity(EntityType<LeaderEntity> p_i48576_1_, Level p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new LeaderMeleeAttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, this::canTarget));
    }

    private boolean canTarget(LivingEntity target) {
        if (!(target instanceof Enemy)) return false;
        if (target instanceof Creeper || target instanceof Villager) return false;

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
        if (source.getEntity() instanceof LivingEntity && source.getEntity().isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(source.getEntity())){
            this.setTarget((LivingEntity) source.getEntity());
        }

        return super.hurt(source, p_70097_2_);
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
}
