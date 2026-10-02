package atlantis.game;

import atlantis.application.FramePipeline;
import atlantis.architecture.Commander;

/**
 * Top abstraction level entity that issues orders to all other modules (managers).
 * /*
 * Executes every time when game has new frame.
 * It represents minimal passage of game-time (one game frame).
 *
 * <p>The ordered list of top-level steps now lives in
 * {@link atlantis.application.FramePipeline} — one explicit source of truth
 * (Stage C, _AI/REVIEW.md §16).</p>
 */
public class AtlantisGameCommander extends Commander {
    public static Class<? extends Commander>[] topLevelSubcommanders() {
        return FramePipeline.steps();
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return topLevelSubcommanders();
    }
}
