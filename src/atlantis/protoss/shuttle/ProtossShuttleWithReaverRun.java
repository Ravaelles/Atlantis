package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;

import static atlantis.units.AUnitType.Protoss_Reaver;

public class ProtossShuttleWithReaverRun extends Manager {
    private Selection reaverEnemies;

    public ProtossShuttleWithReaverRun(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.shieldHealthy()) return false;

        AUnit reaver = unit.loadedUnitsGet(Protoss_Reaver);
        if (reaver == null) {
            ErrorLog.printMaxOncePerMinute("Shuttle without reaver");
            return false;
        }

        reaverEnemies = reaver.enemiesNear().canAttack(unit, 3);

        if (reaverEnemies.combatBuildingsAntiLand().inRadius(9, unit).notEmpty()) return true;

        return reaverEnemies.notEmpty();
    }

    @Override
    public Manager handle() {
        if (unit.runningManager().runFrom(reaverEnemies.center(), 4, Actions.MOVE_AVOID, false)) {
            return usedManager(this, "ShuttleAvoid");
        }

        return null;
    }
}
