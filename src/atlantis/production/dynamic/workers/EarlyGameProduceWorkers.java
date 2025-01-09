package atlantis.production.dynamic.workers;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class EarlyGameProduceWorkers {
    protected static Decision decision() {
        if (We.zerg()) return Decision.TRUE("ZergEarlyGameProduceWorkers");
        if (Count.workers() >= 20) return Decision.INDIFFERENT;

        Decision decision = null;

        if (We.protoss()) decision = forProtoss();

        if (decision != null) return decision;

        return Decision.TRUE("ConstantEarlyFlow");
    }

    private static Decision forProtoss() {
        if (A.minerals() <= 209 && ConstructionRequests.countNotStartedOfType(AUnitType.Protoss_Cybernetics_Core) > 0) {
            return Decision.FALSE("PrioritizeCore");
        }

        return null;
    }
}
