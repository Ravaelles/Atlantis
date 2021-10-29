package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.UnitActions;
import bwapi.TechType;

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
        if (handleHealWoundedUnit(medic)) {
            return true;
        }

        // If there's no "real" infantry around, go to the nearest Marine, Firebat or Ghost.
        return handleStickToAssignments(medic);
    }

    // =========================================================

    private static void healUnit(AUnit medic, AUnit unitToHeal) {
        if (medic != null && unitToHeal != null && !unitToHeal.equals(medic.getTarget())) {
            medic.useTech(TechType.Healing, unitToHeal);
            medic.setTooltip("Heal");
        }
    }

    private static AUnit medicAssignment(AUnit medic) {
        AUnit assignment = medicsAssignments.get(medic);

        if (assignment == null || !assignment.isAlive()) {
            assignment = createMedicAssignment(medic);
        }

        return assignment;
    }

    private static AUnit createMedicAssignment(AUnit medic) {
        AUnit assignment = Select.ourTerranInfantryWithoutMedics().randomWithSeed(medic.id());
        if (assignment != null) {
            medicsAssignments.put(medic, assignment);
            medic.setTooltip("NewAssignment");
            return assignment;
        }

        // If here, then it means no alive infantry left, stick to medics
        return Select.ourTerranInfantry().exclude(medic).randomWithSeed(medic.id());
    }

    private static boolean handleStickToAssignments(AUnit medic) {
        AUnit assignment = medicAssignment(medic);

        if (assignment != null) {
            double dist = assignment.distTo(medic);

            if (dist > 1.9) {
                medic.move(assignment.position(), UnitActions.MOVE, "Stick");
                return true;
            }
            else if (dist <= 1.4) {
                medic.moveAwayFrom(assignment.position(), 0.4, "Spread");
                return true;
            }
        }
        
        return false;
    }

    private static boolean handleHealWoundedUnit(AUnit medic) {
//        if (!medic.isIdle() && medic.getLastCommand().getType() == UnitCommandType.Right_Click_Unit) {
//            return true;
//        }

        AUnit nearestWoundedInfantry = Select.our()
                .organic()
                .wounded()
                .inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, medic)
                .nearestTo(medic);

//        System.out.println(nearestWoundedInfantry + " // " + nearestWoundedInfantry.hp() + " // " + nearestWoundedInfantry.maxHp());

        // =========================================================
        // If there's a wounded unit, heal it.

        if (nearestWoundedInfantry != null) {
            healUnit(medic, nearestWoundedInfantry);
            return true;
        }

        return false;
    }

}
