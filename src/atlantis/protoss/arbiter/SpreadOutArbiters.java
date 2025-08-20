package atlantis.protoss.arbiter;

import atlantis.combat.micro.generic.managers.SpreadOutAirUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class SpreadOutArbiters extends SpreadOutAirUnits {
    public SpreadOutArbiters(AUnit unit) {
        super(unit);
    }

    @Override
    protected Selection unitsSelector() {
        return unit.friendsNear().ofType(AUnitType.Protoss_Arbiter);
    }

    @Override
    protected double minDistBetweenUnits() {
        return 3 + unit.hpPercent() / 20.0;
    }
}
