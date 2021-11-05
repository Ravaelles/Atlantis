package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.EnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;


public class TerranDynamicUnitsManager {

    public static void update() {
        trainMarinesForBunkersIfNeeded();
        handleFactoryProduction();
    }

    // =========================================================

    private static void handleFactoryProduction() {
        if (!AGame.canAffordWithReserved(150, 100)) {
            return;
        }

        for (AUnit factory : Select.ourOfType(AUnitType.Terran_Factory).listUnits()) {
            if (!factory.isTrainingAnyUnit()) {
                requestFactoryUnit(factory);
            }
        }
    }

    private static void requestFactoryUnit(AUnit factory) {
        if (EnemyStrategy.get().isAirUnits()) {
            if (AGame.canAffordWithReserved(150, 100)) {
                AddToQueue.addWithStandardPriority(AUnitType.Terran_Goliath);
                return;
            }
        }

        if (Count.tanks() <= 0.4 * Count.vultures()) {
            AddToQueue.addWithStandardPriority(AUnitType.Terran_Siege_Tank_Tank_Mode);
        }
        else {
            AddToQueue.addWithStandardPriority(AUnitType.Terran_Vulture);
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
                    AUnit idleBarrack = Select.ourOneNotTrainingUnits(AUnitType.Terran_Barracks);
                    if (idleBarrack != null) {
                        AddToQueue.addWithStandardPriority(AUnitType.Terran_Marine);
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
