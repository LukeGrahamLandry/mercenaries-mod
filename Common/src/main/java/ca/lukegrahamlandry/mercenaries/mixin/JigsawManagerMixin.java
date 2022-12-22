package ca.lukegrahamlandry.mercenaries.mixin;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.events.AddVillagerHouse;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(JigsawPlacement.Placer.class)
public class JigsawManagerMixin {
    @Shadow
    private List<? super PoolElementStructurePiece> pieces;

    @Inject(at=@At("HEAD"), method="tryPlacingChildren")
    private void tryPlacingChildrenStart(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, boolean bl, LevelHeightAccessor levelHeightAccessor, RandomState randomState, CallbackInfo ci) {
        if (!MercenariesMod.CONFIG.get().generateLeaderHouses) return;

        boolean alreadyDidLeader = this.pieces.stream().anyMatch((part) -> part instanceof PoolElementStructurePiece && ((PoolElementStructurePiece) part).getElement() instanceof AddVillagerHouse.LeaderJigsawPiece);
        if (alreadyDidLeader){
            AddVillagerHouse.hasDoneLeaderHouse = true;
        }
        if (poolElementStructurePiece.getElement() instanceof AddVillagerHouse.LeaderJigsawPiece){
            MercenariesMod.LOGGER.debug("placed mercenary leader house at " + poolElementStructurePiece.getPosition());
        }
    }
}
