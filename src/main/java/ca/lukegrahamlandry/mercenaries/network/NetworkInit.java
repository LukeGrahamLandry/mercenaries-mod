package ca.lukegrahamlandry.mercenaries.network;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkInit {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerPackets(){
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MercenariesMain.MOD_ID, "mercenaries"), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(nextID(), LightningPacket.class, LightningPacket::toBytes, LightningPacket::new, LightningPacket::handle);

    }
}
