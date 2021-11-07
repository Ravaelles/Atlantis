package atlantis.production.dynamic.terran;

import atlantis.AGame;
import atlantis.information.TerranArmyComposition;
import atlantis.production.AbstractDynamicUnits;
import atlantis.strategy.OurStrategy;
import atlantis.strategy.decisions.OurDecisions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class TerranDynamicInfantry extends TerranDynamicUnitsManager {

    protected static void medics() {
        if (!OurStrategy.get().goingBio() || Count.ofType(AUnitType.Terran_Academy) == 0) {
            return;
        }

        if (TerranArmyComposition.medicsToInfantry() <= 0.23) {
            if (Select.ourOfType(AUnitType.Terran_Barracks).free().isNotEmpty()) {
                AbstractDynamicUnits.addToQueue(AUnitType.Terran_Medic);
            }
        }
    }

    protected static void marines() {
        if (!OurDecisions.shouldBuildBio()) {
            return;
        }

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
                AbstractDynamicUnits.addToQueue(AUnitType.Terran_Marine);
                return;
            }
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
}
