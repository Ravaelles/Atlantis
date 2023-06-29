package atlantis.combat;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.SquadTransfers;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.combat.squad.delta.Delta;
import atlantis.game.A;

import java.util.Iterator;

public class ACombatCommander {

    /**
     * Acts with all battle units.
     */
    public static void update() {
        updateGlobalMission();
        updateSquadTransfers();
        updateSquads();
    }

    private static void updateSquads() {
        for (Iterator<Squad> it = Squad.getSquads().iterator(); it.hasNext();) {
            Squad squad = it.next();
            ASquadManager.update(squad);
        }
    }

    private static void updateSquadTransfers() {
        if (A.seconds() <= 1) {
            makeSureSquadObjectsAreCreated();
        }

        SquadTransfers.updateSquadTransfers();
    }

    private static void updateGlobalMission() {
        // Global mission is de facto Alpha squad's mission
        Alpha alpha = Alpha.get();
        if (alpha.mission() == null) {
            alpha.setMission(Missions.globalMission());
        }
    }

    private static void makeSureSquadObjectsAreCreated() {
        Alpha.get();
        Beta.get();
        Delta.get();
    }
}
