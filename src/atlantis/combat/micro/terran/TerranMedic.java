package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.PositionUtil;
import java.util.HashMap;

public class TerranMedic {
    
    private static HashMap<AUnit, AUnit> medicsAssignments = new HashMap<>();
    
    // =========================================================

    public static boolean update(AUnit medic) {

        // =========================================================
        // Define nearest wounded infantry unit
        if (handleHealWoundedUnit(medic)) {
            return true;
        }

        // =========================================================
        // If there's no "real" infatnry around, go to the nearest Marine, Firebat or Ghost.
        if (handleTooFarFromRealInfantry(medic)) {
            return true;
        }
        
        // =========================================================
        // False: Did not use micro-manager, allow mission behavior
        return false;
    }

    // =========================================================
    private static void healUnit(AUnit medic, AUnit unitToHeal) {
        if (medic.getTarget() == null || !medic.getTarget().equals(unitToHeal)) {
            if (unitToHeal != null) {
                medic.rightClick(unitToHeal.u());
            }
        }
    }

    private static AUnit getInfantryAssignedForThisMedic(AUnit medic) {
        AUnit assignment = medicsAssignments.get(medic);
        
        if (assignment == null || !assignment.exists()) {
            assignment = Select.ourTerranInfantryWithoutMedics().random();
        }
        
        return assignment;
    }

    private static boolean handleTooFarFromRealInfantry(AUnit medic) {
        int realInfantryNearby
                = Select.our().inRadius(4, medic.getPosition()).countUnitsOfType(AUnitType.Terran_Marine,
                AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
        if (realInfantryNearby == 0) {
            AUnit nearestRealInfantry = Select.ourTerranInfantryWithoutMedics().nearestTo(medic.getPosition());
            if (nearestRealInfantry != null) {
                // Check if medic is close to the infantry it should be close to
                AUnit infantryAssignedForThisMedic = getInfantryAssignedForThisMedic(medic);
                medic.move(infantryAssignedForThisMedic.getPosition());
                return true;
            }
        }
        return false;
    }

    private static boolean handleHealWoundedUnit(AUnit medic) {
        AUnit nearestWoundedInfantry = (AUnit) Select.ourCombatUnits().infantry().wounded()
                .inRadius(6, medic.getPosition()).nearestTo(medic.getPosition());

        // =========================================================
        // If there's wounded unit, heal it.
        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        } 

        // =========================================================
        // If no wounded unit, get close to random infantry
        AUnit nearestInfantry = (AUnit) Select.our().ofType(
                AUnitType.Terran_Marine,
                AUnitType.Terran_Firebat,
                AUnitType.Terran_Ghost
        ).nearestTo(medic.getPosition());
        if (nearestInfantry != null && PositionUtil.distanceTo(nearestInfantry, medic) > 1.4
                && !nearestInfantry.equals(medic.getTarget())) {
            healUnit(medic, nearestWoundedInfantry);
        }
        return false;
    }

}
