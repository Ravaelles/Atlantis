package atlantis.combat.micro.avoid.buildings;

import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import bwapi.Color;

public class CircumnavigateCombatBuilding extends Manager {

    public CircumnavigateCombatBuilding(AUnit unit) {
        super(unit);
    }

    /**
     * Try to go around a defensive building by not running back-and-forth, but sideways.
     */
    public  Manager handleAround(AUnit combatBuilding) {
        APosition goTo = findPositionAround(combatBuilding);

        APainter.paintLine(unit, goTo, Color.Orange);
        APainter.paintCircle(goTo, 4, Color.Orange);

        if (unit.move(goTo, Actions.MOVE_MACRO, "Around!", false)) {
            unit.setTooltip("SmartAround", false);
            return usedManager(this);
        }
        
        return null;
    }

    public  APosition findPositionAround(AUnit combatBuilding) {
        int roamingRange = 3;

        APosition raw = unit.translateTilesTowards(-roamingRange - 0.2, combatBuilding);

        // Now we randomize the position to implement "circling around" the combat building
        return raw.randomizePosition(roamingRange);
    }
}
