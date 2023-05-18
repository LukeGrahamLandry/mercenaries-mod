package ca.lukegrahamlandry.mercenaries.client.gui;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.network.RehireMercPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class RehireMercScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MercenariesMod.MOD_ID, "textures/container/blank.png");

    private final MercenaryEntity merc;

    private int price;
    private int imageWidth = 255;
    private int imageHeight = 255;
    private Button hireButton;

    public RehireMercScreen(MercenaryEntity merc, int price) {
        super(Component.literal("Mercenary"));
        this.merc = merc;
        this.passEvents = false;
        this.price = price;
    }

    @Override
    protected void init() {
        super.init();
        String text = this.price != Integer.MAX_VALUE ? "Rehire Merc (" + price + ")" : "Cannot Rehire Another";
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int buttonWidth = 150;
        int xStart = i + ((this.imageWidth - buttonWidth) / 2);
        int yStart = j + 30;

        this.hireButton = new Button(xStart, yStart, buttonWidth, 20, Component.literal(text), (p_214318_1_) -> {
            if (this.hireButton.active) {
                new RehireMercPacket(this.merc.getId()).sendToServer();
                Minecraft.getInstance().setScreen(null);
            }
        });
        hireButton.active = this.price != Integer.MAX_VALUE;
        this.addRenderableWidget(hireButton);
    }
    @Override
    public void render(PoseStack matrixStack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        super.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
    }
}
