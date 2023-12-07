package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class StickToAssignments extends Manager {
    public double MAX_DIST_TO_ASSIGNMENT = 1.65;
    public double MIN_DIST_TO_ASSIGNMENT = 0.45;

    public StickToAssignments(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        AUnit assignment = medicAssignment();

        if (assignment != null && assignment.isAlive()) {
//            APainter.paintLine(medic, assignment, Color.White);

            double dist = assignment.distTo(unit);

            if (dist > MAX_DIST_TO_ASSIGNMENT) {
                return unit.move(assignment.position(), Actions.MOVE_FOLLOW, "Stick", false);
            }
            else if (dist <= MIN_DIST_TO_ASSIGNMENT) {
                return unit.moveAwayFrom(assignment.position(), 0.3, Actions.MOVE_FORMATION, "Separate");
            }
        }

        return false;
    }

    @Override
    public Manager handle() {
        return null;
    }


    private AUnit medicAssignment() {
        AUnit assignment = TerranMedic.medicsToAssignments.get(unit);

        if (assignment != null && !assignment.isAlive()) {
            removeAssignment(assignment);
            assignment = null;
        }

        if (assignment == null) {
            assignment = createMedicAssignment();
        }

        return assignment;
    }

    private AUnit createMedicAssignment() {
        AUnit assignment;
        Selection inSquadSelector = Select.from(unit.squad()).inRadius(20, unit);

        // =========================================================
        // Firebats

        assignment = inSquadSelector.clone().ofType(AUnitType.Terran_Firebat).randomWithSeed(unit.id());
        if (assignment != null) {
            addMedicAssignment(assignment);
            return assignment;
        }

        // =========================================================
        // Infantry without any medics assigned

        assignment = inSquadSelector.clone()
            .terranInfantryWithoutMedics()
            .exclude(TerranMedic.assignmentsToMedics.keySet())
            .randomWithSeed(unit.id());
        if (assignment != null) {
            addMedicAssignment(assignment);
            return assignment;
        }

        // =========================================================
        // Infantry even if already a medic is assigned

        assignment = inSquadSelector.clone()
            .terranInfantryWithoutMedics()
            .randomWithSeed(unit.id());
        if (assignment != null) {
            addMedicAssignment(assignment);
            return assignment;
        }

        return null;
    }

    private void addMedicAssignment(AUnit assignment) {
        TerranMedic.medicsToAssignments.put(unit, assignment);
        TerranMedic.assignmentsToMedics.put(assignment, unit);
        unit.setTooltipTactical("NewAssignment");
    }

    private void removeAssignment(AUnit assignment) {
        TerranMedic.medicsToAssignments.remove(unit);
        TerranMedic.assignmentsToMedics.remove(assignment);
    }
}
