package atlantis.production.dynamic.terran;

import atlantis.AGame;
import atlantis.production.AbstractDynamicUnits;
import atlantis.strategy.EnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;


public class TerranDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        TerranDynamicFactoryUnits.handleFactoryProduction();

        TerranDynamicInfantry.medics();
        TerranDynamicInfantry.marines();
    }

}
