package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.delta.Delta;
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
            squad.addUnit(unit);
            unit.setSquad(squad);
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
        Alpha alpha = Alpha.get();

        if (shouldAssignToDelta()) {
            return Delta.get();
        }

        return alpha;
    }

    private boolean shouldAssignToDelta() {
        return (unit.isAir() && !unit.type().isTransport())
            || unit.type().isDetectorNonBuilding();
    }

    /**
     * Skips buildings, workers and Zerg Larva
     */
    private boolean shouldSkipUnit() {
        if (!unit.isOur()) return true;

        return !unit.isRealUnit() || unit.isWorker() || unit.isABuilding();
    }

}
