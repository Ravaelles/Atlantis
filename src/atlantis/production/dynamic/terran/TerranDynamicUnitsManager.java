package atlantis.production.dynamic.terran;

import atlantis.production.AbstractDynamicUnits;


public class TerranDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        TerranDynamicFactoryUnits.handleFactoryProduction();

        TerranDynamicInfantry.ghosts();
        TerranDynamicInfantry.medics();
        TerranDynamicInfantry.marines();
    }

}
