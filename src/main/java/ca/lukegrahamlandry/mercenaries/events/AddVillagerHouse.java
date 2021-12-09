package ca.lukegrahamlandry.mercenaries.events;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;

// CREDIT: https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342

@Mod.EventBusSubscriber(modid = MercenariesMain.MOD_ID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class AddVillagerHouse {
    private static void addBuildingToPool(MutableRegistry<JigsawPattern> templatePoolRegistry, ResourceLocation poolRL, String nbtPieceRL, int weight) {
        JigsawPattern pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        SingleJigsawPiece piece = SingleJigsawPiece.single(nbtPieceRL).apply(JigsawPattern.PlacementBehaviour.RIGID);

        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<JigsawPiece, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    private static void addToAllVillages(MutableRegistry<JigsawPattern> templatePoolRegistry, String nbtPieceRL, int weight) {
        addBuildingToPool(templatePoolRegistry, new ResourceLocation("minecraft:village/plains/houses"),
                nbtPieceRL, weight);

        addBuildingToPool(templatePoolRegistry, new ResourceLocation("minecraft:village/snowy/houses"),
                nbtPieceRL, weight);

        addBuildingToPool(templatePoolRegistry, new ResourceLocation("minecraft:village/savanna/houses"),
                nbtPieceRL, weight);

        addBuildingToPool(templatePoolRegistry, new ResourceLocation("minecraft:village/taiga/houses"),
                nbtPieceRL, weight);

        addBuildingToPool(templatePoolRegistry, new ResourceLocation("minecraft:village/desert/houses"),
                nbtPieceRL, weight);
    }


    @SubscribeEvent
    public static void addNewVillageBuilding(final FMLServerAboutToStartEvent event) {
        if (MercConfig.leaderStructureWeight.get() == 0) return;

        MutableRegistry<JigsawPattern> templatePoolRegistry = event.getServer().registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).get();
            addToAllVillages(templatePoolRegistry, MercenariesMain.MOD_ID + ":merc", MercConfig.leaderStructureWeight.get());
    }

}
