package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossRetreating;
import atlantis.combat.retreating.terran.TerranRetreating;
import atlantis.combat.retreating.zerg.ZergRetreating;
import atlantis.decions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class ShouldRetreat extends Manager {
    private static Cache<Boolean> cache = new Cache<>();

    public ShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected Manager handle() {
        if (shouldRetreat(unit)) {
            return usedManager(this);
        }

        return null;
    }

    /**
     * If chances to win the skirmish with the Near enemy units aren't favorable, avoid fight and retreat.
     */
    public static boolean shouldRetreat(final AUnit unit) {
        return cache.get(
            "shouldRetreat:" + unit.id(),
            19,
            () -> {
                if (unit.enemiesNear().empty()) return false;
                if (A.isUms() && A.supplyUsed() <= 30) return false;

                if (unit.combatEvalRelative() <= 0.8) return true;

                if (unit.isRunning()) return false;

//                if (TempDontRetreat.temporarilyDontRetreat()) {
//                    return false;
//                }

                Decision decisionRetreat = decisionRetreat(unit);

                return returnDecision(unit, decisionRetreat);
            }
        );
    }

    private static Decision decisionRetreat(AUnit unit) {
        if (We.protoss()) return ProtossRetreating.decision(unit);
        if (We.terran()) return TerranRetreating.decision(unit);
        if (We.zerg()) return ZergRetreating.decision(unit);
        return Decision.INDIFFERENT;
    }

    private static boolean returnDecision(AUnit unit, Decision decisionRetreat) {
        countNewDecisionsToRetreat(unit, decisionRetreat);

        return decisionRetreat.toBoolean();
    }

    private static void countNewDecisionsToRetreat(AUnit unit, Decision decisionRetreat) {
        if (decisionRetreat.isTrue() && unit.isLeader() && !unit.isRetreating()) {
            RetreatManager.GLOBAL_RETREAT_COUNTER++;
        }
    }
}
