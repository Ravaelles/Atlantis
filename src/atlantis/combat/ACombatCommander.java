package atlantis.combat;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.SquadTransfers;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.units.AUnit;

public class ACombatCommander {

    /**
     * Acts with all battle units.
     */
    public static void update() {

        // Global mission is de facto Alpha squad's mission
        Alpha alpha = Alpha.get();
        if (alpha.mission() == null) {
            alpha.setMission(Missions.globalMission());
        }

        SquadTransfers.updateSquadTransfers();

        for (Squad squad : Squad.getSquads()) {
            ASquadManager.update(squad);
        }
    }
}
