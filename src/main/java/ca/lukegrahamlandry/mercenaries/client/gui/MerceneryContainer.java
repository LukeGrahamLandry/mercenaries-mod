package ca.lukegrahamlandry.mercenaries.client.gui;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.inventory.container.PlayerContainer.*;

public class MerceneryContainer extends Container {
    private final IInventory mercInventory;
    private final MercenaryEntity merc;


    public static final ResourceLocation EMPTY_ARMOR_SLOT_SWORD = new ResourceLocation("item/sword_outline");
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlotType[] SLOT_IDS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};


    public MerceneryContainer(int id, PlayerInventory playerInventory, IInventory mercInventory, final MercenaryEntity merc) {
        // dont have to register the gui type because I'm sending the packet manually. just in case I want to send extra info later
        super(null, id);
        this.mercInventory = mercInventory;
        this.merc = merc;
        mercInventory.startOpen(playerInventory.player);

        for(int k = 0; k < 4; ++k) {
            final EquipmentSlotType equipmentslottype = SLOT_IDS[k];
            this.addSlot(new Slot(mercInventory, 23 - k, 8, 8 + k * 18) {
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public void set(ItemStack stack) {
                    super.set(stack);
                    merc.setItemSlot(equipmentslottype, stack);
                }

                public boolean mayPlace(ItemStack p_75214_1_) {
                    return p_75214_1_.canEquip(equipmentslottype, merc);
                }

                public boolean mayPickup(PlayerEntity p_82869_1_) {
                    ItemStack itemstack = this.getItem();
                    return !itemstack.isEmpty() && !p_82869_1_.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(p_82869_1_);
                }

                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(PlayerContainer.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentslottype.getIndex()]);
                }
            });
        }

        this.addSlot(new Slot(mercInventory, 1, 80 + 18, 62) {
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

        this.addSlot(new Slot(mercInventory, 0, 80, 62) {
            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                merc.setItemSlot(EquipmentSlotType.MAINHAND, stack);
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