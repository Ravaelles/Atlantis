package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AvoidCombatBuildingCriticallyClose extends Manager {
    private final AUnit combatBuilding;

    public AvoidCombatBuildingCriticallyClose(AUnit unit, AUnit combatBuilding) {
        super(unit);
        this.combatBuilding = combatBuilding;
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected Manager handle() {
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
}
