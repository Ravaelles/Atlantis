package atlantis.combat.squad;

import atlantis.architecture.Commander;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.combat.squad.delta.Delta;
import atlantis.game.A;

import java.util.Iterator;

public class SquadCommander extends Commander {

    @Override
    public void handle() {
        super.handle();

        SquadTransfers.updateSquadTransfers();
        updateSquads();
        makeSureSquadObjectsAreCreated();
    }


    private void updateSquads() {
        for (Iterator<Squad> it = Squad.getSquads().iterator(); it.hasNext();) {
            Squad squad = it.next();
            ASquadManager.update(squad);
        }
    }

    private void makeSureSquadObjectsAreCreated() {
        if (A.seconds() <= 1) {
            Alpha.get();
            Beta.get();
            Delta.get();
        }
    }
}
