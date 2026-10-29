package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fog-of-war memory for sim scenarios (owner spec 2026-10-29): every
 * {@link #UPDATE_EVERY_FRAMES} frames, each known enemy unit is visible
 * iff any of our units is within {@link #SIGHT_TILES} tiles of it,
 * otherwise it stays in fog with its last-seen position.
 *
 * <p>Deliberately crude: no sight cones, no terrain occlusion, no
 * detector types - the sim models what its tile snapshot can see, and
 * engine specifics (J BWAPI visibility, cloak, burrow) stay live-only.
 * A scenario that needs those uses a live game, not this class.</p>
 */
public final class SimFog {

    /**
     * Visibility radius in tiles (owner rule): at 8 or closer the unit
     * is seen, past 8 it is not.
     */
    public static final double SIGHT_TILES = 8;

    /**
     * Visibility is re-evaluated this often (owner rule): between two
     * evaluations the last verdict holds, which is exactly what makes
     * stale contacts possible in the first place.
     */
    public static final int UPDATE_EVERY_FRAMES = 15;

    /**
     * What the sim remembers about one known enemy unit.
     */
    public static final class Contact {
        public final boolean visible;
        public final int lastSeenFrame;
        public final int lastSeenTx;
        public final int lastSeenTy;

        private Contact(boolean visible, int lastSeenFrame, int lastSeenTx, int lastSeenTy) {
            this.visible = visible;
            this.lastSeenFrame = lastSeenFrame;
            this.lastSeenTx = lastSeenTx;
            this.lastSeenTy = lastSeenTy;
        }

        private static Contact unseen() {
            return new Contact(false, -1, -1, -1);
        }
    }

    private final Map<Integer, Contact> contacts = new HashMap<>();

    /**
     * Re-evaluates visibility on cadence frames ({@link #UPDATE_EVERY_FRAMES});
     * on other frames only registers newcomers as unseen, so the first
     * sighting still waits for the next evaluation.
     */
    public void update(int frame, List<? extends AUnit> ours, List<? extends AUnit> enemies) {
        if (ours == null || enemies == null) return;

        for (AUnit foe : enemies) {
            contacts.putIfAbsent(foe.id(), Contact.unseen());
        }

        if (frame % UPDATE_EVERY_FRAMES != 0) return;

        for (AUnit foe : enemies) {
            boolean seen = anyWithinSight(ours, foe);
            Contact previous = contacts.get(foe.id());

            if (seen) {
                APosition pos = foe.position();
                contacts.put(foe.id(), new Contact(true, frame, pos.tx(), pos.ty()));
            }
            else if (previous == null || previous.visible) {
                // Stays unseen, keeps whatever it remembered (or nothing).
                Contact old = previous == null ? Contact.unseen() : previous;
                contacts.put(foe.id(), new Contact(false, old.lastSeenFrame, old.lastSeenTx, old.lastSeenTy));
            }
        }
    }

    /**
     * Current verdict for a known enemy unit; unknown units read unseen.
     */
    public boolean isVisible(int unitId) {
        Contact contact = contacts.get(unitId);

        return contact != null && contact.visible;
    }

    /**
     * Whatever is remembered about a known enemy unit; unseen if unknown.
     */
    public Contact contact(int unitId) {
        Contact contact = contacts.get(unitId);

        return contact == null ? Contact.unseen() : contact;
    }

    private static boolean anyWithinSight(List<? extends AUnit> ours, AUnit foe) {
        for (AUnit own : ours) {
            if (own == null || !own.isAlive()) continue;

            if (own.position().distTo(foe.position()) <= SIGHT_TILES) return true;
        }

        return false;
    }
}
