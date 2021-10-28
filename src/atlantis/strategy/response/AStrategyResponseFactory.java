package atlantis.strategy.response;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.map.AMap;
import atlantis.production.requests.AAntiAirBuildingRequests;
import atlantis.production.requests.AAntiLandBuildingRequests;
import atlantis.production.requests.ADetectorRequest;
import atlantis.production.requests.ARequests;
import atlantis.scout.AScoutManager;
import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;
import atlantis.strategy.EnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.Cache;
import atlantis.util.Enemy;
import atlantis.util.Us;
import bwapi.Race;


public class AStrategyResponseFactory {

    private static Cache<AStrategyResponse> cache = new Cache<>();

    // =========================================================

    public static AStrategyResponse forOurRace() {
        return cache.get(
                "forOurRace",
                -1,
                () -> {
                    Race race = Us.race();

                    if (race == Race.Protoss) {
                        return new ProtossStrategyResponse();
                    } else if (race == Race.Terran) {
                        return new TerranStrategyResponse();
                    } else if (race == Race.Zerg) {
                        return new ZergStrategyResponse();
                    }
                    return null;
                }
        );
    }

}
