package ca.lukegrahamlandry.mercenaries.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class PlayerMob extends PlayerEntity {
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    EntityType<? extends PlayerMob> realType;
    public PlayerMob(EntityType<? extends PlayerMob> type, World level) {
        super(level, BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "Not A Player"));
        this.realType = type;
        this.goalSelector = new GoalSelector(level.getProfilerSupplier());
        this.targetSelector = new GoalSelector(level.getProfilerSupplier());
        if (level != null && !level.isClientSide()) {
            this.registerGoals();
        }
    }

    @Override
    public EntityType getType(){
        return this.realType;
    }

    protected void registerGoals() {

    }

    @Override
    public void tick() {
        super.tick();

        this.level.getProfiler().push("targetSelector");
        this.targetSelector.tick();
        this.level.getProfiler().pop();
        this.level.getProfiler().push("goalSelector");
        this.goalSelector.tick();
        this.level.getProfiler().pop();
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
