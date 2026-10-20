package atlantis.debug.tweaker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Batch-level manifest for the tuner (NEXT #160, step 5): one file per
 * batch that future meta-analysis reads instead of re-walking per-game
 * dirs. Pure writer: map, jar fingerprint, per-map par, then one line
 * per game (verdict + param digest + full vector, keys sorted so two
 * runs of the same batch diff cleanly).
 *
 * <p>Wiring into the batch runner rides the intensive session; what
 * lands here is the format, the digest, and the round-trip.
 */
public final class TunerBatchManifest {

    private TunerBatchManifest() {
    }

    /** One archived game: its directory, objective row and param vector. */
    public static final class Game {
        public final String dir;
        public final TunerObjective.Row row;
        public final Map<String, Double> vector;

        public Game(String dir, TunerObjective.Row row, Map<String, Double> vector) {
            this.dir = dir;
            this.row = row;
            this.vector = Collections.unmodifiableMap(new LinkedHashMap<>(vector));
        }
    }

    /** Deterministic digest of a param vector (sorted keys, full precision). */
    public static String digest(Map<String, Double> vector) {
        try {
            StringBuilder raw = new StringBuilder();
            for (Map.Entry<String, Double> e : new TreeMap<>(vector).entrySet()) {
                if (raw.length() > 0) raw.append(';');
                raw.append(e.getKey()).append('=').append(e.getValue());
            }
            byte[] hash = MessageDigest.getInstance("MD5").digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b & 0xff));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 unavailable", e);
        }
    }

    /** The whole manifest as text, deterministic in games and in params. */
    public static String render(String map, String jarFingerprint, List<Game> games) {
        StringBuilder out = new StringBuilder();
        out.append("map=").append(map == null ? "?" : map).append('\n');
        out.append("jar=").append(jarFingerprint == null ? "?" : jarFingerprint).append('\n');
        out.append("games=").append(games == null ? 0 : games.size()).append('\n');
        if (games != null) {
            for (Game g : games) {
                out.append(g.dir).append('|')
                    .append(g.row.win() ? "WIN" : "LOSS").append('|')
                    .append(g.row.hitsTaken()).append('|')
                    .append(g.row.seconds()).append('|')
                    .append(digest(g.vector)).append('|');
                boolean first = true;
                for (Map.Entry<String, Double> e : new TreeMap<>(g.vector).entrySet()) {
                    if (!first) out.append(',');
                    out.append(e.getKey()).append('=').append(e.getValue());
                    first = false;
                }
                out.append('\n');
            }
        }
        return out.toString();
    }

    /** Parses back the game dirs (header lines carry no pipe). */
    public static List<String> gameDirs(String manifest) {
        List<String> dirs = new ArrayList<>();
        if (manifest == null) return dirs;
        for (String line : manifest.split("\n")) {
            if (line.isEmpty() || line.indexOf('|') < 0) continue;
            dirs.add(line.substring(0, line.indexOf('|')));
        }
        return dirs;
    }
}
