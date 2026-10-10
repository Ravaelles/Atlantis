package atlantis.units.workers.defence.fight;

import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
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
        // FIRST, before every other rule: a wounded worker with an enemy on it is
        // not "not helping", it is leaving. The lines below used to catch it
        // first - `hp <= 17`, `hp <= minHp()` and the two Protoss health rules all
        // end in f(), which sends the worker back to gathering. So a Probe at 5 hp
        // was told to mine, and WorkerDefenceRun (next in the defence chain)
        // never got a turn (owner's log: GatherResources/WorkerHelpCombatUnitsFight
        // alternating with no run, WorkerDefenceRun absent entirely).
        if (isWoundedAndInDanger()) return false;

        // Construction owns assigned builders. Returning f() here can move them
        // to safety or back to gathering; because this is a Manager chain, that
        // non-null result stops the chain before BuilderManager gets to issue its
        // move/build command. Builders do not join the worker-help fight path.
        if (unit.isBuilder()) return false;

        if (unit.hp() <= 21) return f();
        if (unit.hp() <= minHp()) return f();
        AUnit base = unit.friendsNear().bases().nearestTo(unit);
        if (base == null) return f();
        if (unit.distTo(base) >= 9) return f();
        if (Enemy.protoss() && unit.hp() <= 20) return f();
        if (Enemy.protoss() && unit.hp() <= 36 && unit.lastUnderAttackLessThanAgo(150)) return f();
        if (unit.lastActionLessThanAgo(10)) return f();
        if (unit.enemiesNear().combatUnits().empty()) return f();
        if (unit.friendsNear().combatUnits().nonBuildings().countInRadius(10, unit) == 0) return f();

        // In a base defence the modulo skip must not idle a fifth of the
        // workforce (B-19: the skipped probes never supported the cannon).
        if (!BaseUnderAttack.check() && unit.id() % 5 <= 1) return false;

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
    /**
     * A worker that is hurt and has an enemy nearby must be allowed to leave,
     * before any other rule in {@link #applies()} can send it back to mining.
     *
     * <p>A Zealot kills a 40 hp Probe in three swings, so the window in which
     * running helps is small: the radius here (6 tiles) is wider than the
     * attack range on purpose, because a Zealot closes that distance in a
     * second and the worker should already be moving.
     */
    private boolean isWoundedAndInDanger() {
        if (unit.hp() > 34) return false;

        boolean enemyClose = unit.enemiesNear().combatUnits().inRadius(6, unit).notEmpty();
        boolean wasShot = unit.lastUnderAttackLessThanAgo(30 * 3);

        return enemyClose || wasShot;
    }

    private boolean f() {
        if (unit.isGatheringMinerals() || unit.isGatheringGas()) return false;

//        WorkerDefenceRun workerDefenceRun = new WorkerDefenceRun(unit);
//        if (workerDefenceRun.invokedFrom(this)) {
//            usedManager(workerDefenceRun);
//            return false;
//        }

        // Unit-level rather than the combat-side ProtossAvoidEnemies manager: this
        // package is inside atlantis.units and must not reach into atlantis.combat
        // (ArchUnit rule, and the layering is real - a worker manager should not
        // depend on combat micro). moveToSafety is the same action at unit level.
        if (unit.moveToSafety(atlantis.units.actions.Actions.MOVE_AVOID)) {
            usedManager(this);
            return false;
        }

        GatherResources manager = new GatherResources(unit);
        if (manager.forceHandle() != null) {
            usedManager(this);
            return false;
        }

//        if (unit.action().isAttacking() || unit.lastCommandWasAttack()) {
//            GatherResources manager = new GatherResources(unit);
//            if (manager.forceHandle() != null) {
//                usedManager(this);
//                return false;
//            }
//
//            AUnit mineral = Select.minerals().nearestToMain();
//            if (mineral != null && unit.enemiesNear().combatUnits().nearestToDist(mineral) >= 5) {
//                unit.gather(mineral);
////                ErrorLog.printMaxOncePerMinute("Shouldn't happen: WorkerHelpCombatUnitsFight Fix minerals");
//                return false;
//            }
//
//            ErrorLog.printMaxOncePerMinute("Shouldn't happen: WorkerHelpCombatUnitsFight 2Base");
//
//            WorkerDefenceRun workerDefenceRun = new WorkerDefenceRun(unit);
//            if (workerDefenceRun.invokedFrom(this)) {
//                usedManager(workerDefenceRun);
//                return false;
//            }
//
//            unit.moveToSafety(Actions.MOVE_AVOID);
//        }

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
        if (unit.hp() <= 17) return null;

        if (processFightEnemyCombatUnits(unit)) {
            unit.setTooltip("HelpCombatUnits");
            return usedManager(this);
        }

        return null;
    }
}



