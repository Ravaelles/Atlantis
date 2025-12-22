package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.managers.AsAirAvoidDeadlyAntiAir;
import atlantis.combat.micro.generic.managers.AsAirRunToCannon;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class CorsairAvoidDeadlyAntiAir extends AsAirAvoidDeadlyAntiAir {
    public CorsairAvoidDeadlyAntiAir(AUnit unit) {
        super(unit);
    }

    @Override
    protected boolean moveAway() {
        if (unit.distToLeader() <= 3 && super.moveToAlphaLeader()) return true;

        return super.moveAway();
    }

    @Override
    protected boolean allowedToRunToCannon() {
        if (Count.cannons() == 0) return false;

        return unit.distToLeader() - 10 >= unit.distToCannon();
    }
}
