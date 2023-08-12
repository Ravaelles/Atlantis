package atlantis.combat.micro.generic;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class MobileDetector extends Manager {

    protected AUnit unitAssignedToMainSquad = null;
    protected AUnit unitForSquadScout = null;
    protected AUnit unitForBase = null;

    // =========================================================

    public MobileDetector(AUnit unit) {
        super(unit);
    }

    // =========================================================

    @Override
    public boolean applies() {
        return unit.type().isDetectorNonBuilding();
    }

    @Override
    protected Manager handle() {
        if (update()) return usedManager(this);

        return null;
    }

    protected boolean update() {
        if (handleSpreadOut()) return true;

        if (detectInvisibleUnitsClosestToBase()) return true;

        if (followArmy(false)) return true;

        if (followSquadScout()) return true;

        return followArmy(true);
    }

    // =========================================================

    protected boolean handleSpreadOut() {
        int minDistBetween = minDistanceBetweenUnits();

        Selection units = Select.ourOfType(unit.type()).inRadius(minDistBetween, unit).exclude(unit);
        if (units.count() > 0) {
            AUnit otherunit = units.nearestTo(unit);
            unit.moveAwayFrom(otherunit.position(), minDistBetween, "SpreadDetectors", Actions.MOVE_FORMATION);
            return true;
        }

        return false;
    }

    private int minDistanceBetweenUnits() {
        return We.protoss() ? 15 : 4;
    }

    protected boolean followSquadScout() {
        if (!unit.is(unitForSquadScout)) return false;

        AUnit scout = Alpha.get().squadScout();
        if (scout != null) {
            unitForSquadScout = unit;
            if (scout.distTo(unit) > 1) {
                unitForSquadScout.move(scout, Actions.MOVE_FOLLOW, "FollowScout", true);
            }
            return true;
        }

        return false;
    }

    protected boolean followArmy(boolean shouldFollowItsSquad) {
        if (!shouldFollowItsSquad && !unit.is(unitAssignedToMainSquad)) return false;

        HasPosition goTo = unit.squadCenter();
        if (goTo != null) {
            unitAssignedToMainSquad = unit;
            if (goTo.distTo(unit) > 1) {
                unitAssignedToMainSquad.move(goTo, Actions.MOVE_FOLLOW, "Follow", true);
            }
            return true;
        }

        return false;
    }

    protected boolean detectInvisibleUnitsClosestToBase() {
        if (Select.main() == null) return false;

        if (!unit.equals(Select.ourOfType(unit.type()).first())) return false;

        if (!unit.is(unitForBase)) return false;

        AUnit dangerousInvisibleEnemy = enemyDangerousHiddenUnit();
        if (dangerousInvisibleEnemy != null) {
            unitForBase = unit;
            if (unitForBase.distTo(dangerousInvisibleEnemy) > 0.2) {
                unitForBase.move(dangerousInvisibleEnemy.position(), Actions.MOVE_ENGAGE, "RevealEnemy", true);
            }
            return true;
        }

        return false;
    }

    protected AUnit enemyDangerousHiddenUnit() {
        AUnit invisibleUnit = Select.enemy().effUndetected().combatUnits().nearestTo(Select.main());
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

        return Select.enemy().effUndetected().nearestTo(Select.main());
    }

}
