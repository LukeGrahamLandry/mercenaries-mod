package ca.lukegrahamlandry.mercenaries.events;

import com.mojang.datafixers.util.Either;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.template.ProcessorLists;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.feature.template.Template;

import java.util.function.Function;
import java.util.function.Supplier;

public class LeaderJigsawPiece extends SingleJigsawPiece{
    protected LeaderJigsawPiece(Either<ResourceLocation, Template> p_i242008_1_, Supplier<StructureProcessorList> p_i242008_2_, JigsawPattern.PlacementBehaviour p_i242008_3_) {
        super(p_i242008_1_, p_i242008_2_, p_i242008_3_);
    }

    public LeaderJigsawPiece(Template p_i242009_1_) {
        super(p_i242009_1_);
    }

    public static Function<JigsawPattern.PlacementBehaviour, SingleJigsawPiece> single(String p_242859_0_) {
        return (p_242850_1_) -> {
            return new LeaderJigsawPiece(Either.left(new ResourceLocation(p_242859_0_)), () -> {
                return ProcessorLists.EMPTY;
            }, p_242850_1_);
        };
    }
}
