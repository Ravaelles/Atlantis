package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.A;

public class AbundanceCommander extends Commander {
    @Override
    public boolean applies() {
        return !A.everyFrameExceptNthFrame(17);
    }
}
