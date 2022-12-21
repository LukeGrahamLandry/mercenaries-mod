package ca.lukegrahamlandry.mercenaries.client.gui;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.network.SetMercCampPacket;
import ca.lukegrahamlandry.mercenaries.network.SetMercStancePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;


public class MercenaryScreen extends ContainerScreen<MerceneryContainer> {
    private static final ResourceLocation GUARD_GUI_TEXTURES_NO_SWORD = new ResourceLocation(MercenariesMod.MOD_ID, "textures/container/inventory_old.png");
    private static final ResourceLocation GUARD_GUI_TEXTURES = new ResourceLocation(MercenariesMod.MOD_ID, "textures/container/inventory.png");
    private final MercenaryEntity merc;

    private float xMouse;
    private float yMouse;

    int attackStance;
    int moveStance;
    private static final String[] attackStanceTypes = new String[]{
            "Attack", "Defend", "Passive"
    };

    private static final String[] moveStanceTypes = new String[]{
            "Follow", "Idle", "Stay"
    };

    public MercenaryScreen(MerceneryContainer container, PlayerInventory playerInventory, MercenaryEntity merc) {
        super(container, playerInventory, merc.getDisplayName());
        this.merc = merc;
        this.passEvents = false;
        this.attackStance = this.merc.getAttackStance();
        this.moveStance = this.merc.getMoveStance();
    }

    @Override
    protected void init() {
        super.init();
        ITextComponent stanceText = new StringTextComponent(attackStanceTypes[this.attackStance]);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.addButton(new Button(i - 90, j +20+30, 80, 20, stanceText, (p_214318_1_) -> {
            this.attackStance = (this.attackStance + 1) % 3;
            this.merc.setAttackStance(this.attackStance);
            NetworkInit.INSTANCE.sendToServer(new SetMercStancePacket(this.attackStance, this.moveStance, this.merc.getId()));
            this.buttons.clear();
            this.init();
        }));

        stanceText = new StringTextComponent(moveStanceTypes[this.moveStance]);
        this.addButton(new Button(i - 90, j +20+30+30, 80, 20, stanceText, (p_214318_1_) -> {
            this.moveStance = (this.moveStance + 1) % 3;
            this.merc.setMoveStance(this.moveStance);
            NetworkInit.INSTANCE.sendToServer(new SetMercStancePacket(this.attackStance, this.moveStance, this.merc.getId()));
            this.buttons.clear();
            this.init();
        }));

        this.addButton(new Button(i - 90, j +20+30+30+30, 80, 20, new StringTextComponent("Set Camp Pos"), (p_214318_1_) -> {
            this.merc.setCamp(this.merc.blockPosition());
            NetworkInit.INSTANCE.sendToServer(new SetMercCampPacket(this.merc.getId()));
            this.buttons.clear();
            this.init();
        }));
    }

    protected void renderLabels(MatrixStack matrixStack, int p_230451_2_, int p_230451_3_) {
        // super.renderLabels(matrixStack, p_230451_2_, p_230451_3_);

        int health = MathHelper.ceil(merc.getHealth());
        int armor = merc.getArmorValue();
        ModifiableAttributeInstance attrMaxHealth = merc.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getValue();
        float absorb = MathHelper.ceil(merc.getAbsorptionAmount());
        int food = merc.getFood();
        int money = merc.getMoney();

        BarRenderHelper.renderArmor(matrixStack, 0-90, 0 -15+50, armor, this::blit);
        BarRenderHelper.renderHealth(0-90, 0-10 -15+50, matrixStack, health, healthMax, absorb, this::blit);
        BarRenderHelper.renderFood(0-90, 0-20 -15+50, matrixStack, food, this::blit);
        BarRenderHelper.renderMoney(0-90, 0-30 -15+50, money);
    }

    @Override
    public void render(MatrixStack matrixStack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
        this.xMouse = (float)p_230430_2_;
        this.yMouse = (float)p_230430_3_;

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        BlockPos camp = this.merc.getCamp();
        if (camp != null){
            this.write(matrixStack, "(" + camp.getX() + ", " + camp.getY() + ", " + camp.getZ() + ")", i - 90, j +20+30+30+30 + 25, 0xFFFFFF);
        }

    }

    private void write(MatrixStack matrixStack, String text, int x, int y, int color){
        this.font.draw(matrixStack, new StringTextComponent(text), x, y, color);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.minecraft != null;
        ResourceLocation tex = this.merc.getMainHandItem().isEmpty() ? GUARD_GUI_TEXTURES : GUARD_GUI_TEXTURES_NO_SWORD;
        this.minecraft.getTextureManager().bind(tex);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventory(i + 51, j + 75, 30, (float) (i + 51) - this.xMouse, (float) (j + 75 - 50) - this.yMouse, this.merc);
    }
}
