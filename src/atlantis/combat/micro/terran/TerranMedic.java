package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import bwapi.TechType;
import bwapi.UnitCommandType;
import java.util.HashMap;

public class TerranMedic {
    
    /**
     * Maximum allowed distance for a medic to heal wounded units that are not their assignment.
     * The idea is to disallow them to move away too much.
     */
    private static final int HEAL_OTHER_UNITS_MAX_DISTANCE = 6;

    /**
     * Specific units that medics should follow in order to heal them as fast as possible 
     * when they get wounded.
     */
    private static final HashMap<AUnit, AUnit> medicsAssignments = new HashMap<>();

    // =========================================================
    public static boolean update(AUnit medic) {

        // =========================================================
        // Define nearest wounded infantry unit
        if (handleHealWoundedUnit(medic)) {
            return true;
        }

        // =========================================================
        // If there's no "real" infantry around, go to the nearest Marine, Firebat or Ghost.
        if (handleTooFarFromRealInfantry(medic)) {
            return true;
        }

        // =========================================================
        // False: Did not use micro-manager, allow mission behavior
        return false;
    }

    // =========================================================
    private static void healUnit(AUnit medic, AUnit unitToHeal) {
        if (medic != null && unitToHeal != null && !unitToHeal.equals(medic.getTarget())) {
            medic.useTech(TechType.Healing, unitToHeal);
        }
    }

    private static AUnit getInfantryAssignedForThisMedic(AUnit medic) {
        AUnit assignment = medicsAssignments.get(medic);

        if (assignment == null || !assignment.exists() || !assignment.isAlive()) {
            assignment = Select.ourTerranInfantryWithoutMedics().first();
            
            if (assignment != null) {
                medicsAssignments.put(medic, assignment);
            }
        }

        return assignment;
    }

    private static boolean handleTooFarFromRealInfantry(AUnit medic) {
        AUnit unitAssignedForMedic = getInfantryAssignedForThisMedic(medic);
        if (unitAssignedForMedic != null) {
            if (unitAssignedForMedic.distanceTo(medic) > 1.9) {
                if (Select.ourTerranInfantryWithoutMedics().inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, medic).count() <= 2) {
                    medic.move(unitAssignedForMedic.getPosition(), UnitActions.MOVE);
                    return true;
                }
            }
        }
        
        return false;
    }

    private static boolean handleHealWoundedUnit(AUnit medic) {
        if (!medic.isIdle() && medic.getLastCommand().getUnitCommandType() == UnitCommandType.Right_Click_Unit) {
            return true;
        }

        AUnit nearestWoundedInfantry = (AUnit) Select.ourCombatUnits().infantry().wounded()
                .inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, medic).nearestTo(medic);

        // =========================================================
        // If there's a wounded unit, heal it.
        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        }

        return false;
    }

}
