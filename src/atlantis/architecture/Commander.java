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

    public void invoke() {
        if (A.now() == lastFrameInvoked) return;
        lastFrameInvoked = A.now();

        CodeProfiler.startMeasuring(this);

        if (applies()) {
            handle();
        }

        CodeProfiler.endMeasuring(this);

        handleSubcommanders();
    }

    public void forceHandle() {
        handle();
    }

    protected void handle() {
        handleSubcommanders();
    }

    public void handleSubcommanders() {
        for (Commander commander : commanderObjects) {
            commander.invoke();
        }
    }
}
