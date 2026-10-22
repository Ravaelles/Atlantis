package atlantis.debug.tweaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The tuner-batch winner objective, written down instead of eyeballed
 * (NEXT #160, step 1).
 *
 * <p>Lexicographic, deterministic: a WIN always beats a LOSS (duel first),
 * then fewer hits taken, then fewer seconds. Unknown counters (the batch
 * writes {@code ?} when a bot dies before its summary) sort after any
 * known value - a fully-instrumented row always beats a blind one.
 * Ties break on game name so rankings are stable across runs.
 *
 * <p>Pure tooling: nothing in the game loop calls this. It ranks
 * {@code batch.csv} rows after the fact; the per-map par row and the
 * K-of-N promotion bar are later steps, not folded in here.
 */
public final class TunerObjective {

    private TunerObjective() { }

    /** One {@code batch.csv} row: {@code game,result,seconds,killed,lost,hits,shots,score}. */
    public static final class Row {
        private final String game;
        private final boolean win;
        private final int hitsTaken;
        private final int seconds;
        private final boolean hitsKnown;
        private final boolean secondsKnown;

        public Row(String game, boolean win, int hitsTaken, int seconds) {
            this.game = game;
            this.win = win;
            this.hitsTaken = hitsTaken;
            this.seconds = seconds;
            this.hitsKnown = true;
            this.secondsKnown = true;
        }

        private Row(String game, boolean win, int hitsTaken, boolean hitsKnown,
                int seconds, boolean secondsKnown) {
            this.game = game;
            this.win = win;
            this.hitsTaken = hitsTaken;
            this.hitsKnown = hitsKnown;
            this.seconds = seconds;
            this.secondsKnown = secondsKnown;
        }

        public String game() {
            return game;
        }

        public boolean win() {
            return win;
        }

        public int hitsTaken() {
            return hitsTaken;
        }

        public int seconds() {
            return seconds;
        }
    }

    /**
     * Parses one {@code batch.csv} data line (header: {@code
     * game,result,seconds,killed,lost,hits,shots,score}). Unknown counters
     * ({@code ?}) become unknown fields that sort after known ones.
     * Returns null for the header or malformed lines - the caller skips
     * them, never ranks them.
     */
    public static Row parseBatchLine(String line) {
        if (line == null) return null;

        String[] cols = line.split(",", -1);
        if (cols.length < 8) return null;
        if (cols[0].trim().equalsIgnoreCase("game")) return null;

        String game = cols[0].trim();
        String result = cols[1].trim();
        if (!result.equals("W") && !result.equals("L")) return null;

        int seconds = parseOrUnknown(cols[2].trim());
        int hits = parseOrUnknown(cols[5].trim());

        return new Row(
            game, result.equals("W"),
            hits, hits != Integer.MAX_VALUE,
            seconds, seconds != Integer.MAX_VALUE);
    }

    private static int parseOrUnknown(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /** Negative when {@code a} is the better game. Win, then hits, then seconds, then name. */
    public static int compare(Row a, Row b) {
        if (a.win != b.win) return a.win ? -1 : 1;
        if (a.hitsTaken != b.hitsTaken) return Integer.compare(a.hitsTaken, b.hitsTaken);
        if (a.seconds != b.seconds) return Integer.compare(a.seconds, b.seconds);

        return a.game.compareTo(b.game);
    }

    /** Ranks best-first. The input list is not modified. */
    public static List<Row> rank(List<Row> rows) {
        ArrayList<Row> ranked = new ArrayList<>(rows);
        Collections.sort(ranked, new Comparator<Row>() {
            @Override
            public int compare(Row a, Row b) {
                return TunerObjective.compare(a, b);
            }
        });

        return ranked;
    }

    /** The single winner, or empty when there are no rows. */
    public static Optional<Row> best(List<Row> rows) {
        if (rows == null || rows.isEmpty()) return Optional.empty();

        return Optional.of(rank(rows).get(0));
    }
}
