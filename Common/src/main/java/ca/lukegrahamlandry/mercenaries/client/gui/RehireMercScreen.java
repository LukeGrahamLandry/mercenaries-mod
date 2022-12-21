package ca.lukegrahamlandry.mercenaries.client.gui;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.entity.LeaderEntity;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.init.NetworkInit;
import ca.lukegrahamlandry.mercenaries.network.BuyNewMercPacket;
import ca.lukegrahamlandry.mercenaries.network.RehireMercPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;


public class RehireMercScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MercenariesMain.MOD_ID, "textures/container/blank.png");

    private final MercenaryEntity merc;
    private final PlayerEntity player;
    private final int guiLeft = 0;
    private final int guiTop = 0;

    private float xMouse;
    private float yMouse;
    private int price;
    private int imageWidth = 255;
    private int imageHeight = 255;
    private Button hireButton;

    public RehireMercScreen(PlayerEntity player, MercenaryEntity merc, int price) {
        super(new StringTextComponent("Mercenary"));
        this.player = player;
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

        this.hireButton = new Button(xStart, yStart, buttonWidth, 20, new StringTextComponent(text), (p_214318_1_) -> {
            if (this.hireButton.active) {
                NetworkInit.INSTANCE.sendToServer(new RehireMercPacket(this.merc.getId()));
                Minecraft.getInstance().setScreen(null);
            }
        });
        hireButton.active = this.price != Integer.MAX_VALUE;
        this.addButton(hireButton);
    }
    @Override
    public void render(MatrixStack matrixStack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.minecraft.getTextureManager().bind(TEXTURE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        super.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
        this.xMouse = (float)p_230430_2_;
        this.yMouse = (float)p_230430_3_;
    }
}
