package atlantis.battle.avoid.dont;

import atlantis.architecture.Manager;
import atlantis.battle.avoid.always.ProtossAlwaysAvoidEnemy;
import atlantis.battle.avoid.dont.protoss.ObserverDontAvoidEnemy;
import atlantis.battle.avoid.dont.protoss.ProtossDontAvoid;
import atlantis.battle.avoid.dont.terran.TerranDontAvoidEnemy;
import atlantis.battle.avoid.terran.avoid.TerranAlwaysAvoidEnemy;
import atlantis.battle.avoid.terran.avoid.TerranShouldNeverAvoid;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.util.We;

public class DontAvoidEnemy extends Manager {
    public DontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.effUndetected()) return false;

        if (We.protoss()) {
            return asProtoss();
        }
        else if (We.terran()) {
            return asTerran();
        }

        return false;
    }

    @Override
    protected Manager handle() {
        throw new RuntimeException("Do not call, only use applies()");
    }

    // =========================================================

    private boolean asProtoss() {
        if (unit.isReaver() && unit.shieldWound() <= 16) return true;

        if ((new ProtossDontAvoid(unit)).dontAvoid()) {
            return true;
        }

        if ((new ProtossAlwaysAvoidEnemy(unit)).applies()) {
//                System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - ProtossAlwaysAvoidEnemy");
            return false;
        }

        Decision decision;
        if ((decision = ObserverDontAvoidEnemy.dontAvoid(unit)).notIndifferent()) return decision.toBoolean();

//            if (true) return false;
        return (new ProtossDontAvoid(unit)).dontAvoid();
    }

    // =========================================================

    private boolean asTerran() {
        if ((new TerranShouldNeverAvoid(unit)).shouldNeverAvoid()) return true;
        if (unit.isTank() && unit.cooldownRemaining() <= 0) return true;

        if ((new TerranAlwaysAvoidEnemy(unit)).applies()) return false;

        return (new TerranDontAvoidEnemy(unit)).anySubmanagerApplies() != null;
    }
}
