package atlantis.units.workers.defence.fight;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import atlantis.architecture.Manager;
import atlantis.units.workers.gather.GatherResources;
import atlantis.units.workers.defence.run.WorkerDefenceRun;
import atlantis.util.log.ErrorLog;

import static atlantis.units.workers.defence.fight.WorkerDefenceFightCombatUnits.processFightEnemyCombatUnits;

public class WorkerHelpCombatUnitsFight extends Manager {
    public WorkerHelpCombatUnitsFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.id() % 5 <= 1) return false;
        if (unit.enemiesNear().combatUnits().empty()) return f();
        if (unit.hp() <= minHp()) return f();
        if (unit.isBuilder()) return f();
        if (unit.lastActionLessThanAgo(30 * 5, Actions.GATHER_MINERALS)) return f();
        if (!unit.lastStartedRunningMoreThanAgo(30 * 10)) return f();

        AUnit base = unit.friendsNear().bases().nearestTo(unit);
        if (base == null) return f();
        if (unit.distTo(base) >= 12) return f();

        if (
            unit.isGatheringResources()
                && (unit.lastUnderAttackLessThanAgo(30 * 10) || unit.shieldWounded())
        ) return f();

        if (woundedAndNoCombatNear()) return f();

        Selection enemies = base.enemiesNear().combatUnits().groundUnits().inRadius(7, base);
        int countEnemies = enemies.count();
        if (countEnemies == 0) return f();

        if (countEnemies <= A.whenEnemyProtossZerg(
            (A.s >= 500 || Count.dragoons() >= 2) ? 1 : 0, 2
        )) return f();

        AUnit enemy = enemies.nearestTo(unit);
        if (enemy != null) {
            if (enemy.enemiesNear().combatUnits().countInRadius(3, unit) == 0) {
                return f();
            }
        }

        return true;
    }

    private boolean woundedAndNoCombatNear() {
        return unit.woundHp() >= 9
            && unit.friendsNear().combatUnits().countInRadius(10, unit) == 0;
    }

    private boolean f() {
        if (unit.action().isAttacking() || unit.lastCommandWasAttack()) {
            GatherResources manager = new GatherResources(unit);
            if (manager.forceHandle() != null) {
                usedManager(this);
                return true;
            }

            AUnit mineral = Select.minerals().nearestToMain();
            if (mineral != null && unit.enemiesNear().combatUnits().nearestToDist(mineral) >= 5) {
                unit.gather(mineral);
//                ErrorLog.printMaxOncePerMinute("Shouldn't happen: WorkerHelpCombatUnitsFight Fix minerals");
                return false;
            }

            ErrorLog.printMaxOncePerMinute("Shouldn't happen: WorkerHelpCombatUnitsFight 2Base");

            WorkerDefenceRun workerDefenceRun = new WorkerDefenceRun(unit);
            if (workerDefenceRun.invokedFrom(this)) {
                usedManager(workerDefenceRun);
                return false;
            }

            unit.moveToSafety(Actions.MOVE_AVOID);
        }

        return false;
    }

    private int minHp() {
        if (Enemy.protoss()) {
            return 36;
//                + (unit.meleeEnemiesNearCount(3.5) > 0 ? 18 : 0);
        }

        return 26;
    }

    @Override
    public Manager handle() {
        if (processFightEnemyCombatUnits(unit)) {
            unit.setTooltip("HelpCombatUnits");
            return usedManager(this);
        }

        return null;
    }
}



