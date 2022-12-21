package ca.lukegrahamlandry.mercenaries.integration;

import ca.lukegrahamlandry.mercenaries.MercConfig;
import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import com.infamous.dungeons_gear.items.artifacts.ArtifactItem;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class UseArtifactGoal extends Goal {
    private final MercenaryEntity merc;

    int index;
    int time;

    public UseArtifactGoal(MercenaryEntity merc){
        this.merc = merc;
    }

    @Override
    public boolean canUse() {
        if (this.merc.getArtifactCooldown() > 0 || this.merc.getTarget() == null) return false;

        List<Integer> validArtifacts = new ArrayList<>();
        for (int i=0;i<20;i++){
            Item check = this.merc.inventory.getItem(i).getItem();
            if (check instanceof ArtifactItem){
                System.out.println("found artifact " + check);
                CooldownTracker cooldowns = this.merc.getCooldowns();
                if (!cooldowns.isOnCooldown(check)){
                    validArtifacts.add(i);
                }

            }
        }

        if (validArtifacts.size() > 0){
            this.index = merc.getRandom().nextInt(validArtifacts.size());
            return true;
        }

        return false;
    }

    public void start(){
        System.out.println("start use artifact");
        this.time = 0;
        this.merc.onStartUseArtifact();

        ItemStack artifact = merc.inventory.getItem(this.index);
        ItemStack oldMainHand = merc.inventory.getItem(0);
        this.merc.setItemInHand(Hand.MAIN_HAND, artifact);
        this.merc.inventory.setItem(0, artifact);
        this.merc.inventory.setItem(this.index, oldMainHand);
    }

    public void stop(){
        this.merc.onEndUseArtifact();

        ItemStack oldMainHand = merc.inventory.getItem(this.index);
        ItemStack artifact = merc.inventory.getItem(0);
        this.merc.inventory.setItem(0, oldMainHand);
        this.merc.setItemInHand(Hand.MAIN_HAND, oldMainHand);
        this.merc.inventory.setItem(this.index, artifact);
    }

    @Override
    public boolean canContinueToUse() {
        return this.time < MercConfig.getTimeToUseArtifact() && this.merc.getTarget() != null;
    }

    @Override
    public void tick() {
        this.time++;

        System.out.println(this.time);
        this.merc.lookAt(EntityAnchorArgument.Type.EYES, this.merc.getTarget().getEyePosition(1));

        if (this.time == (MercConfig.getTimeToUseArtifact() / 2)){
            System.out.println("really artifact");

            Item check = merc.inventory.getItem(0).getItem();
            ItemUseContext context = new ItemUseContext(merc.getFakePlayer(), Hand.MAIN_HAND, getPlayerPOVHitResult(merc.level, this.merc, RayTraceContext.FluidMode.SOURCE_ONLY));
            if (check instanceof ArtifactItem){
                System.out.println("valid artifact");
                this.merc.onActuallyUseArtifact();
                ((ArtifactItem) check).procArtifact(context);
                ArtifactItem.putArtifactOnCooldown(merc.getFakePlayer(), check);
            }
        }
    }

    protected static BlockRayTraceResult getPlayerPOVHitResult(World p_219968_0_, MercenaryEntity p_219968_1_, RayTraceContext.FluidMode p_219968_2_) {
        float f = p_219968_1_.xRot;
        float f1 = p_219968_1_.yRot;
        Vector3d vector3d = p_219968_1_.getEyePosition(1.0F);
        float f2 = MathHelper.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * ((float)Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = 5; // p_219968_1_.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();;
        Vector3d vector3d1 = vector3d.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
        return p_219968_0_.clip(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.OUTLINE, p_219968_2_, p_219968_1_));
    }

}
