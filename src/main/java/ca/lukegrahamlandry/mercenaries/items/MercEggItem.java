package ca.lukegrahamlandry.mercenaries.items;

import ca.lukegrahamlandry.mercenaries.SaveMercData;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.init.EntityInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;

import javax.annotation.Nullable;
import java.util.Objects;

public class MercEggItem extends SpawnEggItem {
    private final boolean isLeader;

    public MercEggItem(Item.Properties props, boolean isLeader) {
        super(EntityType.PIG, isLeader ? 0xFFFFFF : 0, isLeader ? 0xb7d400 : 0xd68b00, props);
        this.isLeader = isLeader;
    }


    public ActionResultType useOn(ItemUseContext p_195939_1_) {
        World lvt_2_1_ = p_195939_1_.getLevel();
        if (!(lvt_2_1_ instanceof ServerWorld)) {
            return ActionResultType.SUCCESS;
        } else {
            ItemStack lvt_3_1_ = p_195939_1_.getItemInHand();
            BlockPos lvt_4_1_ = p_195939_1_.getClickedPos();
            Direction lvt_5_1_ = p_195939_1_.getClickedFace();
            BlockState lvt_6_1_ = lvt_2_1_.getBlockState(lvt_4_1_);
            if (lvt_6_1_.is(Blocks.SPAWNER)) {
                TileEntity lvt_7_1_ = lvt_2_1_.getBlockEntity(lvt_4_1_);
                if (lvt_7_1_ instanceof MobSpawnerTileEntity) {
                    AbstractSpawner lvt_8_1_ = ((MobSpawnerTileEntity)lvt_7_1_).getSpawner();
                    EntityType<?> lvt_9_1_ = this.getType(lvt_3_1_.getTag());
                    lvt_8_1_.setEntityId(lvt_9_1_);
                    lvt_7_1_.setChanged();
                    lvt_2_1_.sendBlockUpdated(lvt_4_1_, lvt_6_1_, lvt_6_1_, 3);
                    lvt_3_1_.shrink(1);
                    return ActionResultType.CONSUME;
                }
            }

            BlockPos lvt_7_3_;
            if (lvt_6_1_.getCollisionShape(lvt_2_1_, lvt_4_1_).isEmpty()) {
                lvt_7_3_ = lvt_4_1_;
            } else {
                lvt_7_3_ = lvt_4_1_.relative(lvt_5_1_);
            }

            EntityType<?> lvt_8_2_ = this.getType(lvt_3_1_.getTag());
            Entity e = lvt_8_2_.spawn((ServerWorld)lvt_2_1_, lvt_3_1_, p_195939_1_.getPlayer(), lvt_7_3_, SpawnReason.SPAWN_EGG, true, !Objects.equals(lvt_4_1_, lvt_7_3_) && lvt_5_1_ == Direction.UP);

            if (e != null) {
                lvt_3_1_.shrink(1);
                if (e instanceof MercenaryEntity && p_195939_1_.getPlayer() instanceof ServerPlayerEntity){
                    ((MercenaryEntity) e).setOwner(p_195939_1_.getPlayer());
                    SaveMercData.get().addMerc((ServerPlayerEntity) p_195939_1_.getPlayer(), (MercenaryEntity)e);
                }
            }

            return ActionResultType.CONSUME;
        }
    }


    public EntityType<?> getType(@Nullable CompoundNBT p_208076_1_) {
        EntityType type = this.isLeader ? EntityInit.LEADER.get() : EntityInit.MERCENARY.get();
        if (p_208076_1_ != null && p_208076_1_.contains("EntityTag", 10)) {
            CompoundNBT lvt_2_1_ = p_208076_1_.getCompound("EntityTag");
            if (lvt_2_1_.contains("id", 8)) {
                return (EntityType)EntityType.byString(lvt_2_1_.getString("id")).orElse(type);
            }
        }

        return type;
    }
}
