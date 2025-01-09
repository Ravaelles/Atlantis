package atlantis.information.strategy.response;

import atlantis.information.strategy.response.protoss.ProtossStrategyResponse;
import atlantis.information.strategy.response.terran.TerranStrategyResponse;
import atlantis.information.strategy.response.zerg.ZergStrategyResponse;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import bwapi.Race;


public class RaceStrategyResponseFactory {

    private static Cache<RaceStrategyResponse> cache = new Cache<>();

    // =========================================================

    public static RaceStrategyResponse forOurRace() {
        return cache.get(
            "forOurRace",
            -1,
            () -> {
                Race race = We.race();

                if (race == Race.Protoss) {
                    return new ProtossStrategyResponse();
                }
                else if (race == Race.Terran) {
                    return new TerranStrategyResponse();
                }
                else if (race == Race.Zerg) {
                    return new ZergStrategyResponse();
                }
                return null;
            }
        );
    }

}
