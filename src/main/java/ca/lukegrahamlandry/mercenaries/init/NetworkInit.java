package ca.lukegrahamlandry.mercenaries.init;

import ca.lukegrahamlandry.mercenaries.MercenariesMain;
import ca.lukegrahamlandry.mercenaries.network.*;
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

        INSTANCE.registerMessage(nextID(), OpenMercenaryInventoryPacket.class, OpenMercenaryInventoryPacket::toBytes, OpenMercenaryInventoryPacket::new, OpenMercenaryInventoryPacket::handle);
        INSTANCE.registerMessage(nextID(), SetMercStancePacket.class, SetMercStancePacket::toBytes, SetMercStancePacket::new, SetMercStancePacket::handle);
        INSTANCE.registerMessage(nextID(), BuyNewMercPacket.class, BuyNewMercPacket::toBytes, BuyNewMercPacket::new, BuyNewMercPacket::handle);
        INSTANCE.registerMessage(nextID(), OpenLeaderScreenPacket.class, OpenLeaderScreenPacket::toBytes, OpenLeaderScreenPacket::new, OpenLeaderScreenPacket::handle);
        INSTANCE.registerMessage(nextID(), MercKeybindPacket.class, MercKeybindPacket::toBytes, MercKeybindPacket::new, MercKeybindPacket::handle);
        INSTANCE.registerMessage(nextID(), SetMercCampPacket.class, SetMercCampPacket::toBytes, SetMercCampPacket::new, SetMercCampPacket::handle);
    }
}
