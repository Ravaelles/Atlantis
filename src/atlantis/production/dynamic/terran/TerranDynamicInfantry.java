package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.terran.ShouldMakeTerranBio;
import atlantis.information.generic.TerranArmyComposition;
import atlantis.information.decisions.Decisions;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class TerranDynamicInfantry extends TerranDynamicUnitsManager {

    private static boolean DEBUG = false;
//    private static boolean DEBUG = true;

    protected static boolean saveForFactory() {
        if (!A.hasMinerals(200)) {
            if (
                    Count.existingOrInProduction(AUnitType.Terran_Factory) == 0
                    && Count.inQueue(AUnitType.Terran_Factory, 2) > 0
            ) {
                return true;
            }
        }

        return false;
    }

    protected static boolean ghosts() {
        if (Enemy.zerg()) {
            return false;
        }

        if (
            Count.ofType(AUnitType.Terran_Covert_Ops) == 0
            && Count.ofType(AUnitType.Terran_Science_Facility) == 0
        ) {
            return false;
        }

        int ghosts = Count.ofType(AUnitType.Terran_Ghost);

        if (ghosts >= 2 && !A.hasGas(150)) {
            return false;
        }

        if (ghosts >= 8 || A.supplyUsed() <= 80 + 5 * ghosts) {
            return false;
        }

        if (ghosts >= 4 && !AGame.canAffordWithReserved(60, 200)) {
            return false;
        }

        if (!A.hasGas(ghosts * 25)) {
            return false;
        }

        return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Ghost);
    }

    protected static boolean medics() {
        if (Count.ofType(AUnitType.Terran_Academy) == 0) {
            return false;
        }

        if (!Decisions.shouldMakeTerranBio()) {
            if (Have.academy() && Count.infantry() >= 4 && Count.medics() <= 0) {
                return false;
            }
        }

        // =========================================================

        if (A.hasGas(25) && Count.medics() == 0 && Count.marines() > 0) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Medic, 2);
        }

        // We have medics, but all of them are depleted from energy
        if (Count.medics() > 0 && Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(30).isEmpty()) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Medic, 4);
        }

        if (saveForFactory()) {
            return false;
        }

        if (!AGame.canAffordWithReserved(60, 30)) {
            return false;
        }

        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
        if (barracks.isNotEmpty()) {

            // Firebats
            if (!Enemy.terran()) {
                if (Count.medics() >= 3 && Count.ourOfTypeWithUnfinished(AUnitType.Terran_Firebat) < minFirebats()) {
                    return AddToQueue.maxAtATime(AUnitType.Terran_Firebat, 2);
                }
            }

            // Medics
            if (TerranArmyComposition.medicsToInfantryRatioTooLow()) {
//                produceUnit(barracks.first(), AUnitType.Terran_Medic);
                return AddToQueue.maxAtATime(AUnitType.Terran_Medic, 4);
            }
        }

        return false;
    }

    private static int minFirebats() {
        if (Enemy.terran()) {
            return 0;
        }
        if (Enemy.protoss()) {
            return Math.max(4, (Count.marines() / 10 + Count.medics() / 5 - 1));
        }

        // Zerg
        return Math.max(4, Count.medics() / 4);
    }

    protected static boolean marines() {
        if (Count.ofType(AUnitType.Terran_Barracks) == 0) {
            return false;
        }

        int marines = Count.marines();

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
            if (DEBUG) System.out.println("Marines - Dont - shouldNOTMakeTerranBio - " + ShouldMakeTerranBio.reason);
            return false;
        }

        if (saveForFactory()) {
            if (DEBUG) System.out.println("Marines - Dont - saveForFactory");
            return false;
        }

        if (A.supplyUsed() >= 32 && !AGame.canAffordWithReserved(80, 0)) {
            if (DEBUG) System.out.println("Marines - Dont - cantNOTAffordWithReserved");
            return false;
        }

        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
        if (barracks.isNotEmpty() && A.hasMinerals(100 + 50 * barracks.count())) {
            return AddToQueue.maxAtATime(AUnitType.Terran_Marine, 1);
//            AbstractDynamicUnits.addToQueue(AUnitType.Terran_Marine);
//            return;
        }

        return trainMarinesForBunkersIfNeeded();
    }

    protected static boolean trainMarinesForBunkersIfNeeded() {
        int bunkers = Select.countOurOfTypeWithUnfinished(AUnitType.Terran_Bunker);
        if (bunkers > 0) {
            int marines = Select.countOurOfType(AUnitType.Terran_Marine);
            int shouldHaveMarines = defineOptimalNumberOfMarines(bunkers);

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

    protected static int defineOptimalNumberOfMarines(int bunkers) {
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

    // =========================================================

    private static void produceUnit(AUnit building, AUnitType type) {
        building.train(type);
    }
}
