package ca.lukegrahamlandry.mercenaries.integration;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import com.telepathicgrunt.repurposedstructures.world.structures.pieces.StructurePiecesBehavior;
import net.minecraft.util.ResourceLocation;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepurposedStructuresCompat {
    private static void add(ResourceLocation targetStructure, String piece){
        ResourceLocation pieceRL = new ResourceLocation(piece);

        if (StructurePiecesBehavior.REQUIRED_PIECES_COUNT.containsKey(targetStructure)){
            Map<ResourceLocation, StructurePiecesBehavior.RequiredPieceNeeds> requiredPieces = StructurePiecesBehavior.REQUIRED_PIECES_COUNT.get(targetStructure);
            requiredPieces.put(pieceRL, new StructurePiecesBehavior.RequiredPieceNeeds(1, 0));
        } else {
            StructurePiecesBehavior.REQUIRED_PIECES_COUNT.put(targetStructure, Stream.of(new AbstractMap.SimpleImmutableEntry<>(pieceRL, new StructurePiecesBehavior.RequiredPieceNeeds(1, 4))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }

    public static void forceHousesInRSVillages(){
        System.out.println("call");

        String rl = "repurposed_structures:village_";

        add(new ResourceLocation(rl + "mountains"), MercenariesMain.MOD_ID + ":mountains");
        add(new ResourceLocation(rl + "badlands"), MercenariesMain.MOD_ID + ":badlands");
        add(new ResourceLocation(rl + "jungle"), MercenariesMain.MOD_ID + ":jungle");
        add(new ResourceLocation(rl + "swamp"), MercenariesMain.MOD_ID + ":swamp");

        // i dont have unique buildings for these. just reuse them
        add(new ResourceLocation(rl + "birch"), MercenariesMain.MOD_ID + ":plains");
        add(new ResourceLocation(rl + "dark_forest"), MercenariesMain.MOD_ID + ":plains");
        add(new ResourceLocation(rl + "giant_tree_taiga"), MercenariesMain.MOD_ID + ":taiga");
        add(new ResourceLocation(rl + "oak"), MercenariesMain.MOD_ID + ":plains");

    }
}
