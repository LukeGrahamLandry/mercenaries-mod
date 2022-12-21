package ca.lukegrahamlandry.mercenaries.goals;

import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.HashMap;


public class LeaderMeleeAttackGoal extends MeleeAttackGoal {
    LeaderEntity owner;
    public LeaderMeleeAttackGoal(LeaderEntity mob, double speedMultiplier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedMultiplier, followingTargetEvenIfNotSeen);
        this.owner = mob;
    }

    static final ItemStack sword = new ItemStack(Items.IRON_SWORD);
    static {
        HashMap<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantments.SHARPNESS, 5);
        EnchantmentHelper.setEnchantments(enchants, sword);
    }

    @Override
    public void start() {
        super.start();
        this.owner.setItemInHand(Hand.MAIN_HAND, sword.copy());
    }

    @Override
    public void stop() {
        super.stop();
        if (this.mob.getTarget() == null || !this.mob.getTarget().isAlive()){
            this.owner.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}

