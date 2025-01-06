package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
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
    private AUnit centerUnit;

    public ProtossShuttleWithReaverEngage(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.loadedUnits().isEmpty()) return false;

        reaver = unit.loadedUnitsGet(Protoss_Reaver);
        if (reaver == null) return false;

        target = defineTarget();
        if (target == null) return false;

        dist = unit.distTo(target);
//        System.err.println("dist = " + dist);

        return true;
    }

    private AUnit defineTarget() {
        centerUnit = Alpha.get().leader();
        if (centerUnit == null) centerUnit = unit;

        targets = centerUnit.enemiesNear().groundUnits().realUnitsAndCombatBuildings().notDeadMan();
//        System.err.println("targets = " + targets.size());

        if (targets.notEmpty()) return targets.nearestTo(reaver);
        else return EnemyUnits.discovered().groundUnits().realUnitsAndCombatBuildings().notDeadMan().nearestTo(reaver);
    }

    @Override
    public Manager handle() {
        if (unit.distTo(centerUnit) >= 6 || (A.s % 8 <= 1)) {
            if (unit.move(centerUnit, Actions.MOVE_ENGAGE, "ShuttleEngageLeader")) return usedManager(this);
        }

        AUnit cb = reaver.enemiesNear()
            .combatBuildingsAntiLand()
            .inRadius(7.7, reaver)
            .nearestTo(reaver);

        if (cb != null) {
            reaver.setTooltip("Dont land - CB");
            unit.moveAwayFrom(target, 4, Actions.MOVE_AVOID, "ShuttleAvoidCB");
            return usedManager(this);
        }

        if (dist <= 8.9 + (unit.shotSecondsAgo() >= 5 ? 2 : 0)) {
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

        ARegion region = walkable.region();
        if (region == null || !region.isConnected()) return false;

        return unit.unload(reaver);
    }
}
