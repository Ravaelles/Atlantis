package atlantis.util;

/**
 * Fail-fast for banned engine shortcuts.
 *
 * <p>Use this when a code path must not be called anymore - typically a direct
 * engine query that has a port-owned replacement (for example a raw
 * {@code Game.hasPath} outside {@code ReachableTiles}, or a raw
 * {@code Unit.gather} outside {@code BwapiOrderSink}). Throwing here is
 * deliberate: silently answering from the wrong source is how the OpenBW
 * region/visibility defects survived for sessions.</p>
 *
 * <p>Do NOT throw this from a query that still needs an answer every frame
 * (reachability, gathering). Those delegate to the port; this exception is
 * for paths that must die loudly, not for hot-path control flow.</p>
 */
public class DeprecatedCodeException extends UnsupportedOperationException {
    public DeprecatedCodeException(String message) {
        super(message);
    }
}
