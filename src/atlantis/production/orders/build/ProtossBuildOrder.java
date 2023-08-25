package atlantis.production.orders.build;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProtossBuildOrder extends ABuildOrder {

//    public static final ProtossBuildOrder PROTOSS_2_GATE_RANGE_EXPAND = new ProtossBuildOrder("2 Gate Range Expand");
//    public static final ProtossBuildOrder PROTOSS_2_GATEWAY_ZEALOT = new ProtossBuildOrder("2 Gateway Zealot");

    // =========================================================

    public ProtossBuildOrder(String name) {
        super(name);
    }
//    protected ProtossBuildOrder(String name, ArrayList<ProductionOrder> productionOrders) {
////        super("Protoss/" + name);
//        super(name);
//    }

    // =========================================================

    @Override
    public boolean produceUnit(AUnitType unitType) {
        AUnitType whatBuildsIt = unitType.whatBuildsIt();
        if (whatBuildsIt == null) {
            System.err.println("Can't find " + whatBuildsIt + " to produce " + unitType);
        }

        AUnit unitThatWillProduce = Select.ourOneNotTrainingUnits(whatBuildsIt);

        if (unitThatWillProduce != null) {
            return unitThatWillProduce.train(unitType);
        }

        return false;
    }

}
