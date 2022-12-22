package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

public class MiscEventHandler {
    public static void onSleep(ServerLevel level, long newTime){
        long oldTime = level.dayTime();

        long difference = 24000 - oldTime +  newTime;
        level.getAllEntities().forEach((entity -> {
            if (entity instanceof MercenaryEntity){
                ((MercenaryEntity)entity).skipTime(difference);
                ((MercenaryEntity) entity).heal(20);
            }
        }));
    }

    public static void onAttack(Enemy target, DamageSource source){
        if (!(target instanceof ServerPlayer)) return;
        if (!(source.getEntity() instanceof LivingEntity)) return;

        MercenariesMod.MERC_LIST.get().forLoadedMercBelongingTo((ServerPlayer) target, (merc) -> {
            if (merc.isDefendStace()) merc.setTarget((LivingEntity) source.getEntity());
        });
    }

    public static void onPlayerDeath(ServerPlayer player){
        MercenariesMod.MERC_LIST.get().forLoadedMercBelongingTo(player, (merc) -> {
            BlockPos pos = merc.getCamp();
            if (pos != null){
                merc.setMoveStance(MercenaryEntity.MovementStance.IDLE);
                merc.setAttackStance(MercenaryEntity.AttackStance.DEFEND);

                ServerLevel level = ((ServerLevel) merc.level).getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, merc.data.synced.campDimension));
                if (level == null) level = (ServerLevel) merc.level; // should never happen;

                merc.setTarget(null);
                merc.getNavigation().stop();

                merc.changeDimension(level);
                merc.teleportToWithTicket(pos.getX(), pos.getY(), pos.getZ());
            }
        });
    }
}
