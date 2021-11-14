package atlantis.combat.micro.generic;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class MobileDetector {

    protected static AUnit unitForArmy = null;
    protected static AUnit unitForSquadScout = null;
    protected static AUnit unitForBase = null;

    // =========================================================

    public static boolean update(AUnit unit) {
        if (detectInvisibleUnitsClosestToBase(unit)) {
            return true;
        }

        if (handleSpreadOut(unit)) {
            return true;
        }

        if (followSquadScout(unit)) {
            return true;
        }

        return followArmy(unit);
    }

    // =========================================================

    protected static boolean handleSpreadOut(AUnit unit) {
        Selection units = Select.ourOfType(unit.type())
                .inRadius(17, unit).exclude(unit);
        if (units.count() > 0) {
            AUnit otherunit = units.nearestTo(unit);
            unit.moveAwayFrom(otherunit.position(), 5, "SpreadVessels");
            return true;
        }

        return false;
    }

    protected static boolean followSquadScout(AUnit unit) {
        if (!unit.is(unitForSquadScout)) {
            return false;
        }

        AUnit scout = Alpha.get().getSquadScout();
        if (scout != null) {
            unitForSquadScout = unit;
            if (scout.distTo(unit) > 1) {
                unitForSquadScout.move(scout, UnitActions.MOVE, "FollowScout");
            }
            return true;
        }

        return false;
    }

    protected static boolean followArmy(AUnit unit) {
        if (!unit.is(unitForArmy)) {
            return false;
        }

        APosition goTo = Alpha.get().center();
        if (goTo != null) {
            unitForArmy = unit;
            if (goTo.distTo(unit) > 1) {
                unitForArmy.move(goTo, UnitActions.MOVE, "Follow");
            }
            return true;
        }

        return false;
    }

    protected static boolean detectInvisibleUnitsClosestToBase(AUnit unit) {
        if (Select.main() == null) {
            return false;
        }

        if (!unit.equals(Select.ourOfType(unit.type()).first())) {
            return false;
        }

        if (!unit.is(unitForBase)) {
            return false;
        }

        AUnit dangerousInvisibleEnemy = enemyDangerousHiddenUnit();
        if (dangerousInvisibleEnemy != null) {
            unitForBase = unit;
            if (unitForBase.distTo(dangerousInvisibleEnemy) > 0.2) {
                unitForBase.move(dangerousInvisibleEnemy.position(), UnitActions.MOVE, "RevealEnemy");
            }
            return true;
        }

        return false;
    }

    protected static AUnit enemyDangerousHiddenUnit() {
        AUnit invisibleUnit = Select.enemy().effCloaked().combatUnits().nearestTo(Select.main());
        if (invisibleUnit != null) {
            return invisibleUnit;
        }

        AUnit lurkerOrDT = Select.enemy().ofType(AUnitType.Zerg_Lurker, AUnitType.Protoss_Dark_Templar).nearestTo(Select.main());
        if (lurkerOrDT != null) {
            return lurkerOrDT;
        }

//        AUnit terranCloaked = Select.enemy().effCloaked().ofType(AUnitType.Terran_Wraith, AUnitType.Terran_Ghost).nearestTo(Select.main());
//        if (terranCloaked != null) {
//            return terranCloaked;
//        }

        return Select.enemy().effCloaked().nearestTo(Select.main());
    }
    
}
