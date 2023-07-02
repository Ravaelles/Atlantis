package atlantis.combat.micro.avoid.buildings;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AvoidCombatBuildingCriticallyClose {

    public static boolean handle(AUnit unit, AUnit combatBuilding) {
        if (isHoldingTooLong(unit, combatBuilding)) {
            return handleHoldTooLong(unit, combatBuilding);
        }

        if (shouldHoldGround(unit, combatBuilding)) {
            unit.holdPosition("HoldHere");
            return true;
        }

        return false;
    }

    private static boolean shouldHoldGround(AUnit unit, AUnit combatBuilding) {
        return unit.isMoving()
            && unit.targetPosition() != null
            && unit.targetPosition().distTo(combatBuilding) < 7.25;
    }

    private static boolean isHoldingTooLong(AUnit unit, AUnit combatBuilding) {
        return unit.isHoldingPosition() && unit.noCooldown() && unit.lastActionMoreThanAgo(5);
    }

    private static boolean handleHoldTooLong(AUnit unit, AUnit combatBuilding) {
        return CircumnavigateCombatBuilding.handle(unit, combatBuilding);
    }
}
