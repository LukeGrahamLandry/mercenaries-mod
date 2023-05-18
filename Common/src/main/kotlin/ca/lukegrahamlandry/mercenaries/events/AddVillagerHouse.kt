package ca.lukegrahamlandry.mercenaries.events

import ca.lukegrahamlandry.lib.helper.PlatformHelper
import ca.lukegrahamlandry.mercenaries.MercenariesMod
import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.data.worldgen.ProcessorLists
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.util.function.Function

// CREDIT: https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342
object AddVillagerHouse {
    var hasDoneLeaderHouse = false // used by mixins. just here so its present at runtime
    private val EMPTY_PROCESSOR_LIST_KEY =
        ResourceKey.create(Registry.PROCESSOR_LIST_REGISTRY, ResourceLocation("minecraft", "empty"))

    private fun addBuildingToPool(
        templatePoolRegistry: Registry<StructureTemplatePool>,
        processorListRegistry: Registry<StructureProcessorList>,
        poolRL: ResourceLocation,
        nbtPieceRL: String,
        weight: Int
    ) {
        val emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY)
        val pool = templatePoolRegistry[poolRL] ?: return

        // LeaderJigsawPiece is checked by the mixins to make them happen once per village
        val piece: SinglePoolElement = LeaderJigsawPiece.legacy(nbtPieceRL, emptyProcessorList).apply(
            StructureTemplatePool.Projection.RIGID
        )
        for (i in 0 until weight) {
            pool.templates.add(piece)
        }
        val listOfPieceEntries: MutableList<Pair<StructurePoolElement, Int>> = ArrayList(pool.rawTemplates)
        listOfPieceEntries.add(Pair(piece, weight))
        pool.rawTemplates = listOfPieceEntries
    }

    fun addNewVillageBuilding(server: MinecraftServer) {
        if (!MercenariesMod.CONFIG.get().generateLeaderHouses) return

        // the mixins used to make the buildings happen once per village make this value irrelevant
        val weight = 99
        val templatePoolRegistry = server.registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).orElseThrow()
        val processorListRegistry = server.registryAccess().registry(Registry.PROCESSOR_LIST_REGISTRY).orElseThrow()

        val villageTypes = listOf("plains", "snowy", "taiga", "savana", "desert")

        for (name in villageTypes){
            addBuildingToPool(
                templatePoolRegistry,
                processorListRegistry,
                ResourceLocation("minecraft:village/$name/houses"),
      "${MercenariesMod.MOD_ID}:$name",
                weight
            )

            // added to terminators as well in case there is not a large enough house spot for a building to spawn. just gives it another chance
            // this feels shitty and maybe unessisary. should revisit
            addBuildingToPool(
                templatePoolRegistry,
                processorListRegistry,
                ResourceLocation("minecraft:village/$name/terminators"),
                "${MercenariesMod.MOD_ID}:end/$name",
                weight
            )
        }


        if (PlatformHelper.isModLoaded("repurposed_structures")) {
            val repurposedStructuresVillageTypes = mapOf(
                "mountains" to "mountains",
                "badlands" to "badlands",
                "jungle" to "jungle",
                "swamp" to "swamp",
                "birch" to "plains",
                "dark_forest"  to "plains",
                "oak"  to "plains",
                "giant_tree_taiga" to "taiga",

            )
            val rl = "repurposed_structures:village"

            for ((key, value) in repurposedStructuresVillageTypes){
                addBuildingToPool(
                    templatePoolRegistry,
                    processorListRegistry,
                    ResourceLocation("$rl/$key/houses"),
                    "${MercenariesMod.MOD_ID}:$value",
                    weight
                )

                addBuildingToPool(
                    templatePoolRegistry,
                    processorListRegistry,
                    ResourceLocation("minecraft:village/$key/terminators"),
                    "${MercenariesMod.MOD_ID}:end/$value",
                    weight
                )
            }
        }
    }

    class LeaderJigsawPiece protected constructor(
        either: Either<ResourceLocation?, StructureTemplate>?,
        holder: Holder<StructureProcessorList>?,
        projection: StructureTemplatePool.Projection?
    ) : LegacySinglePoolElement(either, holder, projection) {
        constructor(structureTemplate: StructureTemplate) : this(
            Either.right<ResourceLocation?, StructureTemplate>(
                structureTemplate
            ), ProcessorLists.EMPTY, StructureTemplatePool.Projection.RIGID
        )

        companion object {
            fun legacy(
                string: String?,
                holder: Holder<StructureProcessorList>?
            ): Function<StructureTemplatePool.Projection, LegacySinglePoolElement> {
                return Function { projection: StructureTemplatePool.Projection? ->
                    LeaderJigsawPiece(
                        Either.left(
                            ResourceLocation(string)
                        ), holder, projection
                    )
                }
            }
        }
    }
}