package atlantis.util.log;

/**
 * Output port (ADR 0001, {@code LogPort}).
 *
 * <p>Every console/banner write in production code goes through this
 * interface, so the destination is a decision of the composition root rather
 * than a hard-wired {@code System.out}. {@link SystemLogPort} is the live
 * implementation; tests substitute a recording one.</p>
 *
 * <p>Deliberately tiny: a port per {@code bwapi} call is an anti-pattern, and
 * output formatting (joining lists, prefixing frames) stays in
 * {@code atlantis.util.AConsole}, not here.</p>
 */
public interface LogPort {

    /**
     * Writes {@code message} without a line break.
     */
    void print(Object message);

    /**
     * Writes {@code message} followed by a line break.
     */
    void println(Object message);

    /**
     * Writes {@code message} followed by a line break to the error stream.
     */
    void printError(Object message);

    /**
     * Reports a fatal or unexpected situation: a banner on the error stream
     * plus the current thread's stack.
     */
    void printStackTrace(String message);
}
