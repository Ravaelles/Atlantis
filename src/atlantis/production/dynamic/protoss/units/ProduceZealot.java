package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.game.race.EnemyRace;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.protoss.dragoon.ProduceDragoonInsteadZealot;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceZealot {
    public static int producedCount = 0;
    private static int zealots;
    private static int freeGateways;

    public static boolean zealot() {
        if (!A.hasMinerals(100)) return false;

        freeGateways = Count.freeGateways();
        if (freeGateways == 0) return false;

        if (produceLateGameWithLotsOfMinerals()) return produceZealot();
        zealots = Count.zealotsWithUnfinished();

        if (zealots <= 1 && !A.hasGas(17) && A.hasMinerals(300)) return produceZealot();
//        if (Enemy.zerg() && zealots < minZealotsToHave()) return produceZealot();

        if (notEnoughZealots()) return produceZealot();
        if (A.minerals() <= 700 && enoughZealots()) return false;

        if (A.s >= 60 * 6 && !A.hasMinerals(550) && Enemy.zerg()) return false;

        if (freeGateways >= 2 && A.hasMinerals(700) && A.supplyUsed() <= 180) return produceZealot();
        if (ProduceDragoonInsteadZealot.dragoonInsteadOfZealot()) return false;

        if (zealots >= 2 && !A.hasMinerals(ReservedResources.minerals() + 100)) return false;

        if (freeGateways >= 2 && A.hasMinerals(700) && A.hasFreeSupply(4)) return produceZealot();

        if (earlyGameZealots(freeGateways)) return produceZealot();

        if (A.hasMinerals(550) && freeGateways >= 2) return produceZealot();

//        System.err.println("@ " + A.now() + " - produceZealot?");

        if (A.supplyUsed() >= 50 && !A.hasMinerals(350)) return false;
        if (Count.ourWithUnfinished(Protoss_Zealot) >= 1 && !A.hasMinerals(250)) return false;

//        if (couldProduceDragoonsButHaveLotsOfMineralsAndFreeGatewaySoMakeZealot()) return produceZealot();
        if (Decisions.needToProduceZealotsNow()) return produceZealot();
//        if (BuildOrderSettings.autoProduceZealots()) return produceZealot();
//        if (ProtossArmyComposition.zealotsToDragoonsRatioTooLow()) return produceZealot();
        if (EnemyRace.isEnemyZerg() && Count.ofType(AUnitType.Protoss_Zealot) <= 0) return produceZealot();

        return false;
    }

    private static boolean produceLateGameWithLotsOfMinerals() {
        return freeGateways >= 2 && A.minerals() >= 1100 && A.supplyUsed() <= 196;
    }

    private static boolean earlyGameZealots(int freeGateways) {
        if (earlyGameDefenceVsProtoss()) return produceZealot();
        if (earlyGameRushZealots()) return produceZealot();

        if (!A.hasGas(25) || !Have.cyberneticsCore()) {
            int minMinerals = ReservedResources.minerals() <= 200 ? 175 : 240;
            if (freeGateways >= 2 && A.hasMinerals(minMinerals)) return produceZealot();
        }

        return false;
    }

    private static boolean earlyGameRushZealots() {
        return A.seconds() <= 350 && OurStrategy.get().isRushOrCheese() && A.hasMinerals(100);
    }

    private static boolean earlyGameDefenceVsProtoss() {
        return Enemy.protoss()
            && A.seconds() <= 410
//            && (AGame.killsLossesResourceBalance() < 0 || OurArmy.relative() <= 90)
            && zealots < minZealotsToHave();
    }

    private static int minZealotsToHave() {
        if (Enemy.zerg()) return 4 + (OurArmy.strength() <= 60 ? zealots : 0);

        return 1;
    }

    private static boolean couldProduceDragoonsButHaveLotsOfMineralsAndFreeGatewaySoMakeZealot() {
        return A.hasGas(100)
            && Have.cyberneticsCore()
            && !A.hasMinerals(500)
            && Count.freeGateways() >= 1;
    }

    private static boolean produceZealot() {
        AUnit gateway = GatewayClosestToEnemy.get();
        if (gateway == null) return false;

//        System.err.println("YES< zealot");
        return gateway.train(
            Protoss_Zealot, ForcedDirectProductionOrder.create(Protoss_Zealot)
        ) && increaseProduced();
    }

    private static boolean increaseProduced() {
        producedCount++;
        return true;
    }

    public static boolean enoughZealots() {
        return (OurArmy.strength() >= 118 || zealots >= 2)
            && zealots >= minZealots();
    }

    public static boolean notEnoughZealots() {
        if (
            zealots >= 1
                && A.s >= 350
                && OurArmy.strength() >= 126
                && EnemyInfo.enemyUnitInMainBase() == null
        ) return false;

        if (Count.zealots() < minZealots()) return true;

        if (
            Enemy.zerg()
                && OurArmy.strength() <= 95
                && A.seconds() <= 420
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
//                && Count.zealots() <= Math.max(4, EnemyUnits.discovered().zealots().count() * 0.3)
                && Count.zealots() <= (Count.cannons() == 0 ? 3 : 2)
                && EnemyUnits.discovered().zerglings().atLeast(8)
        ) {
            return true;
        }

        return false;
    }

    public static double minZealots() {
        if (Enemy.terran()) return minZealotsVsTerran();
        return minZealotsVsZergOrProtoss();
    }

    private static double minZealotsVsTerran() {
        return 1;
    }

    private static double minZealotsVsZergOrProtoss() {
        if (A.hasGas(1) && Count.cannons() >= 2) return 2;

        boolean core = Have.cyberneticsCore();

        if (core && A.hasGas(100)) return 0;
        if (!core && A.hasMinerals(300)) return 5;

        double fromLings = EnemyUnits.discovered().zerglings().count() * 0.32;

        if (A.hasGas(130)) fromLings = A.inRange(2, fromLings, 6);

        return A.inRange(4, fromLings, 9);
    }
}
