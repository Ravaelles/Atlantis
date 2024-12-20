package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

import static atlantis.units.AUnitType.Protoss_Reaver;
import static atlantis.units.AUnitType.Protoss_Shuttle;

public class ProtossShuttleWithReaverEngage extends Manager {

    private Selection targets;
    private AUnit target;
    private double dist;
    private AUnit reaver;

    public ProtossShuttleWithReaverEngage(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        unit.paintCircleFilled(14, Color.Red);

        if (unit.loadedUnits().isEmpty()) return false;

        unit.paintCircleFilled(14, Color.Green);

        reaver = unit.loadedUnitsGet(Protoss_Reaver);
        if (reaver == null) return false;

        targets = unit.enemiesNear().groundUnits().notDeadMan();
//        System.err.println("targets = " + targets.size());

        if (targets.notEmpty()) target = targets.nearestTo(unit);
        else target = EnemyUnits.discovered().groundUnits().notDeadMan().nearestTo(unit);

        if (target == null) return false;


        dist = unit.distTo(target);
//        System.err.println("dist = " + dist);

        return true;
    }

    @Override
    public Manager handle() {
        AUnit cb = reaver.enemiesNear()
            .combatBuildingsAntiLand()
            .inRadius(7.7, reaver)
            .nearestTo(reaver);

        if (cb != null) {
            reaver.setTooltip("Dont land - CB");
            unit.moveAwayFrom(target, 4, Actions.MOVE_AVOID, "ShuttleAvoidCB");
            return usedManager(this);
        }

        if (dist <= 8.2) {
            if (unloadHere()) return usedManager(this, "DELIVERY");
        }
        else {
            if (unit.move(target, Actions.MOVE_ENGAGE, "ShuttleEngage")) return usedManager(this);
        }

        return null;
    }

    private boolean unloadHere() {

        APosition walkable = unit.position().makeWalkable(0);
        if (walkable == null) return false;

        return unit.unload(reaver);
    }
}
