package atlantis.combat;

import atlantis.AtlantisGame;
import atlantis.combat.squad.AtlantisSquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.missions.Missions;
import atlantis.units.AUnit;

public class AtlantisCombatCommander {
    
    /**
     * Acts with all battle units.
     */
    public static void update() {
        if (AtlantisGame.getTimeFrames() % 20 == 0) {
            Missions.handleGlobalMission();
        }
        
        // === Handle all squads ========================================
        
        for (Squad squad : AtlantisSquadManager.getSquads()) {
            handleSquad(squad);
        }
    }

    // =========================================================
    
    /**
     * Acts with all units that are part of given battle squad, according to the SquadMission object and using
     * proper micro managers.
     */
    private static void handleSquad(Squad squad) {

        // Make sure this battle squad has up-to-date strategy
        if (!Missions.getCurrentGlobalMission().equals(squad.getMission())) {
            squad.setMission(Missions.getCurrentGlobalMission());
        }

        // =========================================================
        
        // Act with every unit
//        System.out.println("--- (" + squad.size() + ") ---------------");
        for (AUnit unit : squad.arrayList()) {
//            System.out.println("Commander (" + (unit.getSquad() != null) + "): " + unit);
//            unit.setTooltip("C:" + AtlantisGame.getTimeFrames());
            AtlantisCombatUnitManager.update(unit);
        }
    }

}
