package ca.lukegrahamlandry.mercenaries.integration;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class FakePlayerThatRedirects extends PlayerEntity {
    MercenaryEntity owner;
    public FakePlayerThatRedirects(MercenaryEntity owner) {
        super(owner.level, owner.blockPosition(), 0, new GameProfile(UUID.randomUUID(), "Fake"));
        this.owner = owner;
    }

    @Override
    public void tick() {

    }

    @Override
    public World getCommandSenderWorld(){
        return this.owner.getCommandSenderWorld();
    }

    @Override
    public CooldownTracker getCooldowns() {
        return this.owner.getCooldowns();
    }

    @Override
    public void setDeltaMovement(Vector3d p_213317_1_) {
        this.owner.setDeltaMovement(p_213317_1_);
    }

    @Override
    public Vector3d getDeltaMovement() {
        return this.owner.getDeltaMovement();
    }

    @Override
    public void heal(float p_70691_1_) {
        this.owner.heal(p_70691_1_);
    }

    @Override
    public float getHealth() {
        return this.owner == null ? 10 : this.owner.getHealth();
    }

    @Nullable
    @Override
    public ModifiableAttributeInstance getAttribute(Attribute p_110148_1_) {
        return this.owner == null ? super.getAttribute(p_110148_1_) : this.owner.getAttribute(p_110148_1_);
    }

    @Override
    public AttributeModifierManager getAttributes() {
        return this.owner == null ? super.getAttributes() : this.owner.getAttributes();
    }

    @Override
    public boolean addEffect(EffectInstance p_195064_1_) {
        System.out.println("give effect: " + p_195064_1_.getEffect().getRegistryName().toString());
        return this.owner.addEffect(p_195064_1_);
    }

    @Override
    public boolean hasEffect(Effect p_70644_1_) {
        return this.owner.hasEffect(p_70644_1_);
    }

    @Nullable
    @Override
    public EffectInstance getEffect(Effect p_70660_1_) {
        return this.owner.getEffect(p_70660_1_);
    }

    @Override
    public boolean hurt(DamageSource p_70097_1_, float p_70097_2_) {
        return this.owner.hurt(p_70097_1_, p_70097_2_);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public ItemStack getItemInHand(Hand p_184586_1_) {
        return this.owner.getItemInHand(p_184586_1_);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlotType p_184582_1_) {
        return this.owner.getItemBySlot(p_184582_1_);
    }

    @Override
    public boolean hasItemInSlot(EquipmentSlotType p_190630_1_) {
        return this.owner.hasItemInSlot(p_190630_1_);
    }

    @Override
    public void setItemSlot(EquipmentSlotType p_184201_1_, ItemStack p_184201_2_) {
        this.owner.setItemSlot(p_184201_1_, p_184201_2_);
    }

    @Override
    public boolean setSlot(int p_174820_1_, ItemStack p_174820_2_) {
        return this.owner.setSlot(p_174820_1_, p_174820_2_);
    }

    @Override
    public Iterable<ItemStack> getAllSlots() {
        return this.owner.getAllSlots();
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.owner.getArmorSlots();
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.owner.getHandSlots();
    }

    @Override
    public boolean isAlive() {
        return this.owner.isAlive();
    }
}
