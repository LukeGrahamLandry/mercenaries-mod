package ca.lukegrahamlandry.mercenaries.entity.behaviour;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import org.apache.commons.lang3.function.ToBooleanBiFunction;

import java.util.List;

// Based on https://github.com/Tslat/SmartBrainLib/blob/1.19/Common/src/main/java/net/tslat/smartbrainlib/api/core/behaviour/custom/target/InvalidateAttackTarget.java
// This file is distributed under the MPL 2.0

public class InvalidateHurtByEntity<E extends Mob> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT));

    protected ToBooleanBiFunction<E, LivingEntity> customPredicate = (entity, target) -> target instanceof Player pl && (pl.isCreative() || pl.isSpectator());

    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    /**
     * Sets a custom predicate to invalidate the HURT_BY_ENTITY if none of the previous checks invalidate it first.<br>
     * Overrides the default player gamemode check
     */
    public InvalidateHurtByEntity<E> invalidateIf(ToBooleanBiFunction<E, LivingEntity> predicate) {
        this.customPredicate = predicate;

        return this;
    }

    @Override
    protected void start(E entity) {
        LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).orElse(null);

        if (target == null)
            return;

        if (isTargetInvalid(entity, target) || this.customPredicate.applyAsBoolean(entity, target)) {
            BrainUtils.clearMemory(entity, MemoryModuleType.HURT_BY_ENTITY);
        }
    }

    protected boolean isTargetInvalid(E entity, LivingEntity target) {
        if (entity.level != target.level)
            return true;

        return target.isDeadOrDying() || target.isRemoved();
    }
}