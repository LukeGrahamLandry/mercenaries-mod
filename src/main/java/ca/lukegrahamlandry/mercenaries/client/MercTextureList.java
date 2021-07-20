package ca.lukegrahamlandry.mercenaries.client;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MercTextureList {
    static final int MAX_TEXTURES = 255;
    static final Random rand = new Random();

    static final List<ResourceLocation> validMercTextures = new ArrayList<>();
    static final List<ResourceLocation> validLeaderTextures = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    public static void clientInit(){
        // load the defaults bundled with the mod jar

        String[] mercDefaults = new String[]{
                "default_1", "default_2"
        };
        String[] leaderDefaults = new String[]{
                "default"
        };

        for (String str : mercDefaults) {
            validMercTextures.add(new ResourceLocation(MercenariesMain.MOD_ID, "textures/entity/merc/" + str + ".png"));
        }

        for (String str : leaderDefaults) {
            validLeaderTextures.add(new ResourceLocation(MercenariesMain.MOD_ID, "textures/entity/leader/" + str + ".png"));
        }

        // load the extras added by resource packs. they must be named "texture_i" with i being sequential integers from 0 to 244

        for (int i=0;i<MAX_TEXTURES;i++){
            ResourceLocation check = new ResourceLocation(MercenariesMain.MOD_ID, "textures/entity/merc/texture_" + i + ".png");
            try {
                Minecraft.getInstance().getResourceManager().getResource(check);
                validMercTextures.add(check);
            } catch (IOException e) {
                break;
            }
        }

        for (int i=0;i<MAX_TEXTURES;i++){
            ResourceLocation check = new ResourceLocation(MercenariesMain.MOD_ID, "textures/entity/leader/texture_" + i + ".png");
            try {
                Minecraft.getInstance().getResourceManager().getResource(check);
                validLeaderTextures.add(check);
            } catch (IOException e) {
                break;
            }
        }
    }

    public static int getRandom() {
        return rand.nextInt(MAX_TEXTURES);
    }

    public static ResourceLocation getMercTexture(int textureType){
        int index = textureType % validMercTextures.size();
        return validMercTextures.get(index);
    }

    public static ResourceLocation getLeaderTexture(int textureType){
        int index = textureType % validLeaderTextures.size();
        return validLeaderTextures.get(index);
    }
}
