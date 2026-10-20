package atlantis.debug.tweaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Elitist hall of fame for the tuner batch (NEXT #160, step 2).
 *
 * <p>Keeps the top-N param vectors across batches so good regions are
 * never forgotten when a later batch rolls badly. The next batch
 * mutates around a champion with a shrinking radius instead of uniform
 * random in fixed bounds - exploitation around proven ground,
 * exploration everywhere else.
 *
 * <p>Pure tooling, no game-loop callers: the shrinking schedule (halve
 * the radius each promotion round) and the file handoff between batch
 * games are orchestration policy and land with the wiring, not here.
 * What lives here is the store (bounded, ranked by {@code
 * TunerObjective}), the radius mutation (clamped to per-param bounds),
 * and a crash-proof file round-trip (malformed lines are skipped,
 * never ranked).
 */
public final class TunerHallOfFame {

    /** One archived game: its objective row plus the vector that played it. */
    public static final class Entry {
        private final TunerObjective.Row row;
        private final Map<String, Double> vector;

        public Entry(TunerObjective.Row row, Map<String, Double> vector) {
            this.row = row;
            this.vector = Collections.unmodifiableMap(new LinkedHashMap<>(vector));
        }

        public TunerObjective.Row row() {
            return row;
        }

        public Map<String, Double> vector() {
            return vector;
        }
    }

    private final int capacity;
    private final ArrayList<Entry> entries = new ArrayList<>();

    public TunerHallOfFame(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("hall of fame holds at least one entry");
        this.capacity = capacity;
    }

    /** Offers a played vector; only the top-N by objective survive. Returns true when kept. */
    public boolean offer(TunerObjective.Row row, Map<String, Double> vector) {
        if (row == null || vector == null || vector.isEmpty()) return false;
        entries.add(new Entry(row, vector));
        Collections.sort(entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry a, Entry b) {
                return TunerObjective.compare(a.row, b.row);
            }
        });
        while (entries.size() > capacity) entries.remove(entries.size() - 1);
        for (Entry kept : entries) {
            if (kept.row == row) return true;
        }
        return false;
    }

    /** The best vector ever offered, or empty when nothing was kept. */
    public Optional<Entry> champion() {
        if (entries.isEmpty()) return Optional.empty();
        return Optional.of(entries.get(0));
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Mutates around a champion: every value moves uniformly in
     * {@code +/- radius * (max - min)} and is clamped back into
     * {@code [min, max]}. Params missing from bounds ride along
     * unchanged - the caller owns the bound table, not this store.
     */
    public static Map<String, Double> mutateAround(
            Entry champion, Map<String, double[]> bounds, double radius, Random random) {
        LinkedHashMap<String, Double> mutated = new LinkedHashMap<>();
        for (Map.Entry<String, Double> param : champion.vector().entrySet()) {
            double[] bound = bounds.get(param.getKey());
            if (bound == null || radius <= 0) {
                mutated.put(param.getKey(), param.getValue());
                continue;
            }
            double span = bound[1] - bound[0];
            double shift = (random.nextDouble() * 2 - 1) * radius * span;
            mutated.put(param.getKey(), clamp(param.getValue() + shift, bound[0], bound[1]));
        }
        return mutated;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * File round-trip, one line per entry:
     * {@code game,W,hits,seconds|name=value;name=value}. Malformed lines
     * are skipped so a hand-edited or half-flushed file never breaks a batch.
     */
    public String save() {
        StringBuilder out = new StringBuilder();
        for (Entry entry : entries) {
            TunerObjective.Row row = entry.row();
            out.append(row.game()).append(",")
                .append(row.win() ? "W" : "L").append(",")
                .append(counter(row.seconds())).append(",")
                .append(counter(row.hitsTaken())).append("|");
            boolean first = true;
            for (Map.Entry<String, Double> param : entry.vector().entrySet()) {
                if (!first) out.append(";");
                out.append(param.getKey()).append("=").append(param.getValue());
                first = false;
            }
            out.append("\n");
        }
        return out.toString();
    }

    public void load(String text) {
        if (text == null) return;
        for (String line : text.split("\n")) {
            Entry entry = parseLine(line);
            if (entry != null) offer(entry.row(), entry.vector());
        }
    }

    private static String counter(int value) {
        return value == Integer.MAX_VALUE ? "?" : String.valueOf(value);
    }

    private static int parseCounter(String raw) {
        if (raw.equals("?")) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    private static Entry parseLine(String line) {
        if (line == null) return null;
        int split = line.indexOf('|');
        if (split < 0) return null;
        String[] head = line.substring(0, split).split(",", -1);
        if (head.length != 4) return null;
        if (!head[1].equals("W") && !head[1].equals("L")) return null;
        int seconds = parseCounter(head[2]);
        int hits = parseCounter(head[3]);
        if (seconds == Integer.MIN_VALUE || hits == Integer.MIN_VALUE) return null;
        LinkedHashMap<String, Double> vector = new LinkedHashMap<>();
        String tail = line.substring(split + 1);
        if (!tail.isEmpty()) {
            for (String pair : tail.split(";")) {
                int eq = pair.indexOf('=');
                if (eq < 0) return null;
                try {
                    vector.put(pair.substring(0, eq), Double.parseDouble(pair.substring(eq + 1)));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        if (vector.isEmpty()) return null;
        return new Entry(new TunerObjective.Row(head[0], head[1].equals("W"), hits, seconds), vector);
    }
}
