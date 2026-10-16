package atlantis.combat.squad;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.combat.squad.squads.delta.Delta;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class NewUnitsToSquadsAssigner extends HasUnit {

    public NewUnitsToSquadsAssigner(AUnit unit) {
        super(unit);
    }

    public boolean possibleCombatUnitCreated() {
        if (shouldSkipUnit()) return false;

        Squad squad = chooseSquadFor();

        if (!squad.contains(unit)) {
            AssignUnitToSquad.assignTo(unit, squad);
            return true;
//            System.err.println(unit + " assigned, now unit.squad = " + unit.squad());
        }
//        else {
//            System.err.println(unit + " ALREADY assigned to = " + unit.squad());
//        }

        return false;
    }

    // =========================================================

    private Squad chooseSquadFor() {
        if (shouldAssignToDelta()) {
            return Delta.get();
        }

        if (shouldAssignToX()) {
            return Delta.get();
        }

        return Alpha.get();
    }

    private boolean shouldAssignToDelta() {
        return (unit.isAir() && !unit.type().isTransport())
            || unit.type().isDetectorNonBuilding();
    }

    private boolean shouldAssignToX() {
        return unit.isDarkTemplar();
    }

    /**
     * Skips buildings, workers and Zerg Larva
     */
    private boolean shouldSkipUnit() {
        if (!unit.isOur()) return true;

        return !unit.isRealUnit() || unit.isWorker() || unit.isABuilding();
    }

}
