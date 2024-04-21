package atlantis.combat.retreating.protoss.big_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ProtossBigScaleRetreat extends Manager {
    public ProtossBigScaleRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        if (!unit.isMissionAttack()) return false;

        AUnit base = Select.naturalOrMain();
        if (base == null || base.distTo(unit) <= 8) return false;

        double evalRelative = unit.combatEvalRelative()
            - (unit.distToNearestChokeLessThan(5) ? 0.3 : 0)
            - (unit.lastRetreatedAgo() <= 30 * 5 ? 0.3 : 0);

        return evalRelative <= 0.95;
    }

    @Override
    protected Manager handle() {
        if ((new ProtossStartRetreat(unit)).startRetreatingFrom(null)) {
//            unit.paintCircleFilled(14, Color.Red);
            return usedManager(this);
        }

        return null;
    }
}
