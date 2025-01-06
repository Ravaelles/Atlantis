package atlantis.map.scout.enemy;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TestRoamingAroundBase extends Manager {
    public TestRoamingAroundBase(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    public Manager handle() {
//        Selection workers = Select.ourWorkers();
//        AUnit horse = workers.last();
//        if (horse.equals(worker)) {
//            roamAroundEnemyBase();
//            worker.setTooltipTactical("Patataj");
//            return true;
//        }
//        else {
//            worker.setTooltipTactical("Dajesz kurwa!");
//            if (A.now() % 50 >= 25) {
//                if (worker.move(horse, Actions.MOVE_SPECIAL, "", true)) {
//                    worker.setTooltipTactical("Ciśniesz!");
//                    return true;
//                }
//            }
//        }
//
//        return true;
        return usedManager(this);
    }
}
