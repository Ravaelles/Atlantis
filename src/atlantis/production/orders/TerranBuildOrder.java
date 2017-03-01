package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.production.ADynamicWorkerProductionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

public class TerranBuildOrder extends ABuildOrder {
    
    public static final TerranBuildOrder TERRAN_1_FE = new TerranBuildOrder("1 Fact FE");
    public static final TerranBuildOrder TERRAN_1_Base_Vultures = new TerranBuildOrder("1 Base Vultures");
    public static final TerranBuildOrder TERRAN_Nada_2_Fact = new TerranBuildOrder("Nada 2 Fact");

    // =========================================================
    
    private TerranBuildOrder(String relativePath) {
        super("Terran/" + relativePath);
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
