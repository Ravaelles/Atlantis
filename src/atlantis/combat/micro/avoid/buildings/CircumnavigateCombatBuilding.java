package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class CircumnavigateCombatBuilding extends Manager {
    public CircumnavigateCombatBuilding(AUnit unit) {
        super(unit);
    }

    /**
     * Try to go around a defensive building by not running back-and-forth, but sideways.
     */
    public Manager handle(AUnit combatBuilding) {
        if (unit.isGroundUnit() && A.seconds() >= 600) {
            return null;
        }

        APosition goTo = findPositionAround(combatBuilding);

        APainter.paintLine(unit, goTo, Color.Orange);
        APainter.paintCircle(goTo, 4, Color.Orange);

        if (unit.move(goTo, Actions.MOVE_MACRO, "Around!", false)) {
            unit.setTooltip("SmartAround", false);
            return usedManager(this);
        }

        return null;
    }

    public APosition findPositionAround(AUnit combatBuilding) {
        int roamingRange = unit.isAir() ? 7 : 4;

        APosition raw = unit.translateTilesTowards(-roamingRange - 0.4, combatBuilding);

        // Now we randomize the position to implement "circling around" the combat building
        return raw.randomizePosition(roamingRange);
    }
}
