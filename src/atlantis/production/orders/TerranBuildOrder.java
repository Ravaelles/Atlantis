package atlantis.production.orders;

import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;

public class TerranBuildOrder extends ABuildOrder {

//    public static final TerranBuildOrder TERRAN_BBS = new TerranBuildOrder("BBS");
//    public static final TerranBuildOrder TERRAN_1_FE = new TerranBuildOrder("1 Fact FE");
//    public static final TerranBuildOrder TERRAN_1_Base_Vultures = new TerranBuildOrder("1 Base Vultures");
//    public static final TerranBuildOrder TERRAN_Nada_2_Fact = new TerranBuildOrder("Nada 2 Fact");

    // =========================================================

    public TerranBuildOrder(String name) {
        super(name);
    }

    // =========================================================

    @Override
    public boolean produceUnit(AUnitType type) {
        AUnitType whatBuildsIt = type.getWhatBuildsIt();
        AUnit unitThatWillProduce = Select.ourOfType(whatBuildsIt).free().first();
        if (unitThatWillProduce != null) {
            return unitThatWillProduce.train(type);
        }
//        else {
//            System.err.println("Can't find " + whatBuildsIt + " to produce " + unitType);
//        }

        return false;
    }

    @Override
    public boolean produceWorker() {
        return super.produceWorker();
    }

}
