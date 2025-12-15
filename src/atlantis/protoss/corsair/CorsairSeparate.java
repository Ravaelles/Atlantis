package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.managers.SpreadOutAirUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class CorsairSeparate extends SpreadOutAirUnits {
    public CorsairSeparate(AUnit unit) {
        super(unit);
    }

    @Override
    protected double minDistBetweenUnits() {
        if (unit.lastUnderAttackMoreThanAgo(30 * 6)) return 0;

        if (unit.enemiesNear().air().combatUnits().notEmpty()) return 2;

        return 4 + Math.min(6, unit.shotSecondsAgo() / 10.0);
    }

    @Override
    protected Selection unitsSelector() {
        return unit.friendsNear().ofType(unit.type());
    }
}
