package ca.lukegrahamlandry.mercenaries.init;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY = DeferredRegister.create(ForgeRegistries.ENTITIES, MercenariesMain.MOD_ID);

    public static final RegistryObject<EntityType<MercenaryEntity>> MERCENARY = ENTITY.register("mercenary", () -> EntityType.Builder.of(MercenaryEntity::new, EntityClassification.MISC).sized(1.6F, 0.6F).build("mercenary"));
    public static final RegistryObject<EntityType<LeaderEntity>> LEADER = ENTITY.register("leader", () -> EntityType.Builder.of(LeaderEntity::new, EntityClassification.MISC).sized(1.6F, 0.6F).build("leader"));
}