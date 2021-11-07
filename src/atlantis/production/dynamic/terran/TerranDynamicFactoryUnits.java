package atlantis.production.dynamic.terran;

import atlantis.AGame;
import atlantis.information.TerranArmyComposition;
import atlantis.production.AbstractDynamicUnits;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class TerranDynamicFactoryUnits extends TerranDynamicUnitsManager {

    protected static void handleFactoryProduction() {
        if (!AGame.canAffordWithReserved(150, 100)) {
            return;
        }

        for (AUnit factory : Select.ourOfType(AUnitType.Terran_Factory).listUnits()) {
            if (!factory.isTrainingAnyUnit()) {
                requestFactoryUnit(factory);
            }
        }
    }

    protected static void requestFactoryUnit(AUnit factory) {
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

}
