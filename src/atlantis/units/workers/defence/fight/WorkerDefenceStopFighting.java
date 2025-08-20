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

    @Override
    public boolean applies() {
        if (unit.isGatheringResources()) return false;

        if (unit.distToBase() >= 12) return true;

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

    @Override
    protected Manager handle() {
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
