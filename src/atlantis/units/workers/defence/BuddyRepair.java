package atlantis.units.workers.defence;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

public class BuddyRepair extends Manager {
    public BuddyRepair(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && AGame.canAfford(20, 0);
    }

    @Override
    public Manager handle() {
        if (handleRepairNear(unit)) return usedManager(this);

        return null;
    }


    private static boolean handleRepairNear(AUnit worker) {
        if (!worker.isWounded() || (worker.id() % 5 != 0 && !worker.isRepairing())) return false;

        AUnit wounded = Select.ourWorkers().wounded().inRadius(3, worker).nearestTo(worker);

        if (wounded != null && A.hasMinerals(5) && wounded.isWounded() && wounded.isAlive() && !wounded.isBuilder()) {
            worker.repair(wounded, "BuddyRepair!");
            wounded.repair(worker, "BuddyRepair!");
//            if (!worker.isRepairing()) {
//            }
//            if (!wounded.isRepairing()) {
//            }
//            if (wounded.distToLessThan(worker, 0.6)) {
//                if (!wounded.isBuilder() && !wounded.isRepairing() || wounded.isMoving()) {
//                    wounded.stop("BeRepaired");
//                }
//                wounded.setTooltip("BeRepaired");
//            }
            return true;
        }

        return false;
    }
}
