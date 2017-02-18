package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.AGame;
import atlantis.production.AtlantisWorkerProductionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.workers.AWorkerCommander;
import java.util.ArrayList;

public class TerranBuildOrders extends AtlantisBuildOrdersManager {
    
    @Override
    protected String getFilename() {
        return "Terran/1 Fact FE.csv";
    }

    // =========================================================
    
    @Override
    public void produceWorker() {
        AtlantisWorkerProductionManager.produceWorker();
    }

    @Override
    public void produceUnit(AUnitType unitType) {
        AUnitType whatBuildsIt = unitType.getWhatBuildsIt();
        AUnit unitThatWillProduce = Select.ourOneIdle(whatBuildsIt);
        if (unitThatWillProduce != null) {
            unitThatWillProduce.train(unitType);
        } else {
//            System.err.println("Can't find " + whatBuildsIt + " to produce " + unitType);
        }
    }

    @Override
    public ArrayList<AUnitType> produceWhenNoProductionOrders() {
        ArrayList<AUnitType> units = new ArrayList<>();
        
//        System.out.println("Notice: No production orders, auto-produce.");

        if (AWorkerCommander.shouldTrainWorkers(true)) {
            units.add(AtlantisConfig.WORKER);
            units.add(AtlantisConfig.WORKER);
            units.add(AtlantisConfig.WORKER);
            units.add(AtlantisConfig.WORKER);
        }
        
        // =========================================================
        
//        boolean shouldProduceTanks = Select.ourTanks().count() < 30;
//        
//        if (AGame.canAfford(400, 0) && !AGame.canAfford(0, 200)) {
//            units.add(AUnitType.Terran_Vulture);
//            units.add(AUnitType.Terran_Vulture);
//        }
//        
//        if (shouldProduceTanks) {
//            units.add(AUnitType.Terran_Siege_Tank_Tank_Mode);
//            units.add(AUnitType.Terran_Siege_Tank_Tank_Mode);
//        }

//        if (Select.ourBuildings().ofType(AUnitType.Terran_Academy).count() == 0) {
//            units.add(AUnitType.Terran_Marine);
//        } else {
//            int marines = Select.our().countUnitsOfType(AUnitType.Terran_Marine);
//            int medics = Select.our().countUnitsOfType(AUnitType.Terran_Medic);
//
//            if ((double) marines / medics < 3) {
//                units.add(AUnitType.Terran_Marine);
//            } else {
//                units.add(AUnitType.Terran_Medic);
//                units.add(AUnitType.Terran_Marine);
//            }
//        }
        
        return units;
    }

}
