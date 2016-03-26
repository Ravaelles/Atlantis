package atlantis.combat.micro.terran;

import atlantis.util.PositionUtil;
import atlantis.wrappers.Select;
import atlantis.wrappers.Units;
import java.util.Collection;
import java.util.HashMap;
import bwapi.Unit;
import bwapi.UnitType;

public class TerranMedic {
    
    private static HashMap<Unit, Unit> medicsAssignments = new HashMap<>();
    
    // =========================================================

    public static boolean update(Unit medic) {

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
    private static void healUnit(Unit medic, Unit unitToHeal) {
        if (medic.getTarget() == null || !medic.getTarget().equals(unitToHeal)) {
            medic.rightClick(unitToHeal, false);
        }
    }

    private static Unit getInfantryAssignedForThisMedic(Unit medic) {
        Unit assignment = medicsAssignments.get(medic);
        
        if (assignment == null || !assignment.exists()) {
            assignment = Select.ourTerranInfantryWithoutMedics().random();
        }
        
        return assignment;
    }

    private static boolean handleTooFarFromRealInfantry(Unit medic) {
        int realInfantryNearby
                = Select.our().inRadius(4, medic.getPosition()).countUnitsOfType(UnitType.Terran_Marine,
                UnitType.Terran_Firebat, UnitType.Terran_Ghost);
        if (realInfantryNearby == 0) {
            Unit nearestRealInfantry = Select.ourTerranInfantryWithoutMedics().nearestTo(medic.getPosition());
            if (nearestRealInfantry != null) {
                // Check if medic is close to the infantry it should be close to
                Unit infantryAssignedForThisMedic = getInfantryAssignedForThisMedic(medic);
                medic.move(infantryAssignedForThisMedic.getPosition());
                return true;
            }
        }
        return false;
    }

    private static boolean handleHealWoundedUnit(Unit medic) {
        Unit nearestWoundedInfantry = (Unit) Select.ourCombatUnits().infantry().wounded()
                .inRadius(6, medic.getPosition()).nearestTo(medic.getPosition());

        // =========================================================
        // If there's wounded unit, heal it.
        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        } 

        // =========================================================
        // If no wounded unit, get close to random infantry
        Unit nearestInfantry = (Unit) Select.our().ofType(
                UnitType.Terran_Marine,
                UnitType.Terran_Firebat,
                UnitType.Terran_Ghost
        ).nearestTo(medic.getPosition());
        if (nearestInfantry != null && PositionUtil.distanceTo(nearestInfantry, medic) > 1.4
                && !nearestInfantry.equals(medic.getTarget())) {
            healUnit(medic, nearestWoundedInfantry);
        }
        return false;
    }

}
