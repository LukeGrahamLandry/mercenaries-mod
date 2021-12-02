package ca.lukegrahamlandry.mercenaries.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.client.gui.AbstractGui.GUI_ICONS_LOCATION;

public class BarRenderHelper {
    private static Drawable EMERALD = new Drawable(new ResourceLocation("textures/item/emerald.png"), 10, 10);

    private static void bind(ResourceLocation res){
        Minecraft.getInstance().getTextureManager().bind(res);
    }

    public static void renderArmor(MatrixStack mStack, int left, int top, int level, IBlit draw) {
        bind(GUI_ICONS_LOCATION);
        RenderSystem.enableBlend();


        // level > 0  -> level >= 0
        for (int i = 1; level >= 0 && i < 20; i += 2)
        {
            if (i < level)
            {
                draw.blit(mStack, left, top, 34, 9, 9, 9);
            }
            else if (i == level)
            {
                draw.blit(mStack, left, top, 25, 9, 9, 9);
            }
            else if (i > level)
            {
                draw.blit(mStack, left, top, 16, 9, 9, 9);
            }
            left += 8;
        }

        RenderSystem.disableBlend();
    }


    public static void renderHealth(int left, int top, MatrixStack mStack, int health, float healthMax, float absorb, IBlit draw) {
        bind(GUI_ICONS_LOCATION);
        RenderSystem.enableBlend();

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        final int TOP =  0;
        final int BACKGROUND = 16;
        int MARGIN = 16;
        float absorbRemaining = absorb;

        for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceil((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            draw.blit(mStack, x, y, BACKGROUND, TOP, 9, 9);


            if (absorbRemaining > 0.0F)
            {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                {
                    draw.blit(mStack, x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                }
                else
                {
                    draw.blit(mStack, x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            }
            else
            {
                if (i * 2 + 1 < health)
                    draw.blit(mStack, x, y, MARGIN + 36, TOP, 9, 9); //4
                else if (i * 2 + 1 == health)
                    draw.blit(mStack, x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        RenderSystem.disableBlend();
    }

    public static void renderFood(int xStart, int y, MatrixStack mStack, int level, IBlit draw) {
        bind(GUI_ICONS_LOCATION);
        RenderSystem.enableBlend();
        for (int i = 0; i < 10; ++i) {
            int idx = i * 2 + 1;
            int x = xStart + (i * 8);
            int icon = 16;
            byte background = 0;

            draw.blit(mStack, x, y, 16 + background * 9, 27, 9, 9);

            if (idx < level)
                draw.blit(mStack, x, y, icon + 36, 27, 9, 9);
            else if (idx == level)
                draw.blit(mStack, x, y, icon + 45, 27, 9, 9);
        }
        RenderSystem.disableBlend();
    }

    public static void renderMoney(int left, int top, int level) {
        bind(GUI_ICONS_LOCATION);
        RenderSystem.enableBlend();

        for (int i = 1; level > 0 && i < 20; i += 2) {
            if (i < level) {
                EMERALD.draw(left, top);
            }
            left += 8;
        }

        RenderSystem.disableBlend();
    }

    public interface IBlit {
        void blit(MatrixStack p_238474_1_, int p_238474_2_, int p_238474_3_, int p_238474_4_, int p_238474_5_, int p_238474_6_, int p_238474_7_);
    }
}
