package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = MercenariesMod.MOD_ID)
public class MiscEventHandler {
    public static MinecraftServer server;
    @SubscribeEvent
    public static void saveServer(FMLServerStartedEvent event){
        server = event.getServer();
    }

    @SubscribeEvent
    public static void onSleep(SleepFinishedTimeEvent event){
        long oldTime = event.getWorld().dayTime();
        long newTime = event.getNewTime();

        long difference = 24000 - oldTime +  newTime;
        ((ServerWorld)event.getWorld()).getAllEntities().forEach((entity -> {
            if (entity instanceof MercenaryEntity){
                ((MercenaryEntity)entity).jumpTime(difference);
                ((MercenaryEntity) entity).heal(20);
            }
        }));
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event){
        if (!(event.getEntity() instanceof ServerPlayerEntity)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();

        SaveMercData.get().forLoadedMercBelongingTo(player, (merc) -> {
            if (merc.isDefendStace()) merc.setTarget(attacker);
        });
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event){
        if (!(event.getEntity() instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();

        SaveMercData.get().forLoadedMercBelongingTo(player, (merc) -> {
            BlockPos pos = merc.getCamp();
            if (pos != null){
                merc.setMoveStance(MercenaryEntity.MovementStance.IDLE);
                merc.setAttackStance(MercenaryEntity.AttackStance.DEFEND);

                ServerWorld world = ((ServerWorld) merc.level).getServer().getLevel(merc.campDimension);
                if (world == null) world = (ServerWorld) merc.level; // should never happen;

                merc.setTarget(null);
                merc.getNavigation().stop();

                merc.changeDimension(world);
                merc.teleportToWithTicket(pos.getX(), pos.getY(), pos.getZ());
            }
        });
    }
}
