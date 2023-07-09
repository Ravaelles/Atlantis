package atlantis.combat.squad;

import atlantis.architecture.Commander;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.combat.squad.delta.Delta;
import atlantis.game.A;

public class SquadStateCommander extends Commander {

    @Override
    public void handle() {
//        makeSureSquadObjectsAreInitialized();
    }

    private void makeSureSquadObjectsAreInitialized() {
//        if (A.seconds() <= 1) {
//            Alpha.get();
//            Beta.get();
//            Delta.get();
//        }
    }

}
