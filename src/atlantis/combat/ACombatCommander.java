package atlantis.combat;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.SquadTransfers;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.combat.squad.delta.Delta;
import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.Iterator;

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

        if (A.seconds() <= 1) {
            makeSureSquadObjectsAreCreated();
        }

        SquadTransfers.updateSquadTransfers();

        for (Iterator<Squad> it = Squad.getSquads().iterator(); it.hasNext();) {
            Squad squad = it.next();
            ASquadManager.update(squad);
        }
    }

    private static void makeSureSquadObjectsAreCreated() {
        Alpha.get();
        Beta.get();
        Delta.get();
    }
}
