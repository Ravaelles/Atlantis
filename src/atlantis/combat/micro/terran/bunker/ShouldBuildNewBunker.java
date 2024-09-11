package atlantis.combat.micro.terran.bunker;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ShouldBuildNewBunker {
    protected final int existingAndPlannedBunkers;
    private AStrategy enemyStrategy;

    public ShouldBuildNewBunker() {
        existingAndPlannedBunkers = Count.withPlanned(Terran_Bunker);
        enemyStrategy = EnemyStrategy.get();
    }

    // =========================================================

    public boolean shouldBuildNew() {
        if (enemyIsRushing()) return wantsToBuildABunker();
        if (enemyIsZerg()) return wantsToBuildABunker();

        if (enemyNotStrongEnoughForUsToBuildABunker()) return false;

        return !weHaveEnoughBunkers() && wantsToBuildABunker();
    }

    // =========================================================

    private boolean wantsToBuildABunker() {
        return CountInQueue.count(Terran_Bunker) == 0;
    }

    private boolean enemyIsZerg() {
        return Enemy.zerg() && (
            (A.supplyUsed() >= 14 && enemyStrategy.isUnknown())
                ||
                enemyStrategy.isRushOrCheese()
        );
    }

    public boolean weHaveEnoughBunkers() {
        return existingAndPlannedBunkers >= Count.bases();
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
            && !OurStrategy.get().isRushOrCheese()
            && !enemyStrategy.isRushOrCheese();
    }

}