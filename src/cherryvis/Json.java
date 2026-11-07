package cherryvis;

/**
 * Minimal JSON string escaper for the trace format.
 *
 * <p>The trace is plain JSON the viewer opens directly, so every free-text
 * message passes through here. Pure java.lang, no dependencies.
 */
public final class Json {
    private Json() {
    }

    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
