package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.delta.Delta;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class NewUnitsToSquadsAssigner extends HasUnit {

    public NewUnitsToSquadsAssigner(AUnit unit) {
        super(unit);
    }

    public void possibleCombatUnitCreated() {
        if (shouldSkipUnit()) {
            return;
        }

        Squad squad = chooseSquadFor();


        if (!squad.contains(unit)) {
            squad.addUnit(unit);
            unit.setSquad(squad);
//            System.err.println(unit + " assigned, now unit.squad = " + unit.squad());
        }
    }

    // =========================================================

    private Squad chooseSquadFor() {
        Alpha alpha = Alpha.get();

        if (assignToDelta()) {
            return Delta.get();
        }

        return alpha;
    }

    private boolean assignToDelta() {
        return (unit.isAir() && !unit.type().isTransport())
            || unit.type().isDetectorNonBuilding();
    }

    /**
     * Skips buildings, workers and Zerg Larva
     */
    private boolean shouldSkipUnit() {
        return !unit.isRealUnit() || unit.isWorker() || unit.type().isMine() || unit.isABuilding();
    }

}
