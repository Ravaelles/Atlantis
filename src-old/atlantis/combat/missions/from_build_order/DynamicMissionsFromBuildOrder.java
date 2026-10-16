package atlantis.combat.missions.from_build_order;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicMissionsFromBuildOrder {
    private static final Map<Integer, Mission> supplyToMissions = new HashMap<>();

    public static void addDynamicMission(String mission, int supply) {
        supplyToMissions.put(supply, Missions.fromString(mission));
    }

    public static boolean hasAnyDynamicMission() {
        return !supplyToMissions.isEmpty();
    }

    public static Mission defineDynamicMissionsForCurrentSupply(int currentSupplyUsed) {
        List<Mission> missions = supplyToMissions.keySet().stream()
            .filter(supply -> currentSupplyUsed >= supply)
            .map(supplyToMissions::get)
            .collect(Collectors.toList());

        if (missions.isEmpty()) return null;

        return missions.get(missions.size() - 1);
    }
}
