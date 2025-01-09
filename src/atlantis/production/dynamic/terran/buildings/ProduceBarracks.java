package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.Terran_Barracks;

public class ProduceBarracks {
    private static int existingBarracks;
    private static int freeBarracks;
    private static int bases;
    private static int unfinishedBarracks;

    public static boolean barracks() {
        if (!A.hasMinerals(110)) return false;

        freeBarracks = Count.freeBarracks();
        if (freeBarracks >= 1) return false;

        existingBarracks = Count.withPlanned(Terran_Barracks);
        unfinishedBarracks = ConstructionRequests.countNotFinishedOfType(Terran_Barracks);

        if (unfinishedBarracks >= 1 && !A.hasMinerals(230)) return false;

        if (Strategy.get().isRushOrCheese()) {
            if (A.hasMinerals(134)) {
//                System.err.println("Barracks for RUSH - " + A.supplyUsed() + "/" + A.supplyTotal() + ", min:" + A.minerals());
                return produce();
            }
        }

        if (prioritizeFirstFactory()) return false;
        if (existingBarracks >= 3 && (freeBarracks > 0 || !A.hasMinerals(550))) return false;
        if (unfinishedBarracks >= 1 && !A.hasMinerals(190)) return false;

        if (freeBarracks == 0 && unfinishedBarracks <= 1 && A.hasMinerals(130)) return produce();

        bases = Count.basesWithUnfinished();

        if (existingBarracks <= 4 && A.hasMinerals(126) && Strategy.get().goingBio()) return produce();
        else if (A.hasMinerals(550)) return produce();

//        if (existingBarracks <= 8 && A.hasMinerals(800) && A.hasFreeSupply(4)) return produce();
//        if (existingBarracks >= 3 && !A.hasMinerals(800)) return false;
        if (existingBarracks >= 2 && (!A.hasMinerals(450) || Enemy.terran())) return false;
        if (freeBarracks == 0 && A.hasMinerals(350) && (
            Count.basesWithUnfinished() >= 2 || A.seconds() >= 800
        )) return produce();

//        if (existingBarracks <= 6 && A.hasMinerals(800) && A.hasFreeSupply(6)) return produce();

//        if (Select.ourFree(Terran_Barracks).size() > 0)
//            System.err.println("@ " + A.now() + " - FREE BARRACKS " + Select.ourFree(Terran_Barracks).size());

        if (!A.hasMinerals(630)) {
//            if (existingBarracks >= 3) {
//                return false;
//            }

            //        if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) {
            if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) return false;

            if (existingBarracks >= 3 && A.supplyUsed() <= 40) return false;

            if (existingBarracks >= 4 && A.supplyUsed() <= 70) return false;
        }

        if (Count.inProductionOrInQueue(Terran_Barracks) > 0) return false;

        if (A.canAffordWithReserved(150, 0) || A.hasMinerals(650)) {
//            return DynamicCommanderHelpers.buildIfAllBusyButCanAfford(Terran_Barracks, 0, 0);
            return produce();
        }

        return false;
    }

    private static boolean prioritizeFirstFactory() {
        return existingBarracks >= 2
            && !Have.factoryWithUnfinished()
            && (Army.strength() >= 110 || freeBarracks > 0)
            && !A.hasMinerals(700);
    }

    private static boolean produce() {
        return AddToQueue.withStandardPriority(Terran_Barracks) != null;
    }
}
