package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.client.BotaniaLightningParticle;
import ca.lukegrahamlandry.mercenaries.init.ItemInit;
import ca.lukegrahamlandry.mercenaries.network.LightningPacket;
import ca.lukegrahamlandry.mercenaries.network.NetworkInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = MercenariesMain.MOD_ID)
public class AttackEventHandler {
    @SubscribeEvent
    public static void doAnvilBootsDamage(LivingFallEvent event){
        LivingEntity player = event.getEntityLiving();
        if (player.getItemStackFromSlot(EquipmentSlotType.FEET).getItem() != ItemInit.ANVIL_BOOTS.get()) return;

        AxisAlignedBB box = new AxisAlignedBB(player.getPosX() + 3, player.getPosY(), player.getPosZ() + 3, player.getPosX() - 3, player.getPosY() + 1, player.getPosZ() - 3);
        for(LivingEntity target : player.world.getEntitiesWithinAABB(LivingEntity.class, box)) {
            // don't hurt your self or things in the air or didnt fall far
            if (target.getUniqueID().toString().equals(player.getUniqueID().toString()) || !target.isOnGround() ||  event.getDistance() < 1) continue;

            target.attackEntityFrom(DamageSource.ANVIL, event.getDistance());

            // knockup
            target.setMotion(target.getMotion().add(0, 0.55D, 0));
            target.isAirBorne = true;
            target.velocityChanged = true;
        }

        event.setDamageMultiplier(0.25F);
    }

    @SubscribeEvent
    public static void makeAnvilBootsFallFast(TickEvent.PlayerTickEvent event){
        PlayerEntity player = event.player;
        if (player.getItemStackFromSlot(EquipmentSlotType.FEET).getItem() != ItemInit.ANVIL_BOOTS.get()) return;
        if (player.getMotion().y >= 0 || player.isOnGround()) return;

        player.setMotion(player.getMotion().add(0, -0.11D, 0));
        player.isAirBorne = true;
        player.velocityChanged = true;
    }


    // limit chains to (n+1) mobs including the first one so it can't recurse infinitely
    static final int MAX_CHAIN_LENGTH = 4;
    static final int JUMP_RANGE = 3;

    @SubscribeEvent
    public static void doLightningThorns(LivingAttackEvent event){
        LivingEntity player = event.getEntityLiving();
        if (player.getEntityWorld().isRemote()) return;
        if (player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() != ItemInit.LIGHTNING_CHEST.get()) return;
        if (!(event.getSource().getTrueSource() instanceof LivingEntity)) return;
        LivingEntity attacker = (LivingEntity) event.getSource().getTrueSource();

        List<LivingEntity> finishedTargets = new ArrayList<>();

        // don't hit yourself with the lightning
        finishedTargets.add(player);

        float damage = event.getAmount() / 2 + 1;

        sendLightningToClients(player, attacker);
        attacker.attackEntityFrom(DamageSource.LIGHTNING_BOLT, damage);
        finishedTargets.add(attacker);

        chainLightningRecurse(attacker, finishedTargets, 0, damage);
    }

    private static void chainLightningRecurse(LivingEntity bouncePoint, List<LivingEntity> finishedTargets, int depth, float damage) {
        AxisAlignedBB box = new AxisAlignedBB(bouncePoint.getPosX() + JUMP_RANGE, bouncePoint.getPosY() + JUMP_RANGE, bouncePoint.getPosZ() + JUMP_RANGE, bouncePoint.getPosX() - JUMP_RANGE, bouncePoint.getPosY() - JUMP_RANGE, bouncePoint.getPosZ() - JUMP_RANGE);
        List<LivingEntity> possibleTargets = bouncePoint.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, box);

        for (LivingEntity target : possibleTargets){
            if (finishedTargets.contains(target)) continue;

            sendLightningToClients(bouncePoint, target);

            target.attackEntityFrom(DamageSource.LIGHTNING_BOLT, damage);
            finishedTargets.add(target);

            if (depth < (MAX_CHAIN_LENGTH-1)) chainLightningRecurse(target, finishedTargets, depth + 1, damage);
        }
    }

    private static void sendLightningToClients(Entity start, Entity end){
        for (PlayerEntity player : start.getEntityWorld().getPlayers()){
            NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new LightningPacket(start.getBoundingBox().getCenter(), end.getBoundingBox().getCenter()));
        }
    }
}
