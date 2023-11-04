package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.terran.ShouldMakeTerranBio;
import atlantis.production.dynamic.terran.TerranDynamicInfantry;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.log.ConsoleLog;

import static atlantis.units.AUnitType.Terran_Barracks;
import static atlantis.units.AUnitType.Terran_Marine;

public class ProduceMarines {
    //    private static final boolean DEBUG = true;
    private static final boolean DEBUG = false;

    public static boolean marines() {
        if (Count.ofType(AUnitType.Terran_Barracks) == 0) return false;

        int marines = Count.marines();

        if (marines <= 1 && A.hasMinerals(150)) return produceMarine();

        if (A.canAffordWithReserved(55, 0)) return false;

//        if (marines == 0 && A.hasMinerals(100)) {
//            return AddToQueue.maxAtATime(Terran_Marine, 1) != null;
//        }

        if (Select.ourFree(Terran_Barracks).empty()) return false;
        if (Enemy.terran() && (marines >= 4 && !A.hasMinerals(700 + 100 * marines))) return false;
        if (inRelationToTanks(marines)) return false;

        if (ShouldProduceInfantry.shouldProduceInfantry(marines)) {
            return produceMarine();
        }

        if (!A.supplyUsed(160) && A.hasMinerals(800)) {
            return produceMarine();
        }

        if (marines <= 10 && A.hasMinerals(600)) {
            return produceMarine();
        }

        if (marines >= 8 && A.supplyUsed(170) && !A.hasMinerals(800)) return false;

        if (Enemy.zerg() && A.seconds() >= 300 && marines <= 4) {
            return produceMarine();
        }

        if (!A.hasMinerals(200) && marines >= 4 && !A.canAffordWithReserved(50, 0)) return false;

        if (!Decisions.shouldMakeTerranBio()) {
            if (TerranDynamicInfantry.DEBUG)
                System.out.println("Marines - Dont - shouldNOTMakeTerranBio - " + ShouldMakeTerranBio.reason);
            return false;
        }

        if (TerranDynamicInfantry.needToSaveForFactory()) {
            if (TerranDynamicInfantry.DEBUG) System.out.println("Marines - Dont - saveForFactory");
            return false;
        }

        if (A.supplyUsed() >= 32 && !AGame.canAffordWithReserved(80, 0)) {
            if (TerranDynamicInfantry.DEBUG) System.out.println("Marines - Dont - cantNOTAffordWithReserved");
            return false;
        }

        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
        if (barracks.isNotEmpty() && A.canAffordWithReserved(100 + 50 * barracks.count(), 0)) {
            return produceMarine();
        }

        return trainMarinesForBunkersIfNeeded();
    }

    private static boolean produceMarine() {
        if (Select.ourFree(Terran_Barracks).empty()) return false;

        ProductionOrder result = AddToQueue.maxAtATime(Terran_Marine, 3);

        if (DEBUG) ConsoleLog.message(
            "Produce marine (Marines: " + Count.existingOrInProductionOrInQueue(Terran_Marine) + "," +
                "minerals: " + A.minerals() + ", reserved: " + A.reservedMinerals() + ")"
        );

        return result != null;
    }

    private static boolean inRelationToTanks(int marines) {
        int tanks = Count.tanks();

        if (tanks <= 2) {
            if (A.canAfford(450 + marines * 15, 0)) return produceMarine();
        }
        if (tanks >= 5) {
            if (A.canAfford(450 + marines * 15, 0)) return produceMarine();
        }
        if (tanks <= 2 && marines >= 10 && A.hasMinerals(650)) {
            if (Enemy.zerg() && Count.bunkers() >= 1) return produceMarine();
        }

        return false;
    }

    protected static boolean trainMarinesForBunkersIfNeeded() {
        int bunkers = Select.countOurOfTypeWithUnfinished(AUnitType.Terran_Bunker);
        if (bunkers > 0) {
            int marines = Select.countOurOfType(Terran_Marine);
            int shouldHaveMarines = optimalNumberOfMarines(bunkers);

            // Force at least one marine per bunker
            if (marines < shouldHaveMarines) {
                for (int i = 0; i < shouldHaveMarines - marines; i++) {
                    AUnit idleBarrack = Select.ourOneNotTrainingUnits(AUnitType.Terran_Barracks);
                    if (idleBarrack != null) {
//                        return AbstractDynamicUnits.addToQueue(AUnitType.Terran_Marine);
                        return AddToQueue.maxAtATime(Terran_Marine, 4) != null;
                    }
                    else {
                        break;
                    }
                }
            }
        }
        return false;
    }

    public static int optimalNumberOfMarines(int bunkers) {
        if (bunkers <= 0) {
            return 0;
        }
        else if (bunkers <= 1) {
            return 1;
        }
        else {
            return 4 * bunkers;
        }
    }
}
