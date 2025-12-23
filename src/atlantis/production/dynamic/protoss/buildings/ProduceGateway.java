package atlantis.production.dynamic.protoss.buildings;

import atlantis.cherryvis.CV;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.Army;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.Strategy;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.dynamic.protoss.prioritize.PrioritizeCyberneticsOverZealotsAndGateways;
import atlantis.production.dynamic.protoss.prioritize.PrioritizeGatewaysVsProtoss;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceGateway {
    private static int unfinishedGateways;
    private static int freeGateways;
    private static int existingGateways;
    private static int minerals;
    private static int bases;

    public static boolean produce() {
        minerals = A.minerals();
        if (minerals <= 105) return false;

        freeGateways = Count.freeGateways();
        if (freeGateways >= 1 && minerals <= 1000) return false;

        if (minerals <= (50 + 150 * Count.inProduction(Protoss_Gateway))) return false;
        if (minerals <= 334 && CountInQueue.count(Protoss_Templar_Archives, 3) > 0) return false;

        if (
            A.supplyFree() <= 2
                && A.minerals() < 300
                && ConstructionRequests.countPendingOfType(Protoss_Pylon) == 0
        ) return false;

        if (
            minerals <= 320 && minerals + 200 <= ReservedResources.minerals()
                && !Queue.get().readyToProduceOrders().buildings().isEmpty()
        ) return false;

//        A.errPrintln(A.minSec() + " GATEWAY CHECK");

        existingGateways = Count.gatewaysWithUnfinished();

        if (freeGateways > 0 && minerals <= 360) return false;

        if (minerals >= 700 && existingGateways <= 6 && freeGateways <= 2) return produceGateway() && log("700");
        if (minerals >= 500 && freeGateways <= 1 && existingGateways <= 7) return produceGateway() && log("500");
        if (freeGateways <= 2 && minerals >= 1200) return produceGateway() && log("1200");

        if (
            minerals <= 210 && freeGateways >= 1
                && ConstructionRequests.countPendingOfType(Protoss_Gateway) >= 1
        ) return false;

        if (A.supplyUsed() <= 25) {
            if (freeGateways > 0 && minerals <= 180) return false;
        }
        else {
            if (minerals >= 220 && freeGateways <= 0 && existingGateways <= 3) return produceGateway() && log("220");
            if (minerals >= 210 && freeGateways <= 1 && existingGateways <= 5) return produceGateway() && log("210");
        }

        if (
            minerals >= 190 && freeGateways <= 0 && PrioritizeGatewaysVsProtoss.shouldPrioritizeOverExpanding()
        ) return produceGateway() && log("190");

        if (freeGateways > 0 && minerals <= 180 && existingGateways <= 3) return false;
        if (existingGateways > 0 && A.supplyUsed() <= 17 && minerals <= 500) return false;

//        if (
//            minerals >= 300
//                && existingGateways <= 3
//                && !A.hasFreeSupply(1)
//                && ConstructionRequests.notStartedOfType(Protoss_Gateway).isEmpty()
//        ) return produceGateway();

        if (A.supplyFree() >= 2 || minerals <= 130) {
            if (freeGateways >= 2 && existingGateways >= 4) return false;
//            if (freeGateways >= 1 && existingGateways >= 3 && !A.hasMinerals(550)) return false;
        }

        if (freeGateways <= 1 && existingGateways <= 4 && A.hasMinerals(150)) return produceGateway() && log("150");
        if (freeGateways == 0 && existingGateways <= 2 && Army.strength() <= 130 && A.hasMinerals(230)) return produceGateway();;

        if (ConstructionRequests.notFinished().size() >= (A.hasMinerals(550) ? 2 : 1) && !A.hasMinerals(600)) return false;

        bases = Count.basesWithUnfinished();
        unfinishedGateways = Count.inProductionOrInQueue(Protoss_Gateway);

        if (minerals >= 520 && freeGateways <= 2 && unfinishedGateways <= 2) return produceGateway() && log("520");

        if (existingGateways <= 2 && A.supplyUsed() >= 35 && A.hasMinerals(240)) return produceGateway() && log("240");
        if (minerals >= 550 && (existingGateways <= 8 || freeGateways <= 1)) return produceGateway() && log("550");
        if (minerals >= 250 && existingGateways <= 4 && freeGateways == 0) return produceGateway() && log("250");
        if (
            minerals >= 275 && existingGateways <= 4 && freeGateways <= 1 && !ProtossShouldExpand.shouldExpand()
        ) return produceGateway() && log("275");

        if (A.s <= 60 * 6 && existingGateways >= 2 && freeGateways >= 1 && !A.hasMinerals(220)) return false;
        if (freeGateways >= 2 && !A.hasMinerals(600) && existingGateways <= 12) return false;

        if (
            freeGateways >= 1 && ConstructionRequests.countNotFinishedOfType(type()) >= A.minerals() * 180
        ) return false;
        if (PrioritizeCyberneticsOverZealotsAndGateways.prioritizeCybernetics()) return false;

        if (ConstructionRequests.countNotFinishedOfType(type()) >= (A.hasMinerals(500) ? 2 : 1)) return false;

        if (freeGateways <= 3 && existingGateways <= 14 && A.hasMinerals(650)) return produceGateway() && log("650");
        if (bases >= 2 && minerals >= 215) {
            if (freeGateways <= 0 && existingGateways <= 6) return produceGateway() && log("0-6");
            if (minerals >= 280 && existingGateways <= 6) return produceGateway() && log("280-6");
            if (existingGateways <= 1 && minerals >= 170) return produceGateway() && log("170-1");
        }

        if (againstZergProduceEarly()) return produceGateway();
        if (againstProtossProduceEarly()) return produceGateway();

        if (minerals <= 330 && enemyGoesHiddenUnitsAndNotPrepared()) return false;

        if (allowWhenStrategyIsExpansion()) return produceGateway();

        if (minerals >= 660 && freeGateways <= 2) return produceGateway();
        if (minerals >= 460 && existingGateways <= 8 && freeGateways <= 2) return produceGateway() && log("460");
        if (minerals >= 200 && existingGateways <= 5 && Count.bases() >= 2) return produceGateway() && log("200-5");
        if (minerals >= 210 && freeGateways <= 0) return produceGateway() && log("210-0");

        if (
            !A.hasMinerals(186)
                && freeGateways >= 1
                && existingGateways >= (2 + 3 * bases)
                && bases >= 2
//                && A.isInRange(50, ReservedResources.minerals(), 350)
        ) return false;

        if (!A.hasMinerals(290) && ConstructionRequests.hasNotStarted(Protoss_Cybernetics_Core)) return false;

        if (minerals >= 210 && freeGateways <= 1) return produceGateway() && log("210-1");
        if (minerals >= 490 && freeGateways <= 2) return produceGateway() && log("490-2");

        // =========================================================

        if (!A.hasMinerals(260) && PrioritizeCyberneticsOverZealotsAndGateways.prioritizeCybernetics()) return false;

        if (freeGateways >= 2) return false;
        if (ReservedResources.minerals() >= 250 && !A.hasMinerals(230)) return false;

//        allGateways = existingGateways + unfinishedGateways;

        if (freeGateways >= 2) {
//            if (tooManyGatewaysForNow()) return false;
//            if (allGateways <= 5 * Count.bases()) return produceGateway();
//            return produceGateway();
            return false;
        }

//        if (minerals < 205 && existingGateways >= 3) return false;
        if (existingGateways >= 4 && freeGateways > 0 && !A.hasMinerals(600)) return false;

//        if (unfinishedGateways >= 3 || !Enemy.zerg()) {
//            if (unfinishedGateways >= 1 && !A.hasMinerals(500)) return false;
        if (unfinishedGateways >= 2 && !A.hasMinerals(550)) return false;
//        }

        if (continuousGatewayProduction()) return produceGateway() && log("Continuous");

        return false;
    }

    private static boolean log(String reason) {
        CV.log(
            "Gateway:" + reason
                + " (ex:" + Count.gateways()
                + ",fr:" + Count.freeGateways()
                + ",min:" + A.minerals() + ")"
        );
        return true;
    }

    private static AUnitType type() {
        return Protoss_Gateway;
    }

    private static boolean allowWhenStrategyIsExpansion() {
        if (!Strategy.get().isExpansion()) return false;

        return A.hasMinerals(234) && existingGateways <= 1 && Count.basesWithUnfinished() >= 2;
    }

    private static boolean againstZergProduceEarly() {
        if (!Enemy.zerg()) return false;

        if (
            existingGateways <= 4
                && freeGateways == 0
                && A.hasMinerals(210)
                && Count.basesWithUnfinished() >= 2
        ) return true && log(" Zerg early 4");

        return existingGateways <= 2
            && freeGateways == 0
            && A.hasMinerals(184)
            && Army.strength() <= 180
            && log(" Zerg early 2");
    }

    private static boolean againstProtossProduceEarly() {
        if (!Enemy.protoss()) return false;

        if (
            existingGateways <= 4
                && freeGateways == 0
                && A.hasMinerals(210)
                && Count.basesWithUnfinished() >= 2
        ) return true && log(" Protoss early 4");

        return false;
    }

    private static boolean enemyGoesHiddenUnitsAndNotPrepared() {
        if (
            EnemyStrategy.get().isGoingHiddenUnits()
                || (Enemy.protoss() && EnemyInfo.goesTemplarArchives())
        ) {
            if (
                Count.ourWithUnfinished(Protoss_Photon_Cannon) <= 1
                    && Count.ourWithUnfinished(Protoss_Observer) <= 1
            ) {
                return !A.canAfford(310, 34);
            }

            if (Count.existingOrInProduction(Protoss_Forge) == 0) {
                CancelNotStartedBases.cancelNotStartedOrEarlyBases(null, "HiddenEnemiesPressure");
            }
        }

        return false;
    }

    private static boolean continuousGatewayProduction() {
        return freeGateways <= 1 && (A.hasMinerals(570) || A.canAffordWithReserved(170, 0));
    }

    private static boolean produceGateway() {
        if (freeGateways > 0 && !A.hasMinerals(500)) return false;
        if (CountInQueue.countNotFinished(type()) >= (1 + A.minerals() / 330)) return false;

        ProductionOrder order = AddToQueue.withStandardPriority(Protoss_Gateway);

        if (order != null) order.setMinSupply(A.supplyUsed());
//        else {
//            A.errPrintln(A.minSec() + ", Failed to produce Gateway " + existingGateways + " / " + freeGateways
//                + " / " + unfinishedGateways + " / min:" + A.minerals() + " / " + QueueLastStatus.status());
//            return false;
//        }

        return order != null;
    }

    private static boolean tooManyGatewaysForNow() {
//        int enough = Enemy.zerg() ? 5 : 4;
        int enough = Enemy.zerg() ? 5 : 6;

        return !A.hasMinerals(450)
            && Count.gatewaysWithUnfinished() >= enough
            && (!Have.roboticsFacility() || Count.basesWithUnfinished() <= 1);
    }
}
