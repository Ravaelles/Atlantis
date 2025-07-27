package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.Army;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.Strategy;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.dynamic.protoss.prioritize.PrioritizeCyberneticsOverZealotsAndGateways;
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

//        A.errPrintln(A.minSec() + " GATEWAY CHECK");

        existingGateways = Count.gatewaysWithUnfinished();
        freeGateways = Count.freeGateways();

        if (ConstructionRequests.notStarted().size() >= (A.hasMinerals(350) ? 2 : 1)) return false;

        if (freeGateways >= 2 && existingGateways >= 4) return false;

        bases = Count.basesWithUnfinished();
        unfinishedGateways = Count.inProductionOrInQueue(Protoss_Gateway);

        if (minerals >= 520 && freeGateways <= 2 && unfinishedGateways <= 2) return produceGateway();

        if (existingGateways <= 2 && A.supplyUsed() >= 35 && A.hasMinerals(240)) return produceGateway();
        if (minerals >= 550 && (existingGateways <= 8 || freeGateways <= 1)) return produceGateway();
        if (minerals >= 250 && existingGateways <= 4 && freeGateways == 0) return produceGateway();
        if (
            minerals >= 275 && existingGateways <= 4 && freeGateways <= 1 && !ProtossShouldExpand.shouldExpand()
        ) return produceGateway();

        if (A.s <= 60 * 6 && existingGateways >= 2 && freeGateways >= 1 && !A.hasMinerals(220)) return false;
        if (freeGateways >= 2 && !A.hasMinerals(600) && existingGateways <= 12) return false;

        if (
            freeGateways >= 1 && ConstructionRequests.countNotFinishedOfType(type()) >= A.minerals() * 180
        ) return false;
        if (PrioritizeCyberneticsOverZealotsAndGateways.prioritizeCybernetics()) return false;

        if (ConstructionRequests.countNotFinishedOfType(type()) >= (A.hasMinerals(500) ? 2 : 1)) return false;

        if (freeGateways <= 3 && existingGateways <= 14 && A.hasMinerals(650)) return produceGateway();
        if (bases >= 2 && minerals >= 215) {
            if (freeGateways <= 0 && existingGateways <= 6) return produceGateway();
            if (minerals >= 280 && existingGateways <= 6) return produceGateway();
            if (existingGateways <= 1 && minerals >= 170) return produceGateway();
        }

        if (againstZergProduceEarly()) return produceGateway();
        if (againstProtossProduceEarly()) return produceGateway();

        if (minerals <= 330 && enemyGoesHiddenUnitsAndNotPrepared()) return false;

        if (allowWhenStrategyIsExpansion()) return produceGateway();

        if (minerals >= 660 && freeGateways <= 2) return produceGateway();
        if (minerals >= 460 && existingGateways <= 8 && freeGateways <= 2) return produceGateway();
        if (minerals >= 200 && existingGateways <= 5 && Count.bases() >= 2) return produceGateway();
        if (minerals >= 210 && freeGateways <= 0) return produceGateway();

        if (
            !A.hasMinerals(186)
                && freeGateways >= 1
                && existingGateways >= (2 + 3 * bases)
                && bases >= 2
//                && A.isInRange(50, ReservedResources.minerals(), 350)
        ) return false;

        if (!A.hasMinerals(290) && ConstructionRequests.hasNotStarted(Protoss_Cybernetics_Core)) return false;

        if (minerals >= 210 && freeGateways <= 1) return produceGateway();
        if (minerals >= 490 && freeGateways <= 2) return produceGateway();

        // =========================================================

        if (!A.hasMinerals(260) && PrioritizeCyberneticsOverZealotsAndGateways.prioritizeCybernetics()) return false;

        if (freeGateways >= 2) return false;
        if (ReservedResources.minerals() >= 250 && !A.hasMinerals(230)) return false;

//        allGateways = existingGateways + unfinishedGateways;

        if (freeGateways >= 2) {
            if (tooManyGatewaysForNow()) return false;
//            if (allGateways <= 5 * Count.bases()) return produceGateway();
            return produceGateway();
        }

//        if (minerals < 205 && existingGateways >= 3) return false;
        if (existingGateways >= 4 && freeGateways > 0 && !A.hasMinerals(600)) return false;

//        if (unfinishedGateways >= 3 || !Enemy.zerg()) {
//            if (unfinishedGateways >= 1 && !A.hasMinerals(500)) return false;
        if (unfinishedGateways >= 2 && !A.hasMinerals(550)) return false;
//        }

        if (continuousGatewayProduction()) return produceGateway();

        return false;
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
        ) return true;

        return existingGateways <= 2
            && freeGateways == 0
            && A.hasMinerals(184)
            && Army.strength() <= 180;
    }

    private static boolean againstProtossProduceEarly() {
        if (!Enemy.protoss()) return false;

        if (
            existingGateways <= 4
                && freeGateways == 0
                && A.hasMinerals(210)
                && Count.basesWithUnfinished() >= 2
        ) return true;

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
