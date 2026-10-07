package atlantis.production.v2;

/**
 * "Do we already have one of these?" - the question the scheduler must ask
 * before planning a prerequisite.
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
 * from {@code Select}/{@code Count}, and a test answers from a set. Units and
 * buildings are both "existing" - a Gateway counts as satisfied whether it is
 * finished or under construction, because planning a second one is not what a
 * prerequisite check is for.
 * </p>
 */
public interface ExistingItems {

    /** True when the item exists in the game, or is under construction. */
    boolean have(Producible item);

    /** Nothing exists: the answer for a hand-built scheduler (and for tests). */
    ExistingItems NONE = new ExistingItems() {
        @Override
        public boolean have(Producible item) {
            return false;
        }
    };
}
