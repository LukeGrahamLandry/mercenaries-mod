package ca.lukegrahamlandry.mercenaries.client.gui;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import ca.lukegrahamlandry.mercenaries.network.SetMercCampPacket;
import ca.lukegrahamlandry.mercenaries.network.SetMercStancePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;

public class MercenaryScreen extends AbstractContainerScreen<MerceneryContainer> {
    private static final ResourceLocation GUARD_GUI_TEXTURES_NO_SWORD = new ResourceLocation(MercenariesMod.MOD_ID, "textures/container/inventory_old.png");
    private static final ResourceLocation GUARD_GUI_TEXTURES = new ResourceLocation(MercenariesMod.MOD_ID, "textures/container/inventory.png");
    private final MercenaryEntity merc;

    MercenaryEntity.AttackStance attackStance;
    MercenaryEntity.MovementStance moveStance;

    public MercenaryScreen(MerceneryContainer container, Inventory playerInventory, MercenaryEntity merc) {
        super(container, playerInventory, merc.getDisplayName());
        this.merc = merc;
        this.passEvents = false;
        this.attackStance = this.merc.getAttackStance();
        this.moveStance = this.merc.getMoveStance();
    }

    @Override
    protected void init() {
        super.init();
        Component stanceText = Component.literal(this.attackStance.name().toLowerCase());
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(new Button(i - 90, j +20+30, 80, 20, stanceText, (p_214318_1_) -> {
            this.attackStance = MercenaryEntity.AttackStance.values()[(this.attackStance.ordinal() + 1) % MercenaryEntity.AttackStance.values().length];
            this.merc.setAttackStance(this.attackStance);
            new SetMercStancePacket(this.attackStance, this.moveStance, this.merc.getId()).sendToServer();
            this.clearWidgets();
            this.init();
        }));

        stanceText = Component.literal(this.moveStance.name().toLowerCase());
        this.addRenderableWidget(new Button(i - 90, j +20+30+30, 80, 20, stanceText, (p_214318_1_) -> {
            this.moveStance = MercenaryEntity.MovementStance.values()[(this.moveStance.ordinal() + 1) % MercenaryEntity.MovementStance.values().length];
            this.merc.setMoveStance(this.moveStance);
            new SetMercStancePacket(this.attackStance, this.moveStance, this.merc.getId()).sendToServer();
            this.clearWidgets();
            this.init();
        }));

        this.addRenderableWidget(new Button(i - 90, j +20+30+30+30, 80, 20, Component.literal("Set Camp Pos"), (p_214318_1_) -> {
            this.merc.setCamp(this.merc.blockPosition());
            new SetMercCampPacket(this.merc.getId()).sendToServer();
            this.clearWidgets();
            this.init();
        }));
    }

    protected void renderLabels(PoseStack matrix, int mouseX, int mouseY) {
        int health = Mth.ceil(merc.getHealth());
        int armor = merc.getArmorValue();
        AttributeInstance attrMaxHealth = merc.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getValue();
        float absorb = Mth.ceil(merc.getAbsorptionAmount());
        int food = merc.getFood();
        int money = merc.getMoney();

        BarRenderHelper.renderArmor(matrix, 0-90, 0 -15+50, armor, this::blit);
        BarRenderHelper.renderHealth(0-90, 0-10 -15+50, matrix, health, healthMax, absorb, this::blit);
        BarRenderHelper.renderFood(0-90, 0-20 -15+50, matrix, food, this::blit);
        BarRenderHelper.renderMoney(0-90, 0-30 -15+50, money);
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float ticks) {
        super.render(matrix, mouseX, mouseY, ticks);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        BlockPos camp = this.merc.getCamp();
        if (camp != null){
            this.write(matrix, "(" + camp.getX() + ", " + camp.getY() + ", " + camp.getZ() + ")", i - 90, j +20+30+30+30 + 25, 0xFFFFFF);
        }

    }

    private void write(PoseStack matrixStack, String text, int x, int y, int color){
        this.font.draw(matrixStack, Component.literal(text), x, y, color);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float p_230450_2_, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.minecraft != null;
        ResourceLocation tex = this.merc.getMainHandItem().isEmpty() ? GUARD_GUI_TEXTURES : GUARD_GUI_TEXTURES_NO_SWORD;
        RenderSystem.setShaderTexture(0, tex);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventory(i + 51, j + 75, 30, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY, this.merc);
    }
}
