package ca.lukegrahamlandry.mercenaries.mixin;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.events.AddVillagerHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawPlacement.class)
public class JigsawStructureMixin {
    @Inject(at=@At("HEAD"), method="addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;I)Ljava/util/Optional;")
    private static void generatePiecesSTART(Structure.GenerationContext generationContext, Holder<StructureTemplatePool> holder, Optional<ResourceLocation> optional, int i, BlockPos blockPos, boolean bl, Optional<Heightmap.Types> optional2, int j, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (!MercenariesMod.CONFIG.get().generateLeaderHouses) return;

        AddVillagerHouse.hasDoneLeaderHouse = false;
    }
}
