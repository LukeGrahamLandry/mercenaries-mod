package ca.lukegrahamlandry.mercenaries.wrapped;

import ca.lukegrahamlandry.mercenaries.MercenariesMod;
import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class GlobalMercListData {
    HashMap<UUID, ArrayList<UUID>> mercs;

    public void addMerc(Player player, MercenaryEntity merc){
        ArrayList<UUID> owned = mercs.getOrDefault(player.getUUID(), new ArrayList<>());
        owned.add(merc.getUUID());
        mercs.put(player.getUUID(), owned);
        MercenariesMod.MERC_LIST.setDirty();
    }

    public void removeMerc(Player player, MercenaryEntity merc){
        ArrayList<UUID> owned = mercs.getOrDefault(player.getUUID(), new ArrayList<>());
        owned.remove(merc.getUUID());
        MercenariesMod.MERC_LIST.setDirty();
    }

    public ArrayList<UUID> getMercs(Player player){
        return mercs.getOrDefault(player.getUUID(), new ArrayList<>());
    }

    public void forLoadedMercBelongingTo(Player player, Consumer<MercenaryEntity> action){
        if (player.level.isClientSide()) throw new RuntimeException("Cannot call GlobalMercListData#forLoadedMercBelongingTo on the client");

        for (UUID mID : getMercs(player)){
            Entity maybeMerc = ((ServerLevel)player.level).getEntity(mID);
            if (maybeMerc instanceof MercenaryEntity){
                action.accept((MercenaryEntity) maybeMerc);
            }
        }
    }
}
