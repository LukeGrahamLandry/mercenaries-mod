package ca.lukegrahamlandry.mercenaries.mixin;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.events.AddVillagerHouse;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(StructureTemplatePool.class)
public abstract class JigsawPatternMixin {
    @Inject(at=@At("TAIL"), method="getShuffledTemplates", cancellable = true)
    public void getShuffledTemplates(RandomSource randomSource, CallbackInfoReturnable<List<StructurePoolElement>> cir) {
        if (!MercenariesMod.CONFIG.get().generateLeaderHouses) return;

        if (AddVillagerHouse.hasDoneLeaderHouse){
            List<StructurePoolElement> parts = cir.getReturnValue().stream().filter(part -> !(part instanceof AddVillagerHouse.LeaderJigsawPiece)).collect(Collectors.toList());
            cir.setReturnValue(parts);
        } else {
            List<StructurePoolElement> justLeaders = cir.getReturnValue().stream().filter(part -> part instanceof AddVillagerHouse.LeaderJigsawPiece).collect(Collectors.toList());
            if (justLeaders.size() > 0){
                List<StructurePoolElement> noLeaders = cir.getReturnValue().stream().filter(part -> !(part instanceof AddVillagerHouse.LeaderJigsawPiece)).collect(Collectors.toList());

                if (noLeaders.size() > 0){
                    StructurePoolElement temp = noLeaders.get(0);
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
