package atlantis.combat.squad.commanders;

import atlantis.architecture.Commander;

public class SquadStateCommander extends Commander {

    @Override
    protected void handle() {
//        makeSureSquadObjectsAreInitialized();

        assignAlphaSquadScout();
    }

    private void assignAlphaSquadScout() {

    }

    private void makeSureSquadObjectsAreInitialized() {
//        if (A.seconds() <= 1) {
//            Alpha.get();
//            Omega.get();
//            Delta.get();
//        }
    }

}
