package ca.lukegrahamlandry.mercenaries.client.container;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MerceneryContainer extends Container {
    private final IInventory mercInventory;
    private final MercenaryEntity merc;

    public MerceneryContainer(int id, PlayerInventory playerInventory, IInventory mercInventory, final MercenaryEntity merc) {
        // dont have to register the container type because I'm sending the packet manually. just incase I want to send extra info later
        super(null, id);
        this.mercInventory = mercInventory;
        this.merc = merc;
        mercInventory.startOpen(playerInventory.player);

        // should do armor slots with a for loop
        this.addSlot(new Slot(mercInventory, 39 - 0, 8, 9) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(EquipmentSlotType.HEAD, merc);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlotType.HEAD, stack);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET);
            }
        });
        this.addSlot(new Slot(mercInventory, 39 - 1, 8, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(EquipmentSlotType.CHEST, merc);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlotType.CHEST, stack);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE);
            }
        });
        this.addSlot(new Slot(mercInventory, 39 - 2, 8, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(EquipmentSlotType.LEGS, merc);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlotType.LEGS, stack);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS);
            }
        });
        this.addSlot(new Slot(mercInventory, 39 - 3, 8, 62) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(EquipmentSlotType.FEET, merc);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlotType.FEET, stack);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS);
            }
        });
        this.addSlot(new Slot(mercInventory, 4, 77, 62) {
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlotType.OFFHAND, stack);
            }

            @Override
            public boolean mayPickup(PlayerEntity p_82869_1_) {
                ItemStack itemstack = this.getItem();
                return (itemstack.isEmpty() || p_82869_1_.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(p_82869_1_);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addSlot(new Slot(mercInventory, 5, 77, 44) {
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlotType.MAINHAND, stack);
            }
        });




        // the player

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return this.merc.isAlive() && this.merc.distanceTo(playerIn) < 8.0F;
    }

    
    // seems to hang forever without this
    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int i = this.mercInventory.getContainerSize();
            if (index < i) {
                if (!this.moveItemStackTo(itemstack1, i, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i <= 2 || !this.moveItemStackTo(itemstack1, 2, i, false)) {
                int j = i + 27;
                int k = j + 9;
                if (index >= j && index < k) {
                    if (!this.moveItemStackTo(itemstack1, i, j, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= i && index < j) {
                    if (!this.moveItemStackTo(itemstack1, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, j, j, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(PlayerEntity playerIn) {
        super.removed(playerIn);
        this.mercInventory.stopOpen(playerIn);
    }
}