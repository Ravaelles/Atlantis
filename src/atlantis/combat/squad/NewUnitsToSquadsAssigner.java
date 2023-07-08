package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.delta.Delta;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class NewUnitsToSquadsAssigner {

    public static void possibleCombatUnitCreated(AUnit unit) {
        if (shouldSkipUnit()) {
            return;
        }

        Squad squad = chooseSquadFor();
//        System.out.println("squad = " + squad);
//        System.out.println("squad.contains() = " + squad.contains() + " // " + unit.name());
        if (!squad.contains()) {
            squad.addUnit();
            unit.setSquad(squad);
//            System.err.println(unit + " assigned, now unit.squad = " + unit.squad());
        }
    }

    // =========================================================

    private static Squad chooseSquadFor(AUnit unit) {
        Alpha alpha = Alpha.get();

        if (assignToDelta()) {
            return Delta.get();
        }

        return alpha;
    }

    private static boolean assignToDelta(AUnit unit) {
        return (unit.isAir() && !unit.type().isTransport())
            || unit.type().isDetectorNonBuilding();
    }

    /**
     * Skips buildings, workers and Zerg Larva
     */
    private static boolean shouldSkipUnit(AUnit unit) {
        return !unit.isRealUnit() || unit.isWorker() || unit.type().isMine() || unit.isBuilding();
    }

}
