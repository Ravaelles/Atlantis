package atlantis.units.workers.defence.fight;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.BaseUnderAttack;
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
        // A WOUNDED worker must never be excluded here - it must be free to RUN.
        // The old order put two exclusions before this point that together meant
        // "a hurt worker gathering is told to keep gathering":
        //
        //   hp <= minHp()            -> not eligible to help
        //   isGatheringResources()
        //     && (recently attacked || shield wounded) -> not eligible either
        //
        // so a Probe being chewed on by a Zealot while mining fell through both
        // and stood there (owner report, 2026-10-07: "even when wounded they do
        // not flee, they just die without a reaction").
        //
        // A wounded worker in danger is handed back to the chain instead: this
        // manager returns false, and the defence chain reaches
        // WorkerDefenceRun next, which is where running belongs. (Calling the
        // run manager from here would add a fight -> run dependency the
        // architecture forbids: CONVENTIONS §5, ArchitectureBoundaryTest.)
        if (isWoundedAndInDanger()) return false;

        // In a base defence the modulo skip must not idle a fifth of the
        // workforce (B-19: the skipped probes never supported the cannon).
        if (!BaseUnderAttack.check() && unit.id() % 5 <= 1) return false;
        if (unit.enemiesNear().combatUnits().empty()) return f();
        if (unit.hp() <= minHp()) return f();
        if (unit.isBuilder()) return f();

        // NOTE: `lastActionLessThanAgo(GATHER_MINERALS)` used to gate this out.
        // Gathering is what a worker does by default, so "recently gathered"
        // excluded almost every worker almost always - and the log showed the
        // resulting flip-flop (GatherResources -> WorkerHelpCombatUnitsFight ->
        // TrackEnemyEarlyScout -> GatherResources, forever, until the Probe
        // died). A worker mining NEXT TO AN ENEMY is the case this whole class
        // exists for, so mining is not a reason to stand down.

        // The run lockout applies in the field only; at home a worker that
        // fled is wanted back in the defence (B-19).
        if (!BaseUnderAttack.check() && !unit.lastStartedRunningMoreThanAgo(30 * 10)) return f();

        AUnit base = unit.friendsNear().bases().nearestTo(unit);
        if (base == null) return f();
        if (unit.distTo(base) >= 12) return f();

        // The old `isGatheringResources() && (recently attacked || shield
        // wounded) -> return f()` lived here. It is gone: it told exactly the
        // workers in the most danger to keep mining.

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

    /**
     * A worker that is hurt and has an enemy in reach must be allowed to run.
     * This is the early-game survival rule: one Probe lost at 2 minutes costs
     * more than any amount of mined minerals.
     */
    private boolean isWoundedAndInDanger() {
        if (unit.hp() > 34) return false;

        return unit.enemiesNear().combatUnits().canAttack(unit, 3.5).notEmpty()
            || unit.lastUnderAttackLessThanAgo(30 * 3);
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



