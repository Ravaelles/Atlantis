package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.game.race.EnemyRace;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.protoss.dragoon.DragoonInsteadZealot;
import atlantis.information.generic.OurArmy;
import atlantis.information.generic.ProtossArmyComposition;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceZealot {
    public static boolean zealot() {
        if (!A.hasMinerals(100)) return false;

        int freeGateways = Count.freeGateways();
        if (freeGateways == 0) return false;

        if (A.s >= 60 * 6 && !A.hasMinerals(550) && Enemy.zerg()) return false;

        if (Enemy.zerg() && Count.zealotsWithUnfinished() <= minZealotsToHave()) return produceZealot();

        if (freeGateways >= 2 && A.hasMinerals(700) && A.supplyUsed() <= 180) return produceZealot();
        if (DragoonInsteadZealot.dragoonInsteadOfZealot()) return false;

        if (Count.zealots() >= 2 && !A.hasMinerals(ReservedResources.minerals() + 100)) return false;

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
            && Count.zealotsWithUnfinished() < minZealotsToHave();
    }

    private static int minZealotsToHave() {
        if (Enemy.zerg()) return 1 + (OurArmy.strength() <= 60 ? Count.zealotsWithUnfinished() : 0);

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
        );
    }
}
