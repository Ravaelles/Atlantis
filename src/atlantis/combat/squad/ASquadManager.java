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

        Squad squad = Squad.getAlphaSquad();
        squad.addUnit(unit);
        unit.setSquad(squad);
        
//        AGame.sendMessage("Assign " + unit + " to squad " + squad);
//        System.err.println("Assign " + unit + " to squad " + squad);
        
//        AGame.sendMessage("Squad size: " + squad.size());
//        System.err.println("Squad size: " + squad.size());
    }

    public static void battleUnitDestroyed(AUnit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Squad squad = unit.squad();
        if (squad != null) {
            squad.removeUnit(unit);
            unit.setSquad(null);
        }
    }

    /**
     * Skips buildings, workers and Zerg Larva
     * @param unit
     * @return
     */
    private static boolean shouldSkipUnit(AUnit unit) {
        return unit.isNotRealUnit() || unit.isWorker() || unit.type().isMine();
    }

    // =========================================================
    // Manage squads

}
