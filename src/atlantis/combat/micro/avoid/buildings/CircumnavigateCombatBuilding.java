package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
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

        return true;
    }

    /**
     * Try to go around a defensive building by not running back-and-forth, but sideways.
     */
    @Override
    protected Manager handle() {
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
        int roamingRange = unit.isAir() ? 9 : 4;

        APosition raw = unit.translateTilesTowards(-roamingRange - 1, combatBuilding);

        // Now we randomize the position to implement "circling around" the combat building
        return raw.randomizePosition(roamingRange);
    }
}
