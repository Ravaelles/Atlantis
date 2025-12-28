package atlantis.architecture;

import atlantis.debug.profiler.CodeProfiler;
import atlantis.game.A;

public class Commander extends BaseCommander {
    /**
     * All sub-commanders. Order matters.
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{};
    }

    public boolean applies() {
        return true;
    }

    public boolean invokedCommander() {
        if (A.now == lastFrameInvoked) return false;
        lastFrameInvoked = A.now;

        CodeProfiler.startMeasuring(this);

        boolean result = false;
        if (applies()) {
            result = handle();
        }

        CodeProfiler.endMeasuring(this);

        return result;
    }

    public boolean forceHandle() {
        return handle();
    }

    protected boolean handle() {
        return handleSubcommanders();
    }

    public boolean handleSubcommanders() {
        boolean result = false;

        for (Commander commander : commanderObjects) {
            result = result || commander.invokedCommander();
        }

        return result;
    }
}
