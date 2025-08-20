package atlantis.protoss.reaver.reaver_with_shuttle;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.player.Enemy;
import atlantis.map.position.HasPosition;
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
//        if (unit.shieldHealthy()) return false;

        AUnit reaver = unit.loadedUnitsGet(Protoss_Reaver);
        if (reaver == null) {
            ErrorLog.printMaxOncePerMinute("Shuttle without reaver");
            return false;
        }

        defineReaverEnemies(reaver);

        if (unit.lastStartedRunningLessThanAgo((int) (30 * (3 + unit.woundPercent() / 30)))) return true;
        if (reaverEnemies.combatBuildingsAntiLand().inRadius(9, unit).notEmpty()) return true;

        return reaverEnemies.notEmpty();
    }

    private void defineReaverEnemies(AUnit reaver) {
        reaverEnemies = reaver.enemiesNear().canAttack(unit, safetyMarginBonus(reaver));

        if (reaverEnemies.empty()) reaverEnemies = reaver.enemiesNear().air().havingAntiAirWeapon().inRadius(10, unit);

        if (Enemy.zerg()) {
            if (reaverEnemies.empty()) reaverEnemies =
                reaver.enemiesNear().mutalisks().inRadius(7 + (unit.woundPercent() / 25.0), unit);
        }
    }

    private double safetyMarginBonus(AUnit reaver) {
        return 1.7
            + reaver.woundPercent() / 23.0
            + unit.woundPercent() / 23.0;
    }

    @Override
    public Manager handle() {
        if (runTowardsAlphaCenter()) {
            return usedManager(this, "TowardsAlpha");
        }
        else if (unit.moveAwayFrom(reaverEnemies.center(), 8, Actions.MOVE_AVOID, null)) {
            return usedManager(this, "ShuttleMoveAway");
        }
        else if (unit.moveAwayFrom(reaverEnemies.center(), 14, Actions.MOVE_AVOID, null)) {
            return usedManager(this, "ShuttleMoveAway");
        }

        AUnit runFrom = reaverEnemies.nearestTo(unit);
        if (runFrom != null && unit.runningManager().runFrom(runFrom, 8, Actions.MOVE_AVOID, false)) {
            return usedManager(this, "ShuttleAvoid");
        }
//        else {
//            if (unit.runningManager().runFrom(reaverEnemies.center(), 3, Actions.MOVE_AVOID, false)) {
//                return usedManager(this, "ShuttleAvoidFallback");
//            }
//        }

        return null;
    }

    private boolean runTowardsAlphaCenter() {
        HasPosition center = Alpha.get().center();
        if (center == null) return false;

        double dist = unit.distTo(center);
        if (dist <= 4 && unit.enemiesThatCanAttackMe(2).air().atLeast(1)) return true;

        if (dist <= 3 || dist >= 13) return false;

        return unit.move(center, Actions.MOVE_AVOID, "TowardsAlpha");
    }
}
