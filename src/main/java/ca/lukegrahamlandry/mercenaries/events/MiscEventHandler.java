package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = MercenariesMain.MOD_ID)
public class MiscEventHandler {
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
        if (!(event.getEntity() instanceof PlayerEntity)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntity();
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
        for (MercenaryEntity merc : MercenaryEntity.getFollowersOf(player)){
            if (merc.isDefendStace()) merc.setTarget(attacker);
        }
    }
}
