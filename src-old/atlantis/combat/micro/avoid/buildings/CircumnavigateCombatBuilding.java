package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class CircumnavigateCombatBuilding extends Manager {
    private final AUnit combatBuilding;

    public CircumnavigateCombatBuilding(AUnit unit, AUnit combatBuilding) {
        super(unit);
        this.combatBuilding = combatBuilding;
    }

    @Override
    public boolean applies() {
        if (unit.isGroundUnit()) return false;
        if (unit.woundPercent() >= 40) return false;
        if (unit.looksIdle()) return false;
        if (unit.isWraith() && unit.ranRecently(4) && unit.didntShootRecently(7)) return false;

        return true;
    }

    /**
     * Try to go around a defensive building by not running back-and-forth, but sideways.
     */
    @Override
    protected Manager handle() {
        double woundBonus = unit.woundPercent() / 60.0;

        if (unit.enemiesNear().combatUnits().groundUnits().nonBuildings().canAttack(unit, 2.2 + woundBonus).notEmpty()) return null;
        if (unit.enemiesNear().air().canBeAttackedBy(unit, 0).notEmpty()) return null;

        if (unit.distTo(combatBuilding) <= (8 + woundBonus)) {
            if (unit.moveAwayFrom(combatBuilding, 1, Actions.MOVE_AVOID, "JustAvoid")) {
                return usedManager(this);
            }
        }

        if (unit.isMoving() && unit.isAction(Actions.MOVE_MACRO) && unit.distToTargetPosition() >= 3) {
//            System.out.println("...continue: " + unit.distToTargetPosition());
            return usedManager(this);
        }

//        APosition goTo = findPositionAround(combatBuilding);
        int angle = (unit.idIsOdd() ? 1 : -1);
        APosition goTo = PositionAroundBuilding.around(unit, combatBuilding, radiusAroundCb(), angle);

        APainter.paintLine(unit, goTo, Color.Orange);
        APainter.paintCircle(goTo, 4, Color.Orange);

        if (unit.move(goTo, Actions.MOVE_MACRO, "Around!", false)) {
            unit.setTooltip("SmartAround", false);
            return usedManager(this);
        }

        return null;
    }

    private double radiusAroundCb() {
        return 9.5 + (unit.woundPercent() / 66.0);
    }

//    public APosition findPositionAround(AUnit combatBuilding) {
//        int roamingRange = unit.isAir() ? 9 : 4;
//
//        APosition raw = unit.translateTilesTowards(-roamingRange - 1, combatBuilding);
//
//        // Now we randomize the position to implement "circling around" the combat building
//        return raw.randomizePosition(roamingRange);
//    }
}
