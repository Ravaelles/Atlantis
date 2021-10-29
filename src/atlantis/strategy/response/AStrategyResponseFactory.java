package atlantis.strategy.response;

import atlantis.util.Cache;
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
