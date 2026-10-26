package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.workers.gather.GatherResources;

public class WorkerDefenceStopFighting extends Manager {
    public WorkerDefenceStopFighting(AUnit unit) {
        super(unit);
    }

    /**
     * Owner's line (unsupported pursuit, live report: workers trade far
     * beyond the army, alone, and die): no friendly building inside this,
     * no friendly ground combat unit inside
     * {@link #NO_SUPPORT_WITHIN_TILES}, more than the shared 10 tiles
     * from the main - a worker fighting or pursuing there withdraws
     * instead of donating. Measured radii, pinned by
     * {@code WorkerFarFightTest}.
     */
    private static final int NO_BUILDING_WITHIN_TILES = 5;

    /** Companion radius to the one above: support that cannot join is none. */
    private static final int NO_SUPPORT_WITHIN_TILES = 3;

    @Override
    public boolean applies() {
        if (!unit.isAttacking() && !isPursuingEnemy(unit)) return false;
        if (unit.isGatheringResources()) return false;

        // Owner's hard line for trading blows: no worker fights at 10+
        // tiles from base, however it got there (the entry gates read the
        // decision frame, the pursuit that follows does not re-ask them).
        // Same named reason as the attack gate (CONVENTIONS §31).
        // Pursuit below gets the narrower conjunction instead: walking to
        // a supported fight is joining it, not donating.
        if (unit.isAttacking()) {
            if (WorkerStrayedFromBase.tooFarFromHome(unit)) return true;
        }
        else if (unsupportedPursuit(unit)) return true;

        if (unit.isAttacking() || unit.action().isAttacking() || unit.lastAttackFrameLessThanAgo(60)) {
            if (Enemy.protoss()) {
                if (unit.hp() >= 35) return false;

                if (unit.hp() <= 18) return true;

                if (
                    unit.hp() <= 34
                        && (A.supplyUsed() >= 40 || unit.hasCooldown() || unit.meleeEnemiesNearCount(3) >= 2)
                ) return true;
            }

//            return WorkerDoNotFight.doNotFight(unit);
            return unit.hp() <= 21 || unit.friendsNear().buildings().countInRadius(10, unit) == 0;
        }

        return false;
    }

    /**
     * True when the worker is walking to a live enemy, not yet swinging:
     * attack order in flight with a target worth naming. Gatherers walk
     * to minerals (neutral, never the enemy); builders never reach this
     * chain - so an enemy target plus motion is pursuit, not errand.
     */
    private static boolean isPursuingEnemy(AUnit unit) {
        if (!unit.isMoving()) return false;

        AUnit target = unit.target();

        return target != null && target.isEnemy() && target.hp() > 0;
    }

    /**
     * The unsupported-pursuit conjunction (owner's numbers): more than
     * the shared 10 tiles from the main, no friendly building within
     * {@link #NO_BUILDING_WITHIN_TILES}, no friendly ground combat unit
     * within {@link #NO_SUPPORT_WITHIN_TILES}. Each clause owns false:
     * near the base the swarm rules decide, near buildings the cannon
     * paths decide, with support the fight is joined - only all three
     * together mean the walk is a donation.
     */
    private static boolean unsupportedPursuit(AUnit unit) {
        if (!WorkerStrayedFromBase.tooFarFromHome(unit)) return false;
        if (unit.friendsNear().buildings().countInRadius(NO_BUILDING_WITHIN_TILES, unit) > 0) return false;

        return unit.friendsNear().combatUnits().groundUnits()
            .countInRadius(NO_SUPPORT_WITHIN_TILES, unit) == 0;
    }

    @Override
    protected Manager handle() {
        if (WorkerStrayedFromBase.tooFarFromHome(unit)) {
            unit.setTooltipTactical(WorkerDefenceFightCombatUnits.TOO_FAR_FROM_BASE);
        }

//        if (unit.hp() >= 19) {
//            if ((new GatherResources(unit)).forceHandle() != null) return usedManager(this);
//        }

//        System.out.println("StahpWorka " + unit + " hp:" + unit.hp() + " / healthy:" +unit.isHealthy() );

        AUnit enemy = unit.nearestEnemy();

        if (enemy == null) {
            if ((new GatherResources(unit)).forceHandle() != null) {
                return usedManager(this, "StahpAndGather");
            }

            unit.moveToNearestBase(Actions.MOVE_SAFETY, null);

            return null;
        }

        if (enemy.distToLessThan(unit, 3.3)) {
            if (unit.runOrMoveAway(enemy, 4)) return usedManager(this, "StahpWorka");
        }

        if (unit.moveToNearestBase(Actions.MOVE_SAFETY, null)) {
            return usedManager(this, "StahpFightingWorker");
        }

        return null;
    }
}
