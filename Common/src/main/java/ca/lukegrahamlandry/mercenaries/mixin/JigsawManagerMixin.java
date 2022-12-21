package ca.lukegrahamlandry.mercenaries.mixin;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.events.AddVillagerHouse;
import ca.lukegrahamlandry.mercenaries.events.LeaderJigsawPiece;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(JigsawManager.Assembler.class)
public class JigsawManagerMixin {
    @Shadow
    private List<? super AbstractVillagePiece> pieces;

    @Inject(at=@At("HEAD"), method="tryPlacingChildren(Lnet/minecraft/world/gen/feature/structure/AbstractVillagePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IIZ)V")
    private void tryPlacingChildrenStart(AbstractVillagePiece piece, MutableObject<VoxelShape> i1, int i2, int k2, boolean l2, CallbackInfo ci) {
        if (!MercConfig.generateLeaderHouses.get()) return;

        boolean alreadyDidLeader = this.pieces.stream().anyMatch((part) -> part instanceof AbstractVillagePiece && ((AbstractVillagePiece) part).getElement() instanceof LeaderJigsawPiece);
        if (alreadyDidLeader){
            AddVillagerHouse.hasDoneLeaderHouse = true;
        }
        if (piece.getElement() instanceof LeaderJigsawPiece){
            System.out.println("placed mercenary leader house at " + piece.getPosition());
        }
    }
}
