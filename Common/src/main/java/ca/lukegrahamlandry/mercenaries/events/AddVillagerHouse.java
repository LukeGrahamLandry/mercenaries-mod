package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.lib.helper.PlatformHelper;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// CREDIT: https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342

public class AddVillagerHouse {
    public static boolean hasDoneLeaderHouse = false; // used by mixins. just here so its present at runtime
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "empty"));

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolRL, String nbtPieceRL, int weight) {
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // LeaderJigsawPiece is checked by the mixins to make them happen once per village
        SinglePoolElement piece = LeaderJigsawPiece.legacy(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    public static void addNewVillageBuilding(MinecraftServer server) {
        if (!MercenariesMod.CONFIG.get().generateLeaderHouses) return;

        // the mixins used to make the buildings happen once per village make this value irrelevant
        int weight = 99;

        Registry<StructureTemplatePool> templatePoolRegistry = server.registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = server.registryAccess().registry(Registry.PROCESSOR_LIST_REGISTRY).orElseThrow();

        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/plains/houses"), MercenariesMod.MOD_ID + ":plains", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/snowy/houses"), MercenariesMod.MOD_ID + ":snowy", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/taiga/houses"), MercenariesMod.MOD_ID + ":taiga", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/savanna/houses"), MercenariesMod.MOD_ID + ":savana", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/desert/houses"), MercenariesMod.MOD_ID + ":desert", weight);

        // added to terminators as well in case there is not a large enough house spot for a building to spawn. just gives it another chance
        // this feels shitty and maybe unessisary. should revisit
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/plains/terminators"), MercenariesMod.MOD_ID + ":end/plains", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/snowy/terminators"), MercenariesMod.MOD_ID + ":end/snowy", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/taiga/terminators"), MercenariesMod.MOD_ID + ":end/taiga", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/savanna/terminators"), MercenariesMod.MOD_ID + ":end/savana", weight);
        addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation("minecraft:village/desert/terminators"), MercenariesMod.MOD_ID + ":end/desert", weight);

        if (PlatformHelper.isModLoaded("repurposed_structures")) {
            String rl = "repurposed_structures:village";

            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/mountains/houses"), MercenariesMod.MOD_ID + ":mountains", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/badlands/houses"), MercenariesMod.MOD_ID + ":badlands", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/jungle/houses"), MercenariesMod.MOD_ID + ":jungle", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/swamp/houses"), MercenariesMod.MOD_ID + ":swamp", weight);

            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/birch/houses"), MercenariesMod.MOD_ID + ":plains", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/dark_forest/houses"), MercenariesMod.MOD_ID + ":plains", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/giant_tree_taiga/houses"), MercenariesMod.MOD_ID + ":taiga", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/oak/houses"), MercenariesMod.MOD_ID + ":plains", weight);


            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/mountains/terminators"), MercenariesMod.MOD_ID + ":mountains", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/badlands/terminators"), MercenariesMod.MOD_ID + ":badlands", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/jungle/terminators"), MercenariesMod.MOD_ID + ":jungle", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/swamp/terminators"), MercenariesMod.MOD_ID + ":swamp", weight);

            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/birch/terminators"), MercenariesMod.MOD_ID + ":plains", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/dark_forest/terminators"), MercenariesMod.MOD_ID + ":plains", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/giant_tree_taiga/terminators"), MercenariesMod.MOD_ID + ":taiga", weight);
            addBuildingToPool(templatePoolRegistry, processorListRegistry, new ResourceLocation(rl + "/oak/terminators"), MercenariesMod.MOD_ID + ":plains", weight);
        }
    }

    public static class LeaderJigsawPiece extends LegacySinglePoolElement {
        protected LeaderJigsawPiece(Either<ResourceLocation, StructureTemplate> either, Holder<StructureProcessorList> holder, StructureTemplatePool.Projection projection) {
            super(either, holder, projection);
        }

        public LeaderJigsawPiece(StructureTemplate structureTemplate) {
            this(Either.right(structureTemplate), ProcessorLists.EMPTY, StructureTemplatePool.Projection.RIGID);
        }

        public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String string, Holder<StructureProcessorList> holder) {
            return (projection) -> new LeaderJigsawPiece(Either.left(new ResourceLocation(string)), holder, projection);
        }
    }
}
