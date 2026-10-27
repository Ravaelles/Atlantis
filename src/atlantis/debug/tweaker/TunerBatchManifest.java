package atlantis.debug.tweaker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

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

    /**
     * Parses back the game rows of a manifest written by {@link #render}:
     * {@code dir|WIN|hits|seconds|digest|k=v,...} lines; header lines and
     * malformed rows are skipped, never ranked. The digest is an integrity
     * hint for humans, not a gate - a hand-tweaked vector still loads, so
     * the loop never stalls on a cosmetic field.
     */
    public static java.util.List<Game> parseGames(String manifest) {
        java.util.List<Game> games = new java.util.ArrayList<>();
        if (manifest == null) return games;

        for (String line : manifest.split("\n")) {
            if (line.isEmpty() || line.indexOf('|') < 0) continue;
            String[] cols = line.split("\\|", -1);
            if (cols.length != 6) continue;
            String dir = cols[0].trim();
            boolean win;
            if (cols[1].trim().equals("WIN")) win = true;
            else if (cols[1].trim().equals("LOSS")) win = false;
            else continue;
            int hits;
            int seconds;
            try {
                hits = Integer.parseInt(cols[2].trim());
                seconds = Integer.parseInt(cols[3].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            java.util.LinkedHashMap<String, Double> vector = new java.util.LinkedHashMap<>();
            boolean bad = cols[5].trim().isEmpty();
            if (!bad) {
                for (String pair : cols[5].split(",")) {
                    int eq = pair.indexOf('=');
                    if (eq < 0) {
                        bad = true;
                        break;
                    }
                    try {
                        vector.put(pair.substring(0, eq), Double.parseDouble(pair.substring(eq + 1)));
                    } catch (NumberFormatException e) {
                        bad = true;
                        break;
                    }
                }
            }
            if (bad || vector.isEmpty()) continue;
            games.add(new Game(dir, new TunerObjective.Row(dir, win, hits, seconds), vector));
        }
        return games;
    }

    /**
     * The played map from the manifest header, or null when absent.
     */
    public static String parseMap(String manifest) {
        if (manifest == null) return null;

        for (String line : manifest.split("\n")) {
            if (line.startsWith("map=") && line.length() > 4) return line.substring(4).trim();
        }
        return null;
    }

    /**
     * Deterministic digest of a param vector (sorted keys, full precision).
     */
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

    /**
     * The whole manifest as text, deterministic in games and in params.
     */
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

    /**
     * Parses back the game dirs (header lines carry no pipe).
     */
    public static List<String> gameDirs(String manifest) {
        List<String> dirs = new ArrayList<>();
        if (manifest == null) return dirs;

        for (String line : manifest.split("\n")) {
            if (line.isEmpty() || line.indexOf('|') < 0) continue;
            dirs.add(line.substring(0, line.indexOf('|')));
        }
        return dirs;
    }

    /**
     * One archived game: its directory, objective row and param vector.
     */
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
}
