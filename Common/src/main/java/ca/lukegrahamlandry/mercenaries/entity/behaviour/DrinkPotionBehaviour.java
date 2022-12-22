package ca.lukegrahamlandry.mercenaries.entity.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;
import java.util.function.Function;

/**
 * Causes the entity to drink a potion (like witches do).
 * A potion item will automatically be created and placed in the entity's main hand (deleting whatever was there before).
 * It will not start if the entity already has all the potion's effects.
 * - If you want to keep the old item, you could use .whenStarting to save it and .whenStopping to put it back.
 * - You could have potionFinder select one from its inventory and use .whenStopping to remove it from the inventory.
 */
public class DrinkPotionBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {
    private Function<E, Potion> potionFinder;

    public DrinkPotionBehaviour(Potion potion) {
        this((self) -> potion);
    }

    public DrinkPotionBehaviour(Function<E, Potion> potionFinder) {
        super();
        this.potionFinder = potionFinder;
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return entity.isUsingItem();
    }

    @Override
    protected void start(E self) {
        self.setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), this.potionFinder.apply(self)));
        self.startUsingItem(InteractionHand.MAIN_HAND);
        self.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void stop(E self) {
        self.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    /**
     * If there is no potion to drink, can't start, obviously.
     * If you already have all the effects the potion will give (with equal or greater amplifier), don't bother drinking it.
     */
    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, E self) {
        Potion potion = this.potionFinder.apply(self);
        if (potion == null) return false;

        for (MobEffectInstance fromPotion : potion.getEffects()){
            MobEffectInstance current = self.getEffect(fromPotion.getEffect());
            if (current != null && current.getAmplifier() < fromPotion.getAmplifier()) return true;
        }
        return false;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }
}
