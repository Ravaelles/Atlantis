package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.AGame;
import atlantis.production.ADynamicWorkerProductionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.workers.AWorkerCommander;
import java.util.ArrayList;

public class TerranBuildOrder extends ABuildOrderManager {
    
    public static final TerranBuildOrder TERRAN_1_FE = new TerranBuildOrder();
    
    // =========================================================
    
    @Override
    protected String getFilename() {
        return "Terran/1 Fact FE.txt";
    }

    // =========================================================
    
    @Override
    public void produceWorker() {
        ADynamicWorkerProductionManager.produceWorker();
    }

    @Override
    public void produceUnit(AUnitType unitType) {
        AUnitType whatBuildsIt = unitType.getWhatBuildsIt();
        AUnit unitThatWillProduce = Select.ourOneIdle(whatBuildsIt);
        if (unitThatWillProduce != null) {
            unitThatWillProduce.train(unitType);
        } 
//        else {
//            System.err.println("Can't find " + whatBuildsIt + " to produce " + unitType);
//        }
    }

    @Override
    public ArrayList<AUnitType> produceWhenNoProductionOrders() {
        ArrayList<AUnitType> units = new ArrayList<>();
        
        if (ADynamicWorkerProductionManager.shouldTrainWorkers(true)) {
            units.add(AtlantisConfig.WORKER);
            units.add(AtlantisConfig.WORKER);
        }
        
        // =========================================================
        
        units.add(AUnitType.Terran_Siege_Tank_Tank_Mode);
        units.add(AUnitType.Terran_Goliath);
        units.add(AUnitType.Terran_Wraith);
        units.add(AUnitType.Terran_Marine);
        units.add(AUnitType.Terran_Siege_Tank_Tank_Mode);
        
        return units;
    }

}
