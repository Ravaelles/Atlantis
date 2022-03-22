package atlantis.terran.repair;

import atlantis.game.AGame;
import atlantis.units.AUnit;

import java.util.Iterator;


public class ARepairCommander {

    public static void update() {
        if (AGame.everyNthGameFrame(11)) {
            RepairerAssigner.removeExcessiveRepairersIfNeeded();

            if (!MaxRepairers.usingMoreRepairersThanAllowed()) {
                RepairerAssigner.assignRepairersToWoundedUnits();
            }
        }

        // === Handle bunker or tank protectors =================================

        AProtectorManager.handleProtectors();
        
        // === Handle normal repairers ==================================

        handleStandardRepairers();
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
            ARepairerManager.updateRepairer(repairer);
        }
    }

}
