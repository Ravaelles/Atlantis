package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class AvoidCombatBuildingCriticallyClose extends Manager {
    private AUnit combatBuilding;

    public AvoidCombatBuildingCriticallyClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return (
            combatBuilding = unit.enemiesNear().buildings().combatBuildingsAnti(unit).inRadius(9, unit).nearestTo(unit)
        ) != null;
    }

    @Override
    protected Manager handle() {
        if (shouldHoldGround(combatBuilding)) {
            unit.holdPosition("HoldHere");
            return usedManager(this);
        }

        else {
            unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_AVOID, "AvoidCB");
            return usedManager(this);
        }
    }

    private boolean shouldHoldGround(AUnit combatBuilding) {
        return unit.isMoving()
            && unit.targetPosition() != null
            && unit.targetPosition().distTo(combatBuilding) < 7.65
            && unit.targetPosition().distTo(combatBuilding) >= 7.1;
    }

    private boolean isHoldingTooLong(AUnit combatBuilding) {
        return unit.isHoldingPosition() && unit.noCooldown() && unit.lastActionMoreThanAgo(5);
    }
}
