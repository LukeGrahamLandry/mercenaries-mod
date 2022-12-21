package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.lib.registry.RegistryWrapper;
import ca.lukegrahamlandry.mercenaries.client.render.LeaderRenderer;
import ca.lukegrahamlandry.mercenaries.client.render.MercenaryRenderer;
import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class MercRegistry {
    private static RegistryWrapper<EntityType<?>> ENTITIES = RegistryWrapper.create(Registry.ENTITY_TYPE, MercenariesMod.MOD_ID);

    public static Supplier<EntityType<MercenaryEntity>> MERCENARY = ENTITIES.register("mercenary",
            EntityType.Builder.of(MercenaryEntity::new, MobCategory.CREATURE).sized(0.6F, 1.6F))
            .withAttributes(MercenaryEntity::makeAttributes)
            .withRenderer(() -> MercenaryRenderer::new)
            .withSpawnEgg(0, 0xd68b00);  // TODO: it needs to bind you to the merc

    public static Supplier<EntityType<LeaderEntity>> MERCENARY_LEADER = ENTITIES.register("leader",
            EntityType.Builder.of(LeaderEntity::new, MobCategory.CREATURE).sized(0.6F, 1.6F))
            .withAttributes(LeaderEntity::makeAttributes)
            .withRenderer(() -> LeaderRenderer::new)
            .withSpawnEgg(0xFFFFFF, 0xb7d400);

    public static void init(){}
}
