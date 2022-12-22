package ca.lukegrahamlandry.mercenaries.client.gui;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import static net.minecraft.world.inventory.InventoryMenu.*;

public class MerceneryContainer extends AbstractContainerMenu {
    private final SimpleContainer mercInventory;
    private final MercenaryEntity merc;

    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{ EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    public MerceneryContainer(int id, Inventory playerInventory, SimpleContainer mercInventory, final MercenaryEntity merc) {
        // dont have to register the gui type because I'm sending the packet manually. just in case I want to send extra info later
        super(null, id);
        this.mercInventory = mercInventory;
        this.merc = merc;
        mercInventory.startOpen(playerInventory.player);

        for(int k = 0; k < 4; ++k) {
            final EquipmentSlot slot = SLOT_IDS[k];
            this.addSlot(new Slot(mercInventory, 23 - k, 8, 8 + k * 18) {
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public void set(ItemStack stack) {
                    super.set(stack);
                    merc.setItemSlot(slot, stack);
                }

                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getSlot() == slot;
                }

                public boolean mayPickup(Player player) {
                    return (player.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getItem())) && super.mayPickup(player);
                }

                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[slot.getIndex()]);
                }
            });
        }

        this.addSlot(new Slot(mercInventory, 1, 80 + 18, 62) {
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlot.OFFHAND, stack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return (player.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getItem())) && super.mayPickup(player);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(BLOCK_ATLAS, EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addSlot(new Slot(mercInventory, 0, 80, 62) {
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }
        });

        for (int i=2;i<20;i++){
            int x = 80 + ((i % 5) * 18);
            int y = 62 - (Math.floorDiv(i, 5) * 18);
            this.addSlot(new Slot(mercInventory, i, x, y));
        }

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
    public boolean stillValid(Player playerIn) {
        return this.merc.isAlive() && this.merc.distanceTo(playerIn) < 8.0F;
    }

    
    // seems to hang forever without this
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            stack = itemstack1.copy();
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

        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.mercInventory.stopOpen(player);
    }
}