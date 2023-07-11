package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

import java.util.Iterator;


public class ARepairCommander extends Commander {

    @Override
    public void handle() {
        if (!We.terran() || Select.ourOfType(AUnitType.Terran_SCV).empty()) {
            return;
        }

        if (AGame.everyNthGameFrame(11)) {
            removeExcessiveRepairersIfNeeded();

            if (!OptimalNumOfRepairers.weHaveTooManyRepairersOverall()) {
                RepairerAssigner.assignRepairersToWoundedUnits();
            }
        }

        // === Handle bunker or tank protectors =================================

        AProtectorManager.handleProtectors();
        
        // === Handle normal repairers ==================================

        handleStandardRepairers();

        // =========================================================

        makeSureEnoughMineralsIsLeftForRepairers();
    }

    // =========================================================

    public static void handleStandardRepairers() {
        for (Iterator<AUnit> iterator = ARepairAssignments.getRepairers().iterator(); iterator.hasNext();) {
            AUnit repairer = iterator.next();

            if (repairer.isProtector()) {
                continue;
            }

            if (!repairer.isAlive()) {
//                System.err.println("Dead repairer " + repairer.name() + " // " + repairer.hp());
                ARepairAssignments.removeRepairer(repairer);
                iterator.remove();
                continue;
            }
            ARepairerManager.handle(repairer);
        }
    }

    protected static void makeSureEnoughMineralsIsLeftForRepairers() {
        int totalRepairers = ARepairAssignments.countTotalRepairers();
        int minMineralsForRepairers = totalRepairers * 20;
        if (totalRepairers >= 1 && ProductionQueue.mineralsReserved() <= minMineralsForRepairers) {
            ProductionQueue.setMineralsNeeded(
                Math.max(ProductionQueue.mineralsNeeded(), minMineralsForRepairers)
            );
        }
    }

}
