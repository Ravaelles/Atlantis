package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.game.race.EnemyRace;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.protoss.dragoon.ProduceDragoonInsteadZealot;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.ProtossStrategies;
import atlantis.information.strategy.Strategy;
import atlantis.production.dynamic.protoss.prioritize.PrioritizeCyberneticsOverZealotsAndGateways;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceZealot {
    public static int producedCount = 0;
    private static int zealots;
    private static int freeGateways;

    public static boolean zealot() {
        if (!A.hasMinerals(100)) return false;

        freeGateways = Count.freeGateways();
        if (freeGateways == 0) return false;

        zealots = Count.zealotsWithUnfinished();
        if (notEnoughZealots()) return produceZealot();

        if (PrioritizeCyberneticsOverZealotsAndGateways.prioritizeCybernetics()) return false;

        if (earlyGameZealots(freeGateways)) return produceZealot();

        if (freeGateways >= 1 && A.hasMinerals(560) && !A.hasGas(200) && Count.zealots() <= 12) return produceZealot();
        if (freeGateways >= 2 && A.hasMinerals(530) && A.supplyUsed() <= 185) return produceZealot();

        if (!Have.assimilator() && A.hasMinerals(250) && zealots <= 7) return produceZealot();
        if (produceLateGameWithLotsOfMinerals()) return produceZealot();

        if (zealots <= 1 && !A.hasGas(17) && A.hasMinerals(300)) return produceZealot();
//        if (Enemy.zerg() && zealots < minZealotsToHave()) return produceZealot();

        if (A.minerals() <= 700 && enoughZealots()) return false;

        if (A.s >= 60 * 6 && !A.hasMinerals(550) && Enemy.zerg()) return false;

        if (freeGateways >= 2 && A.hasMinerals(700) && A.supplyUsed() <= 180) return produceZealot();
        if (ProduceDragoonInsteadZealot.dragoonInsteadOfZealot()) return false;

        if (zealots >= 2 && !A.hasMinerals(ReservedResources.minerals() + 100)) return false;

        if (freeGateways >= 2 && A.hasMinerals(700) && A.hasFreeSupply(4)) return produceZealot();

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

        if (earlyGameVsZerg()) return produceZealot();

        return false;
    }

    private static boolean earlyGameVsZerg() {
        if (!Enemy.zerg()) return false;
        if (freeGateways <= 0) return false;
        if (A.hasMinerals(150)) return false;
        if (!Have.existingUnfinishedOrPlanned(Protoss_Cybernetics_Core)) return false;

        return EnemyUnits.combatUnits() >= (freeGateways >= 2 ? 7 : 8);
    }

    private static boolean earlyGameRushZealots() {
        return A.seconds() <= 350 && Strategy.get().isRushOrCheese() && A.hasMinerals(100);
    }

    private static boolean earlyGameDefenceVsProtoss() {
        return Enemy.protoss()
            && A.seconds() <= 410
//            && (AGame.killsLossesResourceBalance() < 0 || Army.relative() <= 90)
            && zealots < minZealotsToHave();
    }

    private static int minZealotsToHave() {
        if (Enemy.zerg()) return 4 + (Army.strength() <= 60 ? zealots : 0);

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
        return (Army.strength() >= 118 || zealots >= 2)
            && zealots >= minZealots();
    }

    public static boolean notEnoughZealots() {
        if (
            zealots >= 1
                && A.s >= 350
                && Army.strength() >= 126
                && EnemyInfo.enemyUnitInMainBase() == null
        ) return false;

        if (Count.zealots() < minZealots()) return true;

        if (
            Enemy.zerg()
                && Army.strength() <= 95
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
        if (A.s <= 60 * 6.5 && Strategy.is(ProtossStrategies.PROTOSS_Zealot_into_Goon)) return 6;

        if (Enemy.terran()) return minZealotsVsTerran();
        if (Enemy.protoss()) return minZealotsVsProtoss();
        return minZealotsVsZerg();
    }

    private static double minZealotsVsProtoss() {
        boolean core = Have.cyberneticsCore();

        if (core) {
            if (A.hasGas(25)) return 0;
            if (A.hasGas(1) && !A.hasMinerals(201)) return 0;
        }
        else {
            if (!A.hasMinerals(201) && Have.cyberneticsCoreWithUnfinished() && !A.hasGas(1)) return 0;
        }

        if (
            Strategy.get().isGoingTech()
                && (core || CountInQueue.count(Protoss_Cybernetics_Core, 3) > 0)
                && !A.hasMinerals(500)
        ) return 1;

        if (A.hasGas(1) && Count.cannons() >= 2) return 2;

        if (core && A.hasGas(30)) return 0;
//        if (!core && A.hasMinerals(300)) return 5;

        double fromZealots = EnemyUnits.discovered().zealots().count() * 0.6;

        if (A.hasGas(50)) fromZealots = A.inRange(2, fromZealots, 6);

        return A.inRange(2, fromZealots, 9);
    }

    private static double minZealotsVsTerran() {
        return 1;
    }

    private static double minZealotsVsZerg() {
        if (A.hasGas(1) && Count.cannons() >= 2) return 2;

        boolean core = Have.cyberneticsCore();

        if (core) {
            int dragoons = Count.dragoons();
            if (A.hasGas(50) && dragoons <= 1) return 0;
            if (dragoons >= 1) return Math.max(1, dragoons / 5);
        }
        if (!core && A.hasMinerals(300)) return 5;

        double fromLings = EnemyUnits.discovered().zerglings().count() * 0.32;

        if (A.hasGas(130)) fromLings = A.inRange(2, fromLings, 6);

        return A.inRange(4, fromLings, 9);
    }
}
