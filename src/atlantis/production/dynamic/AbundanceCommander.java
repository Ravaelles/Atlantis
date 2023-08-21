package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.A;

public class AbundanceCommander extends Commander {
    @Override
    public boolean applies() {
        if (A.everyFrameExceptNthFrame(17)) return false;
        
        return true;
    }
}
