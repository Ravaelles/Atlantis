package atlantis.production.dynamic.terran;

import atlantis.AGame;
import atlantis.information.TerranArmyComposition;
import atlantis.production.AbstractDynamicUnits;
import atlantis.strategy.decisions.OurDecisions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.A;

public class TerranDynamicInfantry extends TerranDynamicUnitsManager {

    protected static boolean dontProduceBio() {
        if (!A.hasMinerals(250)) {
            if (Count.inQueue(AUnitType.Terran_Factory, 1) > 0) {
                return true;
            }
        }

        return false;
    }

    protected static void medics() {
        if (!OurDecisions.shouldBuildBio() || Count.ofType(AUnitType.Terran_Academy) == 0) {
            return;
        }

        if (dontProduceBio()) {
            return;
        }

        if (TerranArmyComposition.medicsToInfantry() <= 0.23) {
            Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
            if (barracks.isNotEmpty()) {
                produceUnit(barracks.first(), AUnitType.Terran_Medic);
//                AbstractDynamicUnits.addToQueue(AUnitType.Terran_Medic);
            }
        }
    }

    protected static void marines() {
        if (!OurDecisions.shouldBuildBio()) {
            return;
        }

        if (dontProduceBio()) {
            return;
        }

        if (Count.ofType(AUnitType.Terran_Barracks) == 0) {
            return;
        }

        if (A.supplyUsed() >= 40 && !AGame.canAffordWithReserved(80, 0)) {
            return;
        }

        if (Count.ourCombatUnits() > 25) {
            if (!AGame.canAffordWithReserved(80, 0)) {
                return;
            }
        }

        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
        if (
                Count.ofType(AUnitType.Terran_Academy) >= 1
                        && Count.marines() >= 4
                        && Count.medics() <= 2
                        && barracks.isNotEmpty()
        ) {
            return;
        }

//        if (Enemy.zerg() && Count.marines() == 0) {
        if (barracks.isNotEmpty()) {
            produceUnit(barracks.first(), AUnitType.Terran_Marine);
//            AbstractDynamicUnits.addToQueue(AUnitType.Terran_Marine);
//            return;
        }

        trainMarinesForBunkersIfNeeded();
    }

    protected static void trainMarinesForBunkersIfNeeded() {
        int bunkers = Select.countOurOfTypeIncludingUnfinished(AUnitType.Terran_Bunker);
        if (bunkers > 0) {
            int marines = Select.countOurOfType(AUnitType.Terran_Marine);
            int shouldHaveMarines = defineOptimalNumberOfMarines(bunkers);

            // Force at least one marine per bunker
            if (marines < shouldHaveMarines) {
                for (int i = 0; i < shouldHaveMarines - marines; i++) {
                    AUnit idleBarrack = Select.ourOneNotTrainingUnits(AUnitType.Terran_Barracks);
                    if (idleBarrack != null) {
                        AbstractDynamicUnits.addToQueue(AUnitType.Terran_Marine);
                    }
                    else {
                        break;
                    }
                }
            }
        }
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
