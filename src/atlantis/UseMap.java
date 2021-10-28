package atlantis;

public class UseMap {

    public static String activeMap() {
        return "sscai/(?)*.sc?"; // Default map-pack for SSCAIT

        // === UMS maps - great for bot development ============

//        return "ums/";
//        return "ums/";
//        return "ums/";
//        return "ums/";
//        return "ums/";
//        return "ums/";
//        return "ums/micro challenge.scx"; // Even more minigames
//        return "ums/ConTrol2.scx"; // More minigames
//        return "ums/NeWconTrol.scx"; // Cool minigames, starting with 2 drones vs. 2 drones, lings vs. goons etc
//        return "ums/vsGosuComputer.scx"; // Game somewhat against a player
//        return "ums/mar_vs_zea.scx"; // Marines & Medics vs. Zealots on quite small map
//        return "ums/tank-dropship.scm"; // 2 Tanks & 2 Dropships vs. Dragoons
//        return "ums/trainzvreaver.scm"; // Zerglings & Hydras vs. 2 Reavers & Shuttle
//        return "ums/training-PvT.scx"; // Dragoons & Zealots vs. Vultures & Tanks + slowly Hi-Templars & Archons
//        return "ums/trening nr 2.scx";
//        return "ums/micro tvp 1.00.scx"; // Huge Terran army (tanks & vultures) vs. Zealots & Hi-Templars & Dragoons
//        return "ums/micro3.scx";
//        return "ums/wraiths_vs_carriers_obs.scx"; // Wraiths & Valkyries vs. Carriers & Observers
//        return "ums/(1)micro3_007.scx";
//        return "ums/dragoons_vs_map.scx"; // 4 Dragoons attacking Zealots
//        return "ums/dragoon_sweeping_mines.scm"; // 5 dragoons vs. mines
//        return "ums/marines_vs_zerglings.scm"; // 12 marines vs. 24 zerglings
//        return "ums/vulture_control.scx"; // Vulture vs. Broodlings
//        return "ums/MultiTask PvT.scx"; // Weird - ums but starts with bases
//        return "ums/ControlFighterTZ-Easy.scx"; // Tanks & Marines vs. Zerg
//        return "ums/protoss_micro.scx"; // Huge parallel map, bad performance wise
    }

    // =========================================================

    public static String activeMapPath() {
        return "maps/BroodWar/" + activeMap();
    }

}
