package atlantis.repair;

import atlantis.combat.micro.AAvoidDefensiveBuildings;
import atlantis.combat.micro.AAvoidEnemyMeleeUnitsManager;
import atlantis.combat.micro.AAvoidInvisibleEnemyUnits;
import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;

import java.util.*;


public class ARepairerManager {

    public static boolean updateRepairer(AUnit repairer) {
        if (handleRepairerSafety(repairer)) {
            return true;
        }

        return handleRepairs(repairer);
    }

    // =========================================================

    private static boolean handleRepairerSafety(AUnit repairer) {
        if (AAvoidInvisibleEnemyUnits.avoidInvisibleUnits(repairer)) {
            return true;
        }

        if (repairer.getHPPercent() <= 50 && AAvoidEnemyMeleeUnitsManager.avoidCloseMeleeUnits(repairer)) {
            return true;
        }

        if (AAvoidDefensiveBuildings.avoidCloseBuildings(repairer)) {
            return true;
        }

        return false;
    }

    private static boolean handleRepairs(AUnit repairer) {
        AUnit target = ARepairAssignments.getUnitToRepairFor(repairer);
        if (target == null || !target.isAlive()) {
            repairer.setTooltip("Null unit2repair");
            ARepairAssignments.removeRepairerOrProtector(repairer);
            return true;
        }

        // Target is totally healthy
        if (!target.isWounded()) {
            repairer.setTooltip("Repaired!");
            ARepairAssignments.removeRepairerOrProtector(repairer);
            return handleRepairCompletedTryFindingNewTarget(repairer);
        }

        // Target is wounded
        repairer.setTooltip("Repair " + target.getShortNamePlusId() + " " + repairer.getLastUnitOrderWasFramesAgo());
        if (!repairer.isRepairing()) {
            repairer.repair(target);
        }
        return true;
    }

    // =========================================================

    /**
     * The repair of unit assigned has finished, but instead of unproductively going back to base,
     * try finding new repairable unit.
     */
    private static boolean handleRepairCompletedTryFindingNewTarget(AUnit repairer) {
        AUnit closestUnitNeedingRepair = Select.our().repairable(true).inRadius(25, repairer).first();

        if (closestUnitNeedingRepair != null) {
            ARepairAssignments.addRepairer(closestUnitNeedingRepair, closestUnitNeedingRepair);
            repairer.repair(closestUnitNeedingRepair);
            return true;
        }

        return false;
    }

    protected static boolean handleIdleRepairer(AUnit repairer) {
        if (repairer.isMoving() || !repairer.isRepairing() || repairer.isIdle()) {
            int maxAllowedDistToRoam = Missions.globalMission().isMissionDefend() ? 4 : 12;
            
            // Try finding any repairable and wounded unit nearby
            AUnit nearestWoundedUnit = Select.our().repairable(true)
                    .inRadius(maxAllowedDistToRoam, repairer).nearestTo(repairer);
            if (nearestWoundedUnit != null) {
                repairer.repair(nearestWoundedUnit);
                repairer.setTooltip("Help near " + nearestWoundedUnit.getShortName());
                return true;
            }
        }
        
        return false;
    }
}
