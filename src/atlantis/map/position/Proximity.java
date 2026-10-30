package atlantis.map.position;

/**
 * Named distances for decisions (owner 2026-10-30): business logic reads
 * words, not tile counts. A chase is allowed "within close of a
 * building", never "within 2.5" - the number lives here, once, with the
 * meaning attached.
 *
 * <p>One entry per meaning, on purpose: two rules that happen to share a
 * number today get two entries, so tuning one never silently retunes the
 * other. Shared only by intent, never by coincidence.</p>
 */
public enum Proximity {

    /**
     * Close to a standing building: hugging its footprint. The zealot
     * chase veto reads this - a chase that stays inside it is home
     * defence, one that leaves it is a donation.
     */
    CLOSE_TO_BUILDING(2.5),

    /**
     * Toe-to-toe: melee contact distance. A target inside it is being
     * fought, not chased - the chase veto never fires here, whatever
     * the address reads.
     */
    CONTACT(1.0),

    /**
     * At a station: standing this close means holding it (home ground,
     * choke mouths, middle staging). Matches the home-ground gate.
     * Strictly less-than.
     */
    AT_STATION(2.0),

    /**
     * The base zone: inside it a threat is engaged, outside it a chase
     * needs a mission. The breaching raider at 14 tiles is inside, the
     * open-field pack at 16 is outside - that exact boundary is pinned,
     * not tuned.
     */
    BASE_ZONE(15.0);

    private final double tiles;

    Proximity(double tiles) {
        this.tiles = tiles;
    }

    /**
     * The tile distance this word means.
     */
    public double tiles() {
        return tiles;
    }

    /**
     * True when the point sits inside this word around the anchor.
     */
    public boolean covers(HasPosition point, HasPosition anchor) {
        if (point == null || anchor == null) return false;

        try {
            return point.distTo(anchor) <= tiles;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
