package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ADynamicUnitProductionManager {

    public static void update() {
        ADynamicWorkerProductionManager.handleDynamicWorkerProduction();
        
        if (AGame.playsAsTerran()) {
            handleFactoryProductionIfNeeded();
        }
        else if (AGame.playsAsProtoss()) {
            handleScarabProductionIfNeeded();
        }
    }
    
    // === Terran ========================================
    
    private static void handleFactoryProductionIfNeeded() {
        for (AUnit factory : Select.ourOfType(AUnitType.Terran_Factory).listUnits()) {
            if (!factory.isTrainingAnyUnit()) {
                boolean cantAffordTankButCanAffordVulture = AGame.hasMinerals(250)
                        && !AGame.hasGas(70);
                
                if (cantAffordTankButCanAffordVulture) {
                    factory.train(AUnitType.Terran_Vulture);
                }
            }
        }
    }
    
    // === Protoss ========================================

    private static void handleScarabProductionIfNeeded() {
        List<AUnit> reavers = Select.ourOfType(AUnitType.Protoss_Reaver).listUnits();
        for (AUnit reaver : reavers) {
            if (reaver.getScarabCount() < 3 && !reaver.isTrainingAnyUnit()) {
                reaver.train(AUnitType.Protoss_Scarab);
            }
        }
    }
    
}
