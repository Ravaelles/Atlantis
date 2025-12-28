package atlantis.cherryvis.simple;

import atlantis.combat.missions.Mission;
import atlantis.game.A;

import java.util.ArrayList;
import java.util.List;

public class ACherryVis_GlobalState {
    public static List<ACherryVis_GlobalState> all = new ArrayList<>();

    private static Mission mission = null;

//    public static void updateAll() {
//        updateMission();
//    }
//
//    private static void updateMission() {
//        Mission currentMission = Missions.globalMission();
//        if (!currentMission.equals(mission)) {
//            mission = currentMission;
//            ACherryVis_GlobalState.create("Mission", mission.name() + "");
//        }
//    }

    // =========================================================

    private int frame;
    private String key;
    private String value;

    private ACherryVis_GlobalState(String stateName, String stateValue) {
        this.frame = A.now;
        this.key = stateName;
        this.value = stateValue;

        all.add(this);
    }

    protected static void logNewState(String key, String value) {
//        String prefix = A.minSec() + ": ";

        ACherryVis_GlobalState.create(key, value);
    }

    public static ACherryVis_GlobalState create(String stateName, String stateValue) {
        return new ACherryVis_GlobalState(stateName, stateValue);
    }

    public String toJson() {
        return "\"" + frame + "\": {"
            + "\"" + key + "\":\"" + value + "\""
            + "}";
    }
}
