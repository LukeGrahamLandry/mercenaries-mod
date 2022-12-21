package ca.lukegrahamlandry.mercenaries.wrapped;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class MercEntityData {
    public MercServerData server = new MercServerData();
    public MercSyncedData synced = new MercSyncedData();

    public static class MercServerData {
        public int foodTimer = 0;
        public int moneyTimer = 0;
        public UUID owner = null;
        public BlockPos village = null;

        // > 0 when cant use, < 0 when using
        public int shieldCoolDown = 0;
    }

    public static class MercSyncedData {
        public ResourceLocation texture = new ResourceLocation(MercenariesMod.MOD_ID,  "textures/entity/merc/default_1.png");

        public InvResource food = new InvResource("food");
        public InvResource money = new InvResource("money");

        public MercenaryEntity.AttackStance attackStance = MercenaryEntity.AttackStance.ATTACK;
        public MercenaryEntity.MovementStance moveStance = MercenaryEntity.MovementStance.FOLLOW;

        public BlockPos camp = null;
        public ResourceLocation campDimension = Level.OVERWORLD.location();


        public static class InvResource {
            static int total = 20;
            private String name;
            public int value = total;

            public InvResource(String name) {
                this.name = name;
            }

            public boolean consume(SimpleContainer inventory, Function<ItemStack, Integer> points, Consumer<Component> message){
                this.value--;

                int needed = total - value;
                for (int i=0; i<(inventory.getContainerSize() - 4); i++){
                    ItemStack stack = inventory.getItem(i);
                    if (points.apply(stack) > 0 && points.apply(stack) <= needed){
                        stack.shrink(1);
                        this.value += points.apply(stack);
                        break;
                    }
                }

                if (this.value <= 8) {
                    message.accept(Component.translatable(MercenariesMod.MOD_ID + ".alert.1." + this.name));
                } else if (this.value <= 4) {
                    message.accept(Component.translatable(MercenariesMod.MOD_ID + ".alert.2." + this.name));
                }

                return this.value <= 0;
            }
        }
    }
}
