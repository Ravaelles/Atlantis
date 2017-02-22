package atlantis.combat;

import atlantis.AGame;
import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.missions.Missions;
import atlantis.units.AUnit;
import atlantis.util.CodeProfiler;

public class ACombatCommander {
    
    /**
     * Acts with all battle units.
     */
    public static void update() {
        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_COMBAT);
        
        // =========================================================
        
        if (AGame.getTimeFrames() % 20 == 0) {
            Missions.handleGlobalMission();
        }
        
        // === Handle all squads ========================================
        
        for (Squad squad : ASquadManager.getSquads()) {
            handleSquad(squad);
        }
        
        // =========================================================
        
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_COMBAT);
    }

    // =========================================================
    
    /**
     * Acts with all units that are part of given battle squad, according to the SquadMission object and using
     * proper micro managers.
     */
    private static void handleSquad(Squad squad) {

        // Make sure this battle squad has up-to-date strategy
        if (!Missions.getGlobalMission().equals(squad.getMission())) {
            squad.setMission(Missions.getGlobalMission());
        }

        // =========================================================
        
        // Act with every combat unit
        for (AUnit unit : squad.arrayList()) {
            ACombatUnitManager.update(unit);
        }
    }

}
