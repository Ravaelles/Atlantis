package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AvoidCombatBuildingCriticallyClose extends Manager {
    private AUnit combatBuilding;

    public AvoidCombatBuildingCriticallyClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return (combatBuilding = combatBuilding()) != null
            && combatBuildingShouldNotBeEngaged();
    }

    private AUnit combatBuilding() {
        return unit.enemiesNear()
            .buildings()
            .combatBuildingsAnti(unit)
            .inRadius(10, unit)
            .nearestTo(unit);
    }

    private boolean combatBuildingShouldNotBeEngaged() {
        Selection ourCombatUnits = combatBuilding.enemiesNear().combatUnits();
        AUnit ourUnit = ourCombatUnits.nearestTo(combatBuilding);
//        System.err.println("ourUnit.combatEvalRelative() = " + ourUnit.combatEvalRelative());
//        System.err.println("ourUnit.combatEvalAbs() = " + ourUnit.combatEvalAbsolute());
        return ourUnit != null
            && ourCombatUnits.count() <= 4
            && (
            ourUnit.combatEvalRelative() <= 2.5
                && ourUnit.combatEvalAbsolute() <= -200
        );
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
        double minDist = (unit.isRanged() ? 8.0 : 9.2) + unit.woundPercent() / 80.0;
        double dist = unit.targetPosition().distTo(combatBuilding);

        if (
            dist >= 7.2 && dist <= 7.9 && (
                !unit.isAttacking() || !unit.isTargetInWeaponRangeAccordingToGame()
            )) return true;

        return unit.isMoving()
            && unit.targetPosition() != null
            && dist < minDist;
    }
}
