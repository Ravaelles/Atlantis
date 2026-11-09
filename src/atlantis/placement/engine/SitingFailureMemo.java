package atlantis.placement.engine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remembers freshly failed siting hunts so an identical demand does not
 * re-scan the map every frame (owner 2026-11-09, timeout hunt): a
 * failing hunt (power drought, blocked base) re-ran identically each
 * frame, burning ~30 ms/frame for minutes (measured live: Placement
 * 24 s in one 720-frame bucket). A hit answers the stored refusal
 * instead of hunting.
 *
 * <p>Scope, deliberately narrow: failures only (a success mints and the
 * row owns the tile), empty exclusion sets only (funnel retries explore
 * different space), TTL-bounded (240 frames - legality flips only on
 * warps, cancels, new Pylons or destroyed buildings, so a stale refusal
 * delays a newly-legal tile by at most 10 s and heals itself). Passes
 * that declared power support never memoize: the support chain must
 * keep declaring until it is served.
 */
public final class SitingFailureMemo {

    /**
     * How long a failure stays an answer (frames). Stale at most this
     * long after a flip, then the hunt re-runs and re-memoizes.
     */
    public static final int FAILURE_TTL_FRAMES = 240;

    private static final int MAX_ENTRIES = 64;

    private final Map<Key, Entry> failures = new LinkedHashMap<Key, Entry>() {
        @Override
        protected boolean removeEldestEntry(
            java.util.Map.Entry<SitingFailureMemo.Key, SitingFailureMemo.Entry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    /**
     * The stored refusal for an identical hunt that failed inside the
     * TTL window, or null (hunt it). Never throws.
     */
    public String recentRefusal(Key key, int nowFrame) {
        try {
            Entry entry = failures.get(key);
            if (entry == null) return null;
            if (nowFrame - entry.frame > FAILURE_TTL_FRAMES) {
                failures.remove(key);

                return null;
            }

            return entry.refusal;
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    /**
     * Remembers a failed hunt. A null refusal is not stored (nothing to
     * name on a hit).
     */
    public void noteFailure(Key key, String refusal, int nowFrame) {
        try {
            if (key == null || refusal == null) return;
            failures.put(key, new Entry(refusal, nowFrame));
        } catch (Exception | Error ignored) {
        }
    }

    public void clearForTests() {
        failures.clear();
    }

    public int sizeForTests() {
        return failures.size();
    }

    /**
     * What makes two hunts identical: the building, the search centre
     * bucket (8 tiles - the ranker decides order inside it, legality
     * does not flip within it), and the constraint shape.
     */
    public static final class Key {
        private final String typeId;
        private final int centreBucketX;
        private final int centreBucketY;
        private final String mode;
        private final int radius;

        public Key(String typeId, int centreTx, int centreTy, String mode, int radius) {
            this.typeId = typeId == null ? "-" : typeId;
            this.centreBucketX = centreTx / 8;
            this.centreBucketY = centreTy / 8;
            this.mode = mode == null ? "-" : mode;
            this.radius = radius;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key)) return false;
            Key that = (Key) other;

            return typeId.equals(that.typeId)
                && centreBucketX == that.centreBucketX
                && centreBucketY == that.centreBucketY
                && mode.equals(that.mode)
                && radius == that.radius;
        }

        @Override
        public int hashCode() {
            int result = typeId.hashCode();
            result = 31 * result + centreBucketX;
            result = 31 * result + centreBucketY;
            result = 31 * result + mode.hashCode();
            result = 31 * result + radius;

            return result;
        }
    }

    private static final class Entry {
        final String refusal;
        final int frame;

        Entry(String refusal, int frame) {
            this.refusal = refusal;
            this.frame = frame;
        }
    }
}
