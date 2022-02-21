package atlantis.terran.repair;

import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import java.util.Iterator;


public class ARepairCommander {

    public static void update() {
        if (AGame.everyNthGameFrame(13)) {
            RepairerAssigner.assignRepairersToWoundedUnits();
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
                System.err.println("Dead repairer " + repairer.name() + " // " + repairer.hp());
                ARepairAssignments.removeRepairerOrProtector(repairer);
                iterator.remove();
                continue;
            }
            ARepairerManager.updateRepairer(repairer);
        }
    }

    // =========================================================
    // === Asign repairers if needed ===========================
    // =========================================================

    protected static int defineOptimalRepairersForBunker(AUnit bunker) {
        int enemiesNear = Select.enemy().combatUnits().inRadius(10, bunker).count();
        double optimalNumber;

        if (AGame.isEnemyProtoss()) {
            optimalNumber = enemiesNear;
        } else if (AGame.isEnemyTerran()) {
            optimalNumber = enemiesNear * 0.38;
        } else {
            optimalNumber = enemiesNear * 0.4;
        }

        if (bunker.hp() < 100) {
            optimalNumber += 2;
        }

        return Math.min(7, (int) Math.ceil(optimalNumber));
    }

}
