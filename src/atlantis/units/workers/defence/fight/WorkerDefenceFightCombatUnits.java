package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AliveEnemies;
import atlantis.units.BaseUnderAttack;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.Actions;

public class WorkerDefenceFightCombatUnits extends Manager {

    /** From this many raiders the workers stop swarming (see WorkerDefenceRun). */
    private static final int WORKERS_RUN_THRESHOLD = 3;

    /**
     * Below this local combat eval we are losing badly - and a worker that attacks
     * into a lost fight is not defending anything, it is donating a Probe.
     *
     * <p>
     * {@code eval()} for one of our units is the ratio of what the enemy lost to
     * what we lost in the simulation window, so HIGHER is better and ~1 is an even
     * trade. 0.7 is the owner's line: at or below it the exchange is clearly going
     * against us, and no swarm bonus is worth the unit. (The worker run rule uses
     * 1.5 as "behind"; this is the stricter, do-not-even-try floor.) Owner's
     * request, 2026-10-08: workers were dying en masse charging Zealot armies.
     * </p>
     */
    private static final double EVAL_DO_NOT_FIGHT = 0.7;

    public WorkerDefenceFightCombatUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hp() <= 20) return false;
        if (!unit.enemiesNear().combatUnits().notEmpty()) return false;
        if (unit.distToMain() >= 7) return false;

        // Run lockout applies in the field only: at home a worker that fled is
        // wanted back in the defence (B-19).
        if (!BaseUnderAttack.check() && !unit.lastStartedRunningMoreThanAgo(30 * 10)) return false;

        // Any of our buildings, not just a base within 6 tiles: a worker in the
        // mineral line of an expansion used to be excluded from fighting.
        boolean nearOurBuildings = unit.friendsNear().buildings().countInRadius(8, unit) > 0;
        if (!nearOurBuildings) return false;

        return unit.hp() >= (Enemy.protoss() ? 34 : 26)
            && unit.distToBase() <= 15;
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtectScvBusyConstructing::new,
        };
    }

    @Override
    protected Manager handle() {
        if (handleFightEnemyCombatUnits(unit)) return usedManager(this);

        return null;
    }

    private boolean handleFightEnemyCombatUnits(AUnit worker) {
//        if (Enemy.protoss() && worker.hp() <= 38) return false;
//        if (Enemy.protoss() && worker.enemiesNear().combatUnits().inRadius(2.6, unit).atLeast(2)) return false;
//        if (worker.hp() <= 20) return false;

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.hp());

//        if (We.protoss() && worker.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).isNotEmpty()) {
//            return attackNearestEnemy(worker);
//        }

        if (againstProtossDontFightWhenStrongAndTooWounded(worker)) return false;

        int workers = Count.workers();

        // Field harassment is spread across workers by id; at home every hand is
        // needed (B-19). The 3-worker case must not lose a third of the defence.
        boolean holdGround = BaseUnderAttack.check();
        if (!holdGround && workers >= 6) {
            if (workers <= 9 && unit.id() % 3 == 0) return false;
            if (workers >= 16 && unit.isWounded() && (unit.id() % 5 <= 1 || unit.hp() <= 30)) return false;
        }

        if (Select.enemyCombatUnits().ofType(
            AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Zerg_Lurker,
            AUnitType.Zerg_Ultralisk,
            AUnitType.Protoss_Archon,
            AUnitType.Protoss_Dark_Templar,
            AUnitType.Protoss_Reaver
        ).inRadius(8, worker).count() >= 1) {
            return false;
        }

        if (processFightEnemyCombatUnits(unit)) {
            return true;
        }

        return false;
    }

    public static boolean processFightEnemyCombatUnits(AUnit unit) {
        // Do not attack into a fight we are losing. This is the last gate before a
        // worker issues an attack, and both callers (WorkerDefenceFightCombatUnits
        // and WorkerHelpCombatUnitsFight) go through here, so one check covers both
        // of the paths the owner saw in the death log
        // (WorkerHelpCombatUnitsFight / WorkerDefenceFightCombatUnits alternating
        // while a Probe died to Zealots).
        //
        // Owner's rule (2026-10-08): a worker must not fight at eval <= 0.7. The
        // count floor is part of the same rule, not an exception to it: a Probe's
        // eval is a RATIO of what each side lost, and probes lose to a Zealot
        // one-for-one, so "three probes around one Zealot" also reads low (measured
        // 0.145 in the stub world) even though it is a fight we win and the swarm
        // exists to take it. eval alone therefore cannot tell "losing badly" from
        // "individually weaker but more numerous"; the raider count can, and the two
        // together are exactly "only fight when we are not both behind in the trade
        // AND outnumbered".
        //
        // hasEnemyForEval() keeps the reading meaningful: with nothing to simulate,
        // eval() is NO_ENEMY_IN_REACH (9874), far above the floor.
        if (
            unit.hasEnemyForEval()
                && unit.eval() <= EVAL_DO_NOT_FIGHT
                && outnumberedByNearbyRaiders(unit)
        ) {
            unit.setTooltipTactical("TooBadEval");
            return false;
        }

        Selection potentialEnemies = potentialEnemies(unit);
        AUnit enemy = potentialEnemies.nearestTo(unit);

        // A melee worker (a Probe attacks at range 1) cannot reach a Zealot that
        // is still 2 tiles away, so `canBeAttackedBy` finds nothing and the
        // worker never moves - which is exactly "a lone Zealot kills the mineral
        // line while nobody reacts". When the swarm is on, walk into range and
        // the attack follows next frame.
        if (enemy == null) {
            AUnit raider = nearestSwarmableRaider(unit);
            if (raider == null) return false;

            unit.setTooltipTactical("Swarm!");
            return unit.move(raider, Actions.MOVE_ATTACK, "SwarmMove");
        }

        if (shouldSwarm(enemy)) {
            unit.setTooltipTactical("Swarm!");
            return unit.attackUnit(enemy);
        }

        // Fallback: an enemy raiding deep in our territory is attacked even when
        // swarming does not apply.
        if (enemy.enemiesNear().bases().countInRadius(12, enemy) > 0) {
            unit.setTooltipTactical("FurMotherland!");
            return unit.attackUnit(enemy);
        }

        return false;
    }

    /**
     * True when the raiders around this worker outnumber the workers around them -
     * i.e. when a low eval really does mean "we are losing", rather than "each of
     * us is weaker than one Zealot but there are more of us".
     */
    private static boolean outnumberedByNearbyRaiders(AUnit worker) {
        int raiders = worker.enemiesNear().combatUnits().groundUnits().countInRadius(6, worker);
        int workers = Select.ourWorkers().inRadius(6, worker).count();

        return raiders > workers;
    }

    /** The nearest raider worth walking towards, if the swarm is on. */
    private static AUnit nearestSwarmableRaider(AUnit worker) {
        Selection raiders = worker.enemiesNear().combatUnits().groundUnits().inRadius(6, worker);

        for (AUnit raider : raiders.list()) {
            if (shouldSwarm(raider)) return raider;
        }

        return null;
    }

    /**
     * True when the nearby enemy is worth swarming: few enough raiders that the
     * workers win, and enough workers around to finish it. Three raiders is the
     * owner's line - from there the workers flee when the fight is also going
     * badly (see WorkerDefenceRun.runFromZealots).
     */
    private static boolean shouldSwarm(AUnit enemy) {
        if (!enemy.isGroundUnit()) return false;

        // Not worth walking into: a heavy unit kills workers faster than they
        // can surround it. Zealots and Zerglings - the raiders this rule exists
        // for - are well under this.
        if (enemy.hp() > 200) return false;

        int raiders = enemy.enemiesNear().combatUnits().countInRadius(9, enemy);
        if (raiders >= WORKERS_RUN_THRESHOLD) return false;

        // Our workers, not the enemy's: friendsNear() answers from the asking
        // unit's side, so on an ENEMY it returns the enemy's own friends.
        int workersAround = Select.ourWorkers().inRadius(9, enemy).count();
        return workersAround >= 2;
    }

    private static Selection potentialEnemies(AUnit worker) {
        return AliveEnemies.get()
            .canBeAttackedBy(worker, 10);
    }

    private static boolean againstProtossDontFightWhenStrongAndTooWounded(AUnit worker) {
        return Enemy.protoss()
            && worker.hp() <= 33
//            && Count.ourCombatUnits() >= 5
            && worker.meleeEnemiesNearCount(2) == 0;
    }
}
