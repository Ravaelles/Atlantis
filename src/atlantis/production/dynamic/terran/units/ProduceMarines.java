package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.terran.ShouldMakeTerranBio;
import atlantis.production.dynamic.terran.TerranDynamicInfantry;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Terran_Barracks;
import static atlantis.units.AUnitType.Terran_Marine;

public class ProduceMarines {
    public static boolean marines() {
        int marines = Count.marines();

        if (CanProduceInfantry.canProduceInfantry(marines)) {
            return AddToQueue.maxAtATime(Terran_Marine, 4) != null;
        }

        if (Count.ofType(AUnitType.Terran_Barracks) == 0) return false;
//        if (Count.inProductionOrInQueue(Terran_Marine) >= 3 && !A.hasMinerals(400)) return false;
//        if (CountInQueue.countInfantry() >= 4 && Select.free(Terran_Barracks).notEmpty()) return false;
        if (Select.ourFree(Terran_Barracks).empty()) return false;

        if (Enemy.terran() && (marines >= 4 && !A.hasMinerals(700 + 100 * marines))) return false;

        if (inRelationToTanks(marines)) return false;

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
        return AddToQueue.maxAtATime(Terran_Marine, 3) != null;
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
