package atlantis.combat.retreating.protoss.big_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ProtossFullRetreat extends Manager {
    public ProtossFullRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionAttack()) return false;
        if (OurArmy.strength() >= 500) return false;
        if (unit.combatEvalRelative() >= 1) return false;
        if (unit.enemiesNear().combatUnits().empty()) return false;

        AUnit base = Select.naturalOrMain();
        if (base == null || base.distTo(unit) <= 8) return false;

        double evalRelative = unit.combatEvalRelative()
            - (unit.distToNearestChokeLessThan(5) ? 0.15 : 0)
            - (unit.lastRetreatedAgo() <= 30 * 3 ? 0.2 : 0)
            - (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.1 : 0)
            - (unit.lastUnderAttackLessThanAgo(30 * 4) ? 0.05 : 0);

        return evalRelative <= 0.95;
    }

    @Override
    protected Manager handle() {
        if ((new ProtossStartRetreat(unit)).startRetreatingFrom(enemy())) {
//            unit.paintCircleFilled(14, Color.Red);
            unit.addLog("PFull");
            return usedManager(this);
        }

        return null;
    }

    private AUnit enemy() {
        return unit.enemiesNear().groundUnits().combatUnits().nearestTo(unit);
    }
}
