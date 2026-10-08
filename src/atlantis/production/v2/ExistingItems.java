package atlantis.production.v2;

/**
 * "From which frame is this item available?" - the question the scheduler
 * asks before planning a prerequisite. A completed Core satisfies a Dragoon
 * now, one under construction only from its completion frame, a missing one
 * ({@link #MISSING}) must be planned.
 *
 * <p>
 * The engine's own prerequisite data says what a thing <em>requires</em>, never
 * whether the requirement is already satisfied. A Probe's prerequisite is the
 * Nexus, and without this port every worker goal plans a second Nexus (400
 * minerals) before it will schedule a 50-mineral Probe - which is exactly what
 * happened on the real engine (measured 2026-07-10: the first plan was
 * {[pre] Nexus@0-1800, Probe@0-300}, so the opening spent every mineral on a
 * base it already had).
 * </p>
 *
 * <p>
 * DIP: the scheduler is pure and asks this interface; the game adapter answers
 * from {@code Select}/{@code Count}, and a test answers from a map.
 * </p>
 */
public interface ExistingItems {

    int MISSING = -1;

    /** Absolute frame the item is available from, or {@link #MISSING}. */
    int availableFrom(Producible item);

    /** Nothing exists: the answer for a hand-built scheduler (and for tests). */
    ExistingItems NONE = new ExistingItems() {
        @Override
        public int availableFrom(Producible item) {
            return MISSING;
        }
    };
}
