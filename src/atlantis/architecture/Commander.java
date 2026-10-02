package atlantis.architecture;

import atlantis.debug.profiler.CodeProfiler;
import atlantis.game.A;

public class Commander extends BaseCommander {
    /**
     * All sub-commanders, as explicit constructor references. Order matters:
     * it is the execution order.
     */
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{};
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

    /**
     * Handles this commander for the current frame.
     *
     * <p><b>Contract (Stage C):</b> returns {@code true} if this commander (or
     * any of its subcommanders) did anything this frame. The caller
     * ({@link #handleSubcommanders}) OR-accumulates results and runs
     * <b>all</b> subcommanders regardless — a {@code true} here never stops
     * the chain. This differs deliberately from
     * {@link Manager#handle()}, where a non-null return stops the chain.
     * Both contracts are documented (not unified), because unifying the
     * traversal semantics would change bot behaviour.</p>
     */
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
