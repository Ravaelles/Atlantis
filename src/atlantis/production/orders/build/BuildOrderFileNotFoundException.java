package atlantis.production.orders.build;

/**
 * Raised when the build order file for a strategy is missing.
 *
 * <p>Separate type on purpose: the boot path and the in-game strategy switch
 * need to tell "this strategy has no build order file on disk" apart from a
 * genuine I/O or parse failure, and a caller may want to react differently
 * (fail the run at boot, fall back to another strategy mid-game).</p>
 *
 * <p>This class owns the policy decision - a leaf file-reading helper
 * ({@code atlantis.util.AFile}) reports, it does not decide, and it must never
 * terminate the JVM.</p>
 */
public class BuildOrderFileNotFoundException extends RuntimeException {

    private final String filePath;

    public BuildOrderFileNotFoundException(String message, String filePath) {
        super(message);
        this.filePath = filePath;
    }

    /**
     * @return path of the missing file, for callers that want to log or fall back
     */
    public String filePath() {
        return filePath;
    }
}
