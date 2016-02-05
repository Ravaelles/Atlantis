package atlantis.combat.micro.terran;

import atlantis.wrappers.SelectUnits;
import atlantis.wrappers.Units;
import java.util.Collection;
import java.util.HashMap;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

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
        
        if (assignment == null || !assignment.isAlive()) {
            assignment = SelectUnits.ourTerranInfantryWithoutMedics().random();
        }
        
        return assignment;
    }

    private static boolean handleTooFarFromRealInfantry(Unit medic) {
        int realInfantryNearby
                = SelectUnits.our().inRadius(4, medic).countUnitsOfType(UnitType.UnitTypes.Terran_Marine,
                UnitType.UnitTypes.Terran_Firebat, UnitType.UnitTypes.Terran_Ghost);
        if (realInfantryNearby == 0) {
            Unit nearestRealInfantry = SelectUnits.ourTerranInfantryWithoutMedics().nearestTo(medic);
            if (nearestRealInfantry != null) {
                // Check if medic is close to the infantry it should be close to
                Unit infantryAssignedForThisMedic = getInfantryAssignedForThisMedic(medic);
                medic.move(infantryAssignedForThisMedic);
                return true;
            }
        }
        return false;
    }

    private static boolean handleHealWoundedUnit(Unit medic) {
        Unit nearestWoundedInfantry = SelectUnits.ourCombatUnits().infantry().wounded()
                .inRadius(6, medic).nearestTo(medic);

        // =========================================================
        // If there's wounded unit, heal it.
        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        } 

        // =========================================================
        // If no wounded unit, get close to random infantry
        Unit nearestInfantry = SelectUnits.our().ofType(
                UnitType.UnitTypes.Terran_Marine,
                UnitType.UnitTypes.Terran_Firebat,
                UnitType.UnitTypes.Terran_Ghost
        ).nearestTo(medic);
        if (nearestInfantry != null && nearestInfantry.distanceTo(medic) > 1.4
                && !nearestInfantry.equals(medic.getTarget())) {
            healUnit(medic, nearestWoundedInfantry);
        }
        return false;
    }

}
