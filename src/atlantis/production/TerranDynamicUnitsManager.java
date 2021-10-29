package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class TerranDynamicUnitsManager {

    public static void update() {
        trainMarinesForBunkersIfNeeded();
        handleFactoryProduction();
    }

    // =========================================================

    private static void handleFactoryProduction() {
        for (AUnit factory : Select.ourOfType(AUnitType.Terran_Factory).listUnits()) {
            if (!factory.isTrainingAnyUnit()) {
                if (AGame.canAffordWithReserved(270, 110)) {
                    factory.train(AUnitType.Terran_Vulture);
                }
            }
        }
    }

    private static void trainMarinesForBunkersIfNeeded() {
        int bunkers = Select.countOurOfTypeIncludingUnfinished(AUnitType.Terran_Bunker);
        if (bunkers > 0) {
            int marines = Select.countOurOfType(AUnitType.Terran_Marine);
            int shouldHaveMarines = defineOptimalNumberOfMarines(bunkers);

            // Force at least one marine per bunker
            if (marines < shouldHaveMarines) {
                for (int i = 0; i < shouldHaveMarines - marines; i++) {
                    AUnit idleBarrack = Select.ourOneIdle(AUnitType.Terran_Barracks);
                    if (idleBarrack != null) {
                        idleBarrack.train(AUnitType.Terran_Marine);
                    }
                    else {
                        break;
                    }
                }
            }
        }
    }

    private static int defineOptimalNumberOfMarines(int bunkers) {
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
