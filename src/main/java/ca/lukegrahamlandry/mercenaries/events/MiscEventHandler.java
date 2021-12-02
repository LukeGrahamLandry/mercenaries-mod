package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.SaveMercData;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MercenariesMain.MOD_ID)
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
}
