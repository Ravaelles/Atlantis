package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.Strategy;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ShouldProduceNewBunker {
    protected final int existingAndPlannedBunkers;
    private AStrategy enemyStrategy;

    public ShouldProduceNewBunker() {
        existingAndPlannedBunkers = Count.withPlanned(what());
        enemyStrategy = EnemyStrategy.get();
    }

    private AUnitType what() {

        // =========================================================
        return Terran_Bunker;
    }

    public boolean shouldBuild() {
        if (!Have.barracks()) return false;
        if (Count.inProductionOrInQueue(what()) > 0) return false;

        if (ignore()) return false;

        if (existingAndPlannedBunkers <= 0) {
            if (enemyIsRushing()) return wantsToBuildABunker();
            if (enemyIsZerg()) return wantsToBuildABunker();
        }

        if (enemyNotStrongEnoughForUsToBuildABunker()) return false;

        return !weHaveEnoughBunkers() && wantsToBuildABunker();
    }

    private static boolean ignore() {
        return Strategy.get().isRushOrCheese()
            && (A.s <= 60 * 6 || Army.strength() >= 110);
    }

    // =========================================================

    private boolean wantsToBuildABunker() {
        return CountInQueue.count(what()) == 0;
    }

    private boolean enemyIsZerg() {
        return Enemy.zerg() && (
            (A.supplyUsed() >= 14 && enemyStrategy.isUnknown())
                ||
                enemyStrategy.isRushOrCheese()
        );
    }

    public boolean weHaveEnoughBunkers() {
        return existingAndPlannedBunkers >= Count.basesWithUnfinished();
    }

    public boolean enemyIsRushing() {
        return EnemyInfo.isDoingEarlyGamePush();

//        return existingAndPlannedBunkers < 2 && (
//            (A.supplyUsed() >= 14 && enemyStrategy.isUnknown())
//                || (A.supplyUsed() <= 30 && ArmyStrength.weAreMuchWeaker())
//        );
    }

    public boolean enemyNotStrongEnoughForUsToBuildABunker() {
        if (existingAndPlannedBunkers >= 1) return false;

        return GamePhase.isEarlyGame()
            && EnemyUnits.discovered().combatUnits().atMost(Enemy.zerg() ? 8 : 5)
            && (
            !Strategy.get().isRushOrCheese()
                || Army.strengthWithoutCB() <= 90
                || enemyStrategy.isRushOrCheese()
        );
    }

}