package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.mercenaries.entity.MercenaryEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SaveMercData extends WorldSavedData {
    static String ID = MercenariesMain.MOD_ID + ":worlddata";

    HashMap<UUID, ArrayList<UUID>> mercs;

    public SaveMercData(){
        super(ID);
        mercs = new HashMap<>();
    }

    public void addMerc(PlayerEntity player, MercenaryEntity merc){
        ArrayList<UUID> owned = mercs.getOrDefault(player.getUUID(), new ArrayList<>());
        owned.add(merc.getUUID());
        this.setDirty();
    }

    public void removeMerc(PlayerEntity player, MercenaryEntity merc){
        ArrayList<UUID> owned = mercs.getOrDefault(player.getUUID(), new ArrayList<>());
        owned.remove(merc.getUUID());
        this.setDirty();
    }

    public ArrayList<UUID> getMercs(PlayerEntity player){
        return mercs.getOrDefault(player.getUUID(), new ArrayList<>());
    }

    public static SaveMercData getInstance(World world){
        return ((ServerWorld) world.getServer().getLevel(World.OVERWORLD)).getDataStorage().get(SaveMercData::new, ID);
    }

    @Override
    public void load(CompoundNBT nbt) {
        this.mercs = new HashMap<>();

        ListNBT playerList = nbt.getList("owned_mercs", 9);

        for (int i=0;i<playerList.size();i++){
            CompoundNBT tag = playerList.getCompound(i);
            UUID player = tag.getUUID("player");
            ArrayList<UUID> owned = new ArrayList();
            ListNBT mercList = tag.getList("mercs", 11);
            for (int j=0;j<mercList.size();j++){
                owned.add(UUID.fromString(mercList.getString(j)));
            }

            this.mercs.put(player, owned);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT playerList = new ListNBT();
        for (UUID player : this.mercs.keySet()){
            CompoundNBT tag = new CompoundNBT();

            tag.putUUID("player", player);
            ListNBT mercList = new ListNBT();

            for (UUID merc : this.mercs.getOrDefault(player, new ArrayList<>())){
                mercList.add(NBTUtil.createUUID(merc));
            }
            tag.put("mercs", mercList);

            playerList.add(tag);
        }
        nbt.put("owned_mercs", playerList);

        return nbt;
    }
}