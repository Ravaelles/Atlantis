package atlantis.combat.missions;

import java.util.HashMap;
import java.util.Map;

public class MissionsFromBuildOrder {

    private static final Map<Integer, Mission> supplyToMissions = new HashMap<>();

    public static void addDynamicMission(String mission, int supply) {
        supplyToMissions.put(supply, Missions.fromString(mission));
    }

}
