package atlantis.combat.squad;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.combat.squad.squads.omega.Omega;
import atlantis.combat.squad.squads.delta.Delta;

public class SquadsInitializer {
    private final SquadsInitializer init = new SquadsInitializer();

    private SquadsInitializer() {
        Alpha.get();
        Omega.get();
        Delta.get();

        /**
         * To change missions see OnGameStarted class
         */
    }
}
