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
import atlantis.util.Enemy;

public class TerranDynamicInfantry extends TerranDynamicUnitsManager {

    protected static boolean saveForFactory() {
        if (!A.hasMinerals(200)) {
            if (Count.inQueue(AUnitType.Terran_Factory, 1) > 0) {
                System.out.println("Save for Factory");
                return true;
            }
        }

        return false;
    }

    protected static void medicsOrFirebats() {
        if (!OurDecisions.shouldBuildBio() || Count.ofType(AUnitType.Terran_Academy) == 0) {
            return;
        }

        if (saveForFactory()) {
            return;
        }

        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
        if (barracks.isNotEmpty()) {
            if (Count.medics() >= 4 && Count.ourOfTypeIncludingUnfinished(AUnitType.Terran_Firebat) < minFirebats()) {
                produceUnit(barracks.first(), AUnitType.Terran_Firebat);
                return;
            }

            if (TerranArmyComposition.medicsToInfantryRatioTooLow()) {
                produceUnit(barracks.first(), AUnitType.Terran_Medic);
            }
        }
    }

    private static int minFirebats() {
        if (Enemy.terran()) {
            return 0;
        }
        if (Enemy.protoss()) {
            return Math.max(1, Count.medics() / 5);
        }

        // Zerg
        return (int) Math.max(2, Count.medics() / 4);
    }

    protected static void marines() {
        if (!OurDecisions.shouldBuildBio()) {
//            System.out.println("Marines - A");
            return;
        }

        if (saveForFactory()) {
//            System.out.println("Marines - B");
            return;
        }

        if (Count.ofType(AUnitType.Terran_Barracks) == 0) {
            return;
        }

        if (A.supplyUsed() >= 40 && !AGame.canAffordWithReserved(80, 0)) {
//            System.out.println("Marines - D");
            return;
        }

        if (Count.ourCombatUnits() > 25) {
            if (!AGame.canAffordWithReserved(80, 0)) {
//                System.out.println("Marines - E");
                return;
            }
        }

//        if (
//                A.hasGas(30)
//                        && Count.ofType(AUnitType.Terran_Academy) >= 1
//                        && Count.marines() >= 4
//                        && Count.medics() <= 2
//        ) {
////            System.out.println("Marines - F, gas = " + AGame.gas() + " // " + A.hasGas(30));
////            System.out.println(Atlantis.game().self().getRace() + " // " + Atlantis.game().self().getName());
////            System.out.println(Atlantis.game().self().minerals() + " // " + Atlantis.game().self().gatheredMinerals()+ " // " + Atlantis.game().self().gatheredGas() + " / " + Atlantis.game().self().gatheredGas());
////            System.out.println("-------------------");
////            System.out.println(Atlantis.game().enemy().getRace() + " // " + Atlantis.game().enemy().getName());
////            System.out.println(Atlantis.game().enemy().minerals() + " // " + Atlantis.game().enemy().gatheredGas());
//            return;
//        }

//        if (Enemy.zerg() && Count.marines() == 0) {
        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
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
