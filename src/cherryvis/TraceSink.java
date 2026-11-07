package cherryvis;

/**
 * Where the rendered trace goes. The library never touches the filesystem
 * itself: the bot adapter implements this with its own file utilities (and
 * optional compression), so game-end I/O policy stays on the bot side.
 */
public interface TraceSink {
    void writeTextFile(String path, String content);
}
