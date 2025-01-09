package atlantis.combat.micro.avoid.dont;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.always.ProtossAlwaysAvoidEnemy;
import atlantis.combat.micro.avoid.dont.protoss.ObserverDontAvoidEnemy;
import atlantis.combat.micro.avoid.dont.protoss.ProtossDontAvoidEnemy;
import atlantis.combat.micro.avoid.dont.terran.TerranDontAvoidEnemy;
import atlantis.combat.micro.avoid.terran.avoid.TerranAlwaysAvoidEnemy;
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

        // === Protoss ===========================================

        if (We.protoss()) {
            if ((new ProtossAlwaysAvoidEnemy(unit)).applies()) return false;

            Decision decision;
            if ((decision = ObserverDontAvoidEnemy.dontAvoid(unit)).notIndifferent()) return decision.toBoolean();

//            if (true) return false;
            return (new ProtossDontAvoidEnemy(unit)).dontAvoid();
        }

        // === Terran ===========================================

        if (We.terran()) {
            if ((new TerranAlwaysAvoidEnemy(unit)).applies()) return false;

            return (new TerranDontAvoidEnemy(unit)).anySubmanagerApplies() != null;
        }

        // =========================================================

        return false;
    }
}
