package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.information.TerranArmyComposition;
import atlantis.production.AbstractDynamicUnits;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.A;


public class TerranDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        medics();
        marines();
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
                addToQueue(AUnitType.Terran_Goliath);
                return;
            }
        }

        if (Count.tanks() <= 0.4 * Count.vultures()) {
            addToQueue(AUnitType.Terran_Siege_Tank_Tank_Mode);
        }
        else {
            addToQueue(AUnitType.Terran_Vulture);
        }
    }

    // === Infantry ======================================================

    private static void medics() {
        if (!OurStrategy.get().goingBio() || Count.ofType(AUnitType.Terran_Academy) == 0) {
            return;
        }

        if (TerranArmyComposition.medicsToInfantry() <= 0.23) {
            if (Select.ourOfType(AUnitType.Terran_Barracks).free().isNotEmpty()) {
                addToQueue(AUnitType.Terran_Medic);
            }
        }
    }

    private static void marines() {
        if (!AGame.canAffordWithReserved(80, 0)) {
            return;
        }

        if (Count.ofType(AUnitType.Terran_Academy) >= 1 && Count.medics() <= 2) {
            return;
        }

        if (OurStrategy.get().goingBio()) {
            if (Count.ofType(AUnitType.Terran_Barracks) == 0) {
                return;
            }

//        if (Enemy.zerg() && Count.marines() == 0) {
            if (Select.ourOfType(AUnitType.Terran_Barracks).free().isNotEmpty()) {
                addToQueue(AUnitType.Terran_Marine);
                return;
            }
        }

        trainMarinesForBunkersIfNeeded();
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
                        addToQueue(AUnitType.Terran_Marine);
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

    // =========================================================

}
