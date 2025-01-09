package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class AsAirRunToCannon extends Manager {
    private AUnit cannon;

    public AsAirRunToCannon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir()
            && We.protoss()
            && Count.cannons() > 0
            && (cannon = cannonToGoTo()) != null;
    }

    @Override
    public Manager handle() {
        if (unit.move(cannon, Actions.MOVE_SAFETY, "AirToCannon")) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit cannonToGoTo() {
        AUnit cannon = Select.ourOfType(AUnitType.Protoss_Photon_Cannon).nearestTo(unit);
        if (cannon == null) return null;

        double cannonDist = cannon.distTo(unit);

        if (cannonDist <= 2 && unit.cooldown() <= 2) {
            return null;
        }

        if (cannonDist >= 0.6) {
            return cannon;
        }

        return null;
    }
}
