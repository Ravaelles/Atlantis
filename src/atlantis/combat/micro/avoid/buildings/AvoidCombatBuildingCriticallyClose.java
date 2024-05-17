package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AvoidCombatBuildingCriticallyClose extends Manager {
    private AUnit combatBuilding;
    private Selection ourCombatUnitsNearby;

    public AvoidCombatBuildingCriticallyClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return (combatBuilding = combatBuilding()) != null
            && combatBuildingShouldNotBeEngaged();
    }

    @Override
    protected Manager handle() {
        if (shouldHoldGround(combatBuilding)) {
            unit.holdPosition("HoldHere");
            return usedManager(this);
        }

        unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_AVOID, "AvoidCB");
        return usedManager(this);
    }

    private boolean shouldHoldGround(AUnit combatBuilding) {
        if (unit.lastUnderAttackLessThanAgo(30 * 2)) return false;

        double dist = unit.targetPosition().distTo(combatBuilding);
        if (dist <= 7.5) return false;

        double minDistAllowed = (unit.isRanged() ? 8.5 : 9.9) + unit.woundPercent() / 60.0;

        if (
            dist <= minDistAllowed && (
                !unit.isAttacking() || !unit.isTargetInWeaponRangeAccordingToGame()
            )
        ) return true;

        return unit.isMoving()
            && unit.targetPosition() != null
            && dist < minDistAllowed;
    }

    // =========================================================


    private AUnit combatBuilding() {
        return unit.enemiesNear()
            .buildings()
            .onlyCompleted()
            .combatBuildingsAnti(unit)
            .inRadius(10, unit)
            .nearestTo(unit);
    }

    private boolean combatBuildingShouldNotBeEngaged() {
        ourCombatUnitsNearby = combatBuilding.enemiesNear().combatUnits();

        if (dontEngageBecauseTooManyEnemyCombatUnitsNearby()) return true;

        AUnit ourUnit = ourCombatUnitsNearby.nearestTo(combatBuilding);
//        System.err.println("ourUnit.combatEvalRelative() = " + ourUnit.combatEvalRelative());
//        System.err.println("ourUnit.combatEvalAbs() = " + ourUnit.combatEvalAbsolute());
        return ourUnit != null
            && ourCombatUnitsNearby.count() <= 4
            && (
            ourUnit.combatEvalRelative() <= 2.5
                && ourUnit.combatEvalAbsolute() <= -200
        );
    }

    private boolean dontEngageBecauseTooManyEnemyCombatUnitsNearby() {
        if (A.supplyUsed() >= 170 || A.hasMinerals(3000)) return false;

        if (unit.friendsNear().combatUnits().atLeast(25)) return false;

        return combatBuilding.enemiesNear()
            .combatUnits()
            .inRadius(6, combatBuilding).atMost((int) (ourCombatUnitsNearby.count() / 8));
    }
}
