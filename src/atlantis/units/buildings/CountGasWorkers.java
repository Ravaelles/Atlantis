package atlantis.units.buildings;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class CountGasWorkers {
    public static int countWorkersGatheringGasFor(AUnit gasBuilding) {
        int total = 0;

        for (AUnit worker : Select.ourWorkers().inRadius(12, gasBuilding).list()) {
//            if (worker.isGatheringGas() || (worker.isCarryingGas() && worker.) {
//            if (worker.isCarryingGas()) {
//                System.out.println("carrying gas, " + worker.orderTarget() + " / " + worker.isMoving() + " / " + worker.action());
//            }

            if (worker.isGatheringGas() || worker.isAction(Actions.GATHER_GAS)) {
//                System.out.println("gathering gas, " + worker.orderTarget() + " / " + worker.isMoving() + " / " + worker.action());
                total++;
            }
        }

        return total;
    }
}