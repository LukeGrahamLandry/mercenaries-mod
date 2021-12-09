package ca.lukegrahamlandry.mercenaries.client.render;

import ca.lukegrahamlandry.mercenaries.client.model.MercMountModel;
import ca.lukegrahamlandry.mercenaries.entity.MercMountEntity;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.util.Map;

public class MercMountRenderer extends MobRenderer<MercMountEntity, MercMountModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/horse/horse_white.png");

    public MercMountRenderer(EntityRendererManager p_i47205_1_) {
        super(p_i47205_1_, new MercMountModel(), 1);
    }

    public ResourceLocation getTextureLocation(MercMountEntity p_110775_1_) {
        return TEXTURE;
    }
}
