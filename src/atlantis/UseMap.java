package atlantis;

public class UseMap {

    public static String activeMap() {
//        return "sscai/(?)*.sc?"; // Default map-pack for SSCAIT

        // === UMS maps - great for bot development ============

        return "ums/trainzvreaver.scm"; // Zerglings & Hydras vs. 2 Reavers & Shuttle
//        return "ums/training-PvT.scx"; // Dragoons & Zealots vs. Vultures & Tanks
//        return "ums/trening nr 2.scx";
//        return "ums/micro tvp 1.00.scx"; // Huge Terran army (tanks & vultures) vs. Zealots & Hi-Templars & Dragoons
//        return "ums/micro3.scx";
//        return "ums/wraiths_vs_carriers_obs.scx"; // Wraiths & Valkyries vs. Carriers & Observers
//        return "ums/(1)micro3_007.scx";
//        return "ums/dragoons_vs_map.scx"; // 4 Dragoons attacking Zealots
//        return "ums/dragoon_sweeping_mines.scm"; // 5 dragoons vs. mines
//        return "ums/marines_vs_zerglings.scm"; // 12 marines vs. 24 zerglings
//        return "ums/vulture_control.scx"; // Vulture vs. Broodlings
//        return "ums/1a2a3a Micro 2.scx";
//        return "ums/MultiTask PvT.scx";
//        return "ums/ControlFighterTZ-Easy.scx"; // Tanks & Marines vs. Zerg
//        return "ums/protoss_micro.scx";
    }

    // =========================================================

    public static String activeMapPath() {
        return "maps/BroodWar/" + activeMap();
    }

}
