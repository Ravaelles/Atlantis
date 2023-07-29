package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.decisions.terran.ShouldMakeTerranBio;
import atlantis.production.dynamic.terran.TerranDynamicInfantry;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProduceMarines {
    public static boolean marines() {
        if (Count.ofType(AUnitType.Terran_Barracks) == 0) {
            return false;
        }

        int marines = Count.marines();

        if (marines <= 1) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Marine, 1);
        }

        if (!A.supplyUsed(160) && A.hasMinerals(800) && !A.hasGas(120)) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Marine, 2);
        }

        if (marines >= 4 && A.supplyUsed(150)) {
            return false;
        }

        if (Enemy.zerg() && A.seconds() >= 300 && marines <= 4) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Marine, 1);
        }

        if (!A.hasMinerals(200) && marines >= 4 && !A.canAffordWithReserved(50, 0)) {
            return false;
        }

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
            return AddToQueue.maxAtATime(AUnitType.Terran_Marine, 1);
        }

        return trainMarinesForBunkersIfNeeded();
    }

    protected static boolean trainMarinesForBunkersIfNeeded() {
        int bunkers = Select.countOurOfTypeWithUnfinished(AUnitType.Terran_Bunker);
        if (bunkers > 0) {
            int marines = Select.countOurOfType(AUnitType.Terran_Marine);
            int shouldHaveMarines = optimalNumberOfMarines(bunkers);

            // Force at least one marine per bunker
            if (marines < shouldHaveMarines) {
                for (int i = 0; i < shouldHaveMarines - marines; i++) {
                    AUnit idleBarrack = Select.ourOneNotTrainingUnits(AUnitType.Terran_Barracks);
                    if (idleBarrack != null) {
//                        return AbstractDynamicUnits.addToQueue(AUnitType.Terran_Marine);
                        return AddToQueue.maxAtATime(AUnitType.Terran_Marine, 4);
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