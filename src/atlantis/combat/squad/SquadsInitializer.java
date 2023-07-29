package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.combat.squad.delta.Delta;

public class SquadsInitializer {
    private final SquadsInitializer init = new SquadsInitializer();

    private SquadsInitializer() {
        Alpha.get();
        Beta.get();
        Delta.get();
    }
}
