package atlantis.production.orders;

import atlantis.production.ADynamicWorkerProductionManager;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.ArrayList;

public class ProtossBuildOrder extends ABuildOrder {

//    public static final ProtossBuildOrder PROTOSS_2_GATE_RANGE_EXPAND = new ProtossBuildOrder("2 Gate Range Expand");
//    public static final ProtossBuildOrder PROTOSS_2_GATEWAY_ZEALOT = new ProtossBuildOrder("2 Gateway Zealot");

    // =========================================================

    public ProtossBuildOrder(String name, ArrayList<ProductionOrder> productionOrders) {
        super(name, productionOrders);
    }
//    protected ProtossBuildOrder(String name, ArrayList<ProductionOrder> productionOrders) {
////        super("Protoss/" + name);
//        super(name);
//    }

    // =========================================================

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
