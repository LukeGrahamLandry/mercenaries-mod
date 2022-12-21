package ca.lukegrahamlandry.mercenaries.wrapped;

import ca.lukegrahamlandry.lib.config.Comment;
import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MercSyncedConfig {
    @Comment("How many ticks for a mercenary to consume 1 unit of food (a full bar is 20 units). Use a value of 2147483647 to disable food requirement")
    public int foodDecayRate = 24000 / 20 * 4;

    @Comment("How many ticks for a mercenary to consume 1 unit of money (a full bar is 20 units). Use a value of 2147483647 to disable payment requirement")
    public int moneyDecayRate = 24000 / 20 * 4;

    @Comment("How many mercenaries you can have hired at once")
    public int maxMercs = 3;

    @Comment("how many emeralds should your first mercenary cost. this will be scaled up for subsequent hires")
    public int basePrice = 20;

    @Comment("how quickly should the price of mercenaries scale up. base price will be mulitplied by currentlyOwned^priceScaleAmount")
    public double priceScaleAmount = 2;

    @Comment("should leader houses generate in villages?")
    public boolean generateLeaderHouses;

    @Comment("which item should be used to pay for new mercenaries")
    public ResourceLocation hirePaymentItem = new ResourceLocation("minecraft", "emerald");

    @Comment("names to use for mercenaries. these can be translation keys but they don't have to be. one of these will be randomly chosen for each new mercenary")
    public List<String> names = Arrays.asList("Sir Fight-a-lot", "Fighter McKillerson", "Murder McSlayer", "Sir Stab-a-lot");

    @Comment("how much should it cost to rehire a mercenary that has abandoned you. Use -1 to make them just leave forever")
    public int rehirePrice = 15;

    @Comment("when a mercenary is killed, should its inventory drop")
    boolean dropItemsOnDeath = true;

    @Comment("How many money units is each item worth when placed in a mercenary's inventory")
    public Map<ResourceLocation, Integer> itemMoneyValue = new HashMap<>();

    public FollowDistanceGroup followDistance = new FollowDistanceGroup();

    public MercSyncedConfig(){
        itemMoneyValue.put(new ResourceLocation("minecraft", "emerald"), 4);
        itemMoneyValue.put(new ResourceLocation("minecraft", "diamond"), 19);
        itemMoneyValue.put(new ResourceLocation("minecraft", "emerald"), 1);
    }

    public static class FollowDistanceGroup {
        @Comment("When there are no targets, how many blocks should you walk away before your mercenaries start following")
        public int idle = 8;

        @Comment("When there are targets, how many blocks should you walk away before your mercenaries start following")
        public int fighting = 16;

        @Comment("how many blocks should you walk away before your mercenaries teleport to you")
        public int teleport = 24;
    }

    public int caclualteCurrentPrice(Player player) {
        int owned = MercenariesMod.MERC_LIST.get().getMercs(player).size();
        if (owned >= maxMercs) return Integer.MAX_VALUE;
        return (int) (basePrice * Math.pow(priceScaleAmount, owned));
    }
}
