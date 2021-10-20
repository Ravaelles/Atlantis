package atlantis.production.orders;

import atlantis.AGame;
import atlantis.AGameSpeed;
import atlantis.AtlantisConfig;
import atlantis.production.ADynamicWorkerProductionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.A;

import java.util.ArrayList;

public class ProtossBuildOrder extends ABuildOrder {
    
    public static final ProtossBuildOrder PROTOSS_2_GATE_RANGE_EXPAND = new ProtossBuildOrder("2 Gate Range Expand");
    public static final ProtossBuildOrder PROTOSS_2_GATEWAY_ZEALOT = new ProtossBuildOrder("2 Gateway Zealot");
    
    // =========================================================
    
    private ProtossBuildOrder(String relativePath) {
        super("Protoss/" + relativePath);
    }

    // =========================================================

    static int last = 0;

    /**
     * See ADynamicWorkerProductionManager which is also used to produce workers.
     */
    @Override
    public boolean produceWorker() {
        if (!AGame.canAfford(50, 0) || AGame.getSupplyFree() < 1) {
            return false;
        }

        AUnit building = Select.ourOneIdle(AtlantisConfig.BASE);
        if (building != null) {
            return building.train(AtlantisConfig.WORKER);
        }

        // If we're here it means all bases are busy. Try queue request
        for (AUnit base : Select.ourBases().reverse().list()) {
            if (
                    base.getRemainingTrainTime() <= 4
                    && base.hasNothingInQueue()
                    && AGame.getSupplyFree() >= 2
            ) {
                last = A.now();
                return base.train(AtlantisConfig.WORKER);
            }
        }

        return false;
    }

    @Override
    public boolean produceUnit(AUnitType unitType) {
        AUnitType whatBuildsIt = unitType.getWhatBuildsIt();
        AUnit unitThatWillProduce = Select.ourOneIdle(whatBuildsIt);
        if (unitThatWillProduce != null) {
            return unitThatWillProduce.train(unitType);
        }
//        else {
//            System.err.println("Can't find " + whatBuildsIt + " to produce " + unitType);
//        }

        return false;
    }

    @Override
    public ArrayList<AUnitType> produceWhenNoProductionOrders() {
        ArrayList<AUnitType> units = new ArrayList<>();
        
        if (ADynamicWorkerProductionManager.shouldTrainWorkers()) {
            units.add(AUnitType.Protoss_Probe);
            units.add(AUnitType.Protoss_Probe);
        }
                
        units.add(AUnitType.Protoss_Dragoon);
        units.add(AUnitType.Protoss_Zealot);
        
        return units;
    }

}
