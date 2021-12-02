package ca.lukegrahamlandry.mercenaries;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class MercConfig {
    public static final ForgeConfigSpec server_config;

    public static void init(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, server_config);
        CommentedFileConfig file = CommentedFileConfig.builder(new File(FMLPaths.CONFIGDIR.get().resolve(MercenariesMain.MOD_ID + ".toml").toString())).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        server_config.setConfig(file);
    }

    public static final ForgeConfigSpec.IntValue foodDecayRate;
    public static final ForgeConfigSpec.IntValue getMoneyDecayRate;
    public static final ForgeConfigSpec.IntValue maxMercs;
    public static final ForgeConfigSpec.IntValue emeraldValue;
    public static final ForgeConfigSpec.IntValue diamondValue;
    public static final ForgeConfigSpec.IntValue idleFollowDistance;
    public static final ForgeConfigSpec.IntValue fightingFollowDistance;
    public static final ForgeConfigSpec.IntValue teleportDistance;
    public static final ForgeConfigSpec.IntValue basePrice;
    public static final ForgeConfigSpec.DoubleValue priceScaleFactor;
    public static final ForgeConfigSpec.IntValue artifactCooldown;
    public static final ForgeConfigSpec.IntValue artifactUseTime;

    static {
        final ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();

        serverBuilder.comment("Mercenaries server side settings")
                .push("server");

        foodDecayRate = serverBuilder
                .comment("How many ticks for a mercenary to consume 1 unit of food (a full bar is 20 units)")
                .defineInRange("foodDecayRate", 24000 / 20 * 4, 1, Integer.MAX_VALUE);

        getMoneyDecayRate = serverBuilder
                .comment("How many ticks for a mercenary to consume 1 unit of money (a full bar is 20 units)")
                .defineInRange("getMoneyDecayRate", 24000 / 20 * 4, 1, Integer.MAX_VALUE);

        maxMercs = serverBuilder
                .comment("How many mercenaries you can have hired at once")
                .defineInRange("maxMercs", 3, 0, Integer.MAX_VALUE);

        emeraldValue = serverBuilder
                .comment("How many money units is an emerald worth")
                .defineInRange("emeraldValue", 4, 0, Integer.MAX_VALUE);

        diamondValue = serverBuilder
                .comment("How many money units is a diamond worth")
                .defineInRange("diamondValue", 4, 0, Integer.MAX_VALUE);

        idleFollowDistance = serverBuilder
                .comment("When there are no targets, how many blocks should you walk away before your mercenaries start following")
                .defineInRange("idleFollowDistance", 10, 1, Integer.MAX_VALUE);

        fightingFollowDistance = serverBuilder
                .comment("When there are targets, how many blocks should you walk away before your mercenaries start following")
                .defineInRange("fightingFollowDistance", 20, 1, Integer.MAX_VALUE);

        teleportDistance = serverBuilder
                .comment("how many blocks should you walk away before your mercenaries teleport to you")
                .defineInRange("fightingFollowDistance", 30, 1, Integer.MAX_VALUE);

        basePrice = serverBuilder
                .comment("how many emeralds should your first mercenary cost. this will be scaled up for subsequent hires")
                .defineInRange("basePrice", 20, 1, Integer.MAX_VALUE);

        priceScaleFactor = serverBuilder
                .comment("how quickly should the price of mercenaries scale up. base price will be mulitplied by currentlyOwned^priceScaleFactor")
                .defineInRange("priceScaleFactor", 2.0D, 1, Integer.MAX_VALUE);

        artifactCooldown = serverBuilder
                .comment("UNUSED. ticks between a mercenary using any artifacts (in addition to their normal cool down)")
                .defineInRange("artifactCooldown", 100, 1, Integer.MAX_VALUE);

        artifactUseTime = serverBuilder
                .comment("UNUSED. ticks it takes the mercenary to use an artifact")
                .defineInRange("artifactUseTime", 30, 1, Integer.MAX_VALUE);

        server_config = serverBuilder.build();
    }




    public static int getFoodDecayRate(){
        return foodDecayRate.get();
    }

    public static int getMoneyDecayRate(){
        return getMoneyDecayRate.get();
    }

    public static int maxMercs(){
        return maxMercs.get();
    }

    public static Item buyMercItem(){
        return Items.EMERALD;
    }

    public static int getMoneyValue(Item item){
        if (item == Items.EMERALD) return emeraldValue.get();
        if (item == Items.DIAMOND) return diamondValue.get();

        return 0;
    }

    public static boolean artifactsInstalled(){
        return isModLoaded("dungeons_gear");
    }

    private static boolean isModLoaded(String modID) {
        return ModList.get() != null && ModList.get().getModContainerById(modID).isPresent();
    }

    public static int getSharedArtifactCooldown() {
        return artifactCooldown.get();
    }

    public static int getTimeToUseArtifact() {
        return artifactUseTime.get();
    }

    public static int caclualteCurrentPrice(ServerPlayerEntity player) {
        int owned = SaveMercData.get().getMercs(player).size();
        if (owned >= maxMercs()) return Integer.MAX_VALUE;
        return (int) (basePrice.get() * Math.pow(priceScaleFactor.get(), owned));
    }

    public static double getIdleFollowDistance() {
        return Math.pow(idleFollowDistance.get(), 2);
    }

    public static double getFightingFollowDistance() {
        return Math.pow(fightingFollowDistance.get(), 2);
    }

    public static double getTeleportDistance() {
        return Math.pow(teleportDistance.get(), 2);
    }
}
