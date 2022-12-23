package ca.lukegrahamlandry.mercenaries;

import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.lib.config.ConfigWrapper;
import ca.lukegrahamlandry.lib.data.DataWrapper;
import ca.lukegrahamlandry.lib.data.impl.GlobalDataWrapper;
import ca.lukegrahamlandry.mercenaries.wrapped.GlobalMercListData;
import ca.lukegrahamlandry.mercenaries.wrapped.MercSyncedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MercenariesMod {
    public static final String MOD_ID = "mercenaries";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ConfigWrapper<MercSyncedConfig> CONFIG = ConfigWrapper.synced(MercSyncedConfig.class).named(MOD_ID);
    public static GlobalDataWrapper<GlobalMercListData> MERC_LIST = DataWrapper.global(GlobalMercListData.class).named(MOD_ID).saved();

    public static void init(){
        MercRegistry.init();
        MercKeybinds.init();
    }
}
