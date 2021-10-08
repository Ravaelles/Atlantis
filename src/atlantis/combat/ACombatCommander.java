package atlantis.combat;

import atlantis.AGame;
import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.util.CodeProfiler;

public class ACombatCommander {
    
    /**
     * Acts with all battle units.
     */
    public static void update() {
        CodeProfiler.startMeasuring(CodeProfiler.ASPECT_COMBAT);
        
        if (AGame.everyNthGameFrame(40)) {
            Missions.evaluateGlobalMission();
        }
        
        // === Handle all squads ===================================
        
        for (Squad squad : ASquadManager.getSquads()) {
            handleSquad(squad);
        }
        
        // =========================================================
        
        CodeProfiler.endMeasuring(CodeProfiler.ASPECT_COMBAT);
    }

    // =============================================================
    
    /**
     * Acts with all units that are part of given battle squad, according to the SquadMission object and using
     * proper micro managers.
     */
    private static void handleSquad(Squad squad) {
        squad.setMission(Missions.globalMission());

        // =========================================================
        
        // Act with every combat unit
        for (AUnit unit : squad.arrayList()) {
            ACombatUnitManager.update(unit);

            if (AGame.everyNthGameFrame(30)) {
                unit.lastX = unit.getX();
                unit.lastY = unit.getY();
            }
        }
    }

}
