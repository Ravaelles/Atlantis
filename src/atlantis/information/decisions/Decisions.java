package atlantis.information.decisions;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.dynamic.zerg.units.ProduceZerglings;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;

import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;

import static atlantis.units.AUnitType.*;

public class Decisions {
    protected static Cache<Boolean> cache = new Cache<>();

    // =========================================================

    //    public static boolean wantsToBeAbleToProduceTanksSoon() {
//        return cache.get(
//            "beAbleToProduceTanks",
//            93,
//            () -> {
//                if (
//                    !SoonInQueue.unit(AUnitType.Terran_Siege_Tank_Tank_Mode)
//                        && !SoonInQueue.unit(AUnitType.Terran_Machine_Shop)
////                    !ProductionQueue.isAtTheTopOfQueue(AUnitType.Terran_Siege_Tank_Tank_Mode, 5)
////                        && !ProductionQueue.isAtTheTopOfQueue(AUnitType.Terran_Machine_Shop, 5)
//                ) return false;
//
//                return EnemyInfo.startedWithCombatBuilding && OurStrategy.get().goingBio();
//            }
//        );
//    }

    public static boolean shouldMakeZerglings() {
        return cache.get(
            "shouldMakeTerranBio",
            97,
            () -> ProduceZerglings.zerglings()
        );
    }

    public static boolean isEnemyGoingAirAndWeAreNotPreparedEnough() {
        return cache.get(
            "isEnemyGoingAirAndWeAreNotPreparedEnough",
            89,
            () -> {
                if (EnemyStrategy.get().isAirUnits()) {
                    if (Count.ourStrictlyAntiAir() <= 10 || ArmyStrength.weAreWeaker()) {
                        return true;
                    }
                }
                return false;
            }
        );
    }

    public static boolean buildRoboticsFacility() {
        if (Have.roboticsFacility() || Have.notEvenPlanned(Protoss_Forge)) return false;

        if (EnemyInfo.hasHiddenUnits()) {
//            System.err.println("roboticsFacility because hasHiddenUnits");
            return true;
        }
        if (A.supplyUsed() <= 44 && enemyStrategyIsRushOrCheese()) return false;
        if (A.supplyUsed() <= 46 && Have.cannon()) return false;

//        System.err.println("----- buildRoboticsFacility OK" );
//        System.err.println("EnemyStrategy.get().isRushOrCheese() = " + EnemyStrategy.get().isRushOrCheese());
//        System.err.println("EnemyStrategy.get().isRush() = " + EnemyStrategy.get().isRush());

        return true;
    }

    public static int minZealotsAgainstEnemyRush() {
        if (Enemy.protoss()) return enemyStrategyIsRushOrCheese() ? 4 : 3;
        if (Enemy.terran()) return 1;
        return 5;
    }

    public static boolean needToProduceZealotsNow() {
        int zealots = Count.zealots();

        if (Have.cyberneticsCore() && !A.hasMinerals(175) && A.hasGas(100)) return false;

        // Early game
        if (GamePhase.isEarlyGame()) {
            if (
                enemyStrategyIsRushOrCheese()
                    && zealots < minZealotsAgainstEnemyRush()
            ) {
                return true;
            }

            if (zealots <= 1 || (A.hasMinerals(225) && Have.free(Protoss_Gateway))) {
                return true;
            }
        }

        // Mid + Late game
        else {
            if (zealots <= 1) {
                return true;
            }

            if (AGame.canAffordWithReserved(130, 0)) {
                return true;
            }
        }

        return false;
    }

    protected static boolean enemyStrategyIsRushOrCheese() {
        return EnemyStrategy.get().isRushOrCheese();
    }
}
