package atlantis.combat.squad;

import atlantis.units.AUnit;
import java.util.ArrayList;

/**
 * Commands all existing battle squads.
 */
public class ASquadManager {

    /**
     * List of all unit squads.
     */
    protected static ArrayList<Squad> squads = new ArrayList<>();

    // =========================================================
    
    public static void possibleCombatUnitCreated(AUnit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Squad squad = Squad.alpha();
        if (!squad.list().contains(unit)) {
            squad.addUnit(unit);
            unit.setSquad(squad);
        }

//        AGame.sendMessage("Assign " + unit + " to squad " + squad);
//        System.err.println("Assign " + unit + " to squad " + squad);
        
//        AGame.sendMessage("Squad size: " + squad.size());
//        System.err.println("Squad size: " + squad.size());
    }

    public static void unitDestroyed(AUnit unit) {
        Squad squad = unit.squad();
        if (squad != null) {
            unit.setSquad(null);
            squad.removeUnit(unit);
        }
    }

    /**
     * Skips buildings, workers and Zerg Larva
     */
    private static boolean shouldSkipUnit(AUnit unit) {
        return unit.isNotRealUnit() || unit.isWorker() || unit.type().isMine();
    }

    // =========================================================
    // Manage squads

}
