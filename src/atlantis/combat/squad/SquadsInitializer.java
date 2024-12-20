package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.omega.Omega;
import atlantis.combat.squad.delta.Delta;

public class SquadsInitializer {
    private final SquadsInitializer init = new SquadsInitializer();

    private SquadsInitializer() {
        Alpha.get();
        Omega.get();
        Delta.get();

        /**
         * To change missions see OnStart class
         */
    }
}
