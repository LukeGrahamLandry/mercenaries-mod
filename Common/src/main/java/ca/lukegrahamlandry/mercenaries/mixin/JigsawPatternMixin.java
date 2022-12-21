package ca.lukegrahamlandry.mercenaries.mixin;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.events.AddVillagerHouse;
import ca.lukegrahamlandry.mercenaries.events.LeaderJigsawPiece;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.JigsawStructure;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Mixin(JigsawPattern.class)
public abstract class JigsawPatternMixin {
    @Shadow public abstract ResourceLocation getName();

    @Inject(at=@At("TAIL"), method="getShuffledTemplates(Ljava/util/Random;)Ljava/util/List;", cancellable = true)
    public void getShuffledTemplates(Random p_214943_1_, CallbackInfoReturnable<List<JigsawPiece>> cir) {
        if (!MercConfig.generateLeaderHouses.get()) return;

        if (AddVillagerHouse.hasDoneLeaderHouse){
            List<JigsawPiece> parts = cir.getReturnValue().stream().filter(part -> !(part instanceof LeaderJigsawPiece)).collect(Collectors.toList());
            cir.setReturnValue(parts);
        } else {
            List<JigsawPiece> justLeaders = cir.getReturnValue().stream().filter(part -> part instanceof LeaderJigsawPiece).collect(Collectors.toList());
            if (justLeaders.size() > 0){
                List<JigsawPiece> noLeaders = cir.getReturnValue().stream().filter(part -> !(part instanceof LeaderJigsawPiece)).collect(Collectors.toList());

                if (noLeaders.size() > 0){
                    JigsawPiece temp = noLeaders.get(0);
                    noLeaders.set(0, justLeaders.get(0));
                    noLeaders.add(temp);
                    cir.setReturnValue(noLeaders);
                } else {
                    cir.setReturnValue(justLeaders);
                }
            }
        }
    }
}
