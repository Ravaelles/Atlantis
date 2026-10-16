package atlantis.combat.micro.generic.managers;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class SpreadOutDetectors extends SpreadOutAirUnits {
    public SpreadOutDetectors(AUnit unit) {
        super(unit);
    }

    @Override
    protected Selection unitsSelector() {
        return unit.friendsNear().ofType(unit.type());
    }

    @Override
    protected double minDistBetweenUnits() {
        return 9;
    }
}