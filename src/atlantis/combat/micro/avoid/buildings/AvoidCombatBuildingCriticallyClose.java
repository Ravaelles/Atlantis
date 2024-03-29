package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AvoidCombatBuildingCriticallyClose extends Manager {
    private CircumnavigateCombatBuilding circumnavigateCombatBuilding;

    public AvoidCombatBuildingCriticallyClose(AUnit unit) {
        super(unit);
        circumnavigateCombatBuilding = new CircumnavigateCombatBuilding(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    public Manager handle(AUnit combatBuilding) {
        if (isHoldingTooLong(combatBuilding)) {
            return handleHoldTooLong(combatBuilding);
        }

        if (shouldHoldGround(combatBuilding)) {
            unit.holdPosition("HoldHere");
            return usedManager(this);
        }

        return null;
    }

    private boolean shouldHoldGround(AUnit combatBuilding) {
        return unit.isMoving()
            && unit.targetPosition() != null
            && unit.targetPosition().distTo(combatBuilding) < 7.25;
    }

    private boolean isHoldingTooLong(AUnit combatBuilding) {
        return unit.isHoldingPosition() && unit.noCooldown() && unit.lastActionMoreThanAgo(5);
    }

    private Manager handleHoldTooLong(AUnit combatBuilding) {
        return circumnavigateCombatBuilding.handle(combatBuilding);
    }
}
