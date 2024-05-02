package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.information.decisions.protoss.dragoon.DragoonInsteadZealot;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;
import static atlantis.units.AUnitType.Protoss_Gateway;

public class ProduceGateway {

    private static int allGateways;
    private static int unfinishedGateways;
    private static int freeGateways;
    private static int existingGateways;
    private static int minerals;

    public static boolean produce() {
        minerals = A.minerals();

        if (minerals <= 100) return false;

        existingGateways = Count.gateways();

        if (!A.hasMinerals(350) && ConstructionRequests.hasNotStarted(Protoss_Cybernetics_Core)) return false;

        if (
            !A.hasMinerals(176)
                && existingGateways >= 4
                && A.isInRange(50, ReservedResources.minerals(), 350)
        ) return false;

        freeGateways = Count.freeGateways();

        if (minerals >= 470 && freeGateways <= 1) return produceGateway();

        // =========================================================

        if (!A.hasMinerals(260) && prioritizeCybernetics()) return false;

        if (freeGateways >= 2) return false;
        if (ReservedResources.minerals() >= 250 && !A.hasMinerals(230)) return false;

        unfinishedGateways = Count.inProductionOrInQueue(Protoss_Gateway);
        allGateways = existingGateways + unfinishedGateways;

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

    private static boolean prioritizeCybernetics() {
        return !Have.cyberneticsCore()
            && !A.hasMinerals(300)
            && Have.notEvenPlanned(Protoss_Cybernetics_Core)
            && DragoonInsteadZealot.dragoonInsteadOfZealot();
    }

    private static boolean continuousGatewayProduction() {
        return freeGateways <= 1 && (A.hasMinerals(570) || A.canAffordWithReserved(170, 0));
    }

    private static boolean produceGateway() {
        AddToQueue.withStandardPriority(Protoss_Gateway);
//        A.printStackTrace("Produce Gateway");
        return true;
    }

    private static boolean tooManyGatewaysForNow() {
//        int enough = Enemy.zerg() ? 5 : 4;
        int enough = Enemy.zerg() ? 5 : 6;

        return !A.hasMinerals(450)
            && Count.gatewaysWithUnfinished() >= enough
            && (!Have.roboticsFacility() || Count.basesWithUnfinished() <= 1);
    }
}
