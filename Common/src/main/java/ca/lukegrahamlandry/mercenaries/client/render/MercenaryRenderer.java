package ca.lukegrahamlandry.mercenaries.client.render;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;

public class MercenaryRenderer extends LivingEntityRenderer<MercenaryEntity, PlayerModel<MercenaryEntity>> {
    public MercenaryRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ArrowLayer<>(context, this));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
    }

    public void render(MercenaryEntity p_225623_1_, float p_225623_2_, float p_225623_3_, PoseStack p_225623_4_, MultiBufferSource p_225623_5_, int p_225623_6_) {
        this.setModelProperties(p_225623_1_);
        super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    public Vec3 getRenderOffset(MercenaryEntity p_225627_1_, float p_225627_2_) {
        return p_225627_1_.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(p_225627_1_, p_225627_2_);
    }

    private void setModelProperties(MercenaryEntity p_177137_1_) {
        PlayerModel<MercenaryEntity> playermodel = this.getModel();
        playermodel.setAllVisible(true);
        playermodel.crouching = p_177137_1_.isCrouching();
        HumanoidModel.ArmPose bipedmodel$armpose = getArmPose(p_177137_1_, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose bipedmodel$armpose1 = getArmPose(p_177137_1_, InteractionHand.OFF_HAND);
        if (bipedmodel$armpose.isTwoHanded()) {
            bipedmodel$armpose1 = p_177137_1_.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        if (p_177137_1_.getMainArm() == HumanoidArm.RIGHT) {
            playermodel.rightArmPose = bipedmodel$armpose;
            playermodel.leftArmPose = bipedmodel$armpose1;
        } else {
            playermodel.rightArmPose = bipedmodel$armpose1;
            playermodel.leftArmPose = bipedmodel$armpose;
        }
    }

    private static HumanoidModel.ArmPose getArmPose(MercenaryEntity p_241741_0_, InteractionHand p_241741_1_) {
        ItemStack itemstack = p_241741_0_.getItemInHand(p_241741_1_);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (p_241741_0_.getUsedItemHand() == p_241741_1_ && p_241741_0_.getUseItemRemainingTicks() > 0) {
                UseAnim useaction = itemstack.getUseAnimation();
                if (useaction == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useaction == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useaction == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (useaction == UseAnim.CROSSBOW && p_241741_1_ == p_241741_0_.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!p_241741_0_.swinging && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    public ResourceLocation getTextureLocation(MercenaryEntity merc) {
        return merc.getTexture();
    }

    protected void scale(MercenaryEntity p_225620_1_, PoseStack p_225620_2_, float p_225620_3_) {
        float f = 0.9375F;
        p_225620_2_.scale(0.9375F, 0.9375F, 0.9375F);
    }


    public void renderRightHand(PoseStack p_229144_1_, MultiBufferSource p_229144_2_, int p_229144_3_, MercenaryEntity p_229144_4_) {
        this.renderHand(p_229144_1_, p_229144_2_, p_229144_3_, p_229144_4_, (this.model).rightArm, (this.model).rightSleeve);
    }

    public void renderLeftHand(PoseStack p_229146_1_, MultiBufferSource p_229146_2_, int p_229146_3_, MercenaryEntity p_229146_4_) {
        this.renderHand(p_229146_1_, p_229146_2_, p_229146_3_, p_229146_4_, (this.model).leftArm, (this.model).leftSleeve);
    }

    private void renderHand(PoseStack p_229145_1_, MultiBufferSource p_229145_2_, int p_229145_3_, MercenaryEntity p_229145_4_, ModelPart p_229145_5_, ModelPart p_229145_6_) {
        PlayerModel<MercenaryEntity> playermodel = this.getModel();
        this.setModelProperties(p_229145_4_);
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;
        playermodel.setupAnim(p_229145_4_, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        p_229145_5_.xRot = 0.0F;
        p_229145_5_.render(p_229145_1_, p_229145_2_.getBuffer(RenderType.entitySolid(p_229145_4_.getTexture())), p_229145_3_, OverlayTexture.NO_OVERLAY);
        p_229145_6_.xRot = 0.0F;
        p_229145_6_.render(p_229145_1_, p_229145_2_.getBuffer(RenderType.entityTranslucent(p_229145_4_.getTexture())), p_229145_3_, OverlayTexture.NO_OVERLAY);
    }

    protected void setupRotations(MercenaryEntity p_225621_1_, PoseStack p_225621_2_, float p_225621_3_, float p_225621_4_, float p_225621_5_) {
        float f = p_225621_1_.getSwimAmount(p_225621_5_);
        if (p_225621_1_.isFallFlying()) {
            super.setupRotations(p_225621_1_, p_225621_2_, p_225621_3_, p_225621_4_, p_225621_5_);
            float f1 = (float)p_225621_1_.getFallFlyingTicks() + p_225621_5_;
            float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!p_225621_1_.isAutoSpinAttack()) {
                p_225621_2_.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - p_225621_1_.getXRot())));
            }

            Vec3 vec3 = p_225621_1_.getViewVector(p_225621_5_);
            Vec3 vec31 = p_225621_1_.getDeltaMovement();
            double d0 = vec31.horizontalDistanceSqr();
            double d1 = vec3.horizontalDistanceSqr();
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
                double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
                p_225621_2_.mulPose(Vector3f.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            super.setupRotations(p_225621_1_, p_225621_2_, p_225621_3_, p_225621_4_, p_225621_5_);
            float f3 = p_225621_1_.isInWater() ? -90.0F - p_225621_1_.getXRot() : -90.0F;
            float f4 = Mth.lerp(f, 0.0F, f3);
            p_225621_2_.mulPose(Vector3f.XP.rotationDegrees(f4));
            if (p_225621_1_.isVisuallySwimming()) {
                p_225621_2_.translate(0.0D, -1.0D, (double)0.3F);
            }
        } else {
            super.setupRotations(p_225621_1_, p_225621_2_, p_225621_3_, p_225621_4_, p_225621_5_);
        }

    }
}
