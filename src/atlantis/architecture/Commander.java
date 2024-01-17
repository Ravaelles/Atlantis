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

    public void invokeCommander() {
        if (A.now() == lastFrameInvoked) return;
        lastFrameInvoked = A.now();

        CodeProfiler.startMeasuring(this);

        if (applies()) {
            handle();

            handleSubcommanders();
        }

        CodeProfiler.endMeasuring(this);
    }

    public void forceHandle() {
        handle();
    }

    protected void handle() {
        handleSubcommanders();
    }

    public void handleSubcommanders() {
        for (Commander commander : commanderObjects) {
            commander.invokeCommander();
        }
    }
}
