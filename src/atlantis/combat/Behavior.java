package atlantis.combat;

/**
 * What a combat unit is doing this frame and why (owner 2026-10-26).
 *
 * <p>The codebase used to scatter these meanings as string literals
 * ({@code "ClusterKite"}, {@code "FormHold"}, ...) across every file
 * that issues an order - same battle, fifteen nameless spellings, no
 * place to learn the vocabulary. The vocabulary now lives here, once:
 * each behavior is a named thing with the journal tag it renders as.</p>
 *
 * <p>Behavior is the <i>reason</i>, {@code Action} is the <i>order</i>:
 * {@code unit.move(dest, Actions.MOVE_ENGAGE, Behavior.KITE.tag())}
 * reads as "move to dest because we kite". The tag strings are frozen
 * (death logs and tooling read them); only the names are new.</p>
 */
public enum Behavior {

    /**
     * Hold formation slots facing the expected enemy.
     */
    HOLD_FORMATION("FormHold"),
    /**
     * Advance in formation toward the expected enemy.
     */
    ADVANCE_FORMATION("ClusterForm"),
    /**
     * Yield ground from a threat, shoot when ready.
     */
    KITE("ClusterKite"),
    /**
     * Duel standoff: yield to hold distance, never shove past it.
     */
    DUEL_YIELD("ClusterDuelYield"),
    /**
     * Glide to the firing position, shoot on arrival.
     */
    REENGAGE("ClusterReengage"),
    /**
     * March at the freshest fight instead of holding slots.
     */
    REINFORCE("ClusterReinforce"),
    /**
     * Hunt the last remembered contact on a baseless map.
     */
    HUNT("ClusterHunt"),
    /**
     * Step out of an ally's escape ray.
     */
    MAKE_WAY("ClusterMakeWay"),
    /**
     * STOP a wedged order queue, then start over.
     */
    UNSTICK("ClusterUnstick"),
    /**
     * Creep toward a remembered enemy after the head start.
     */
    GHOST_CREEP("ClusterGhost"),
    /**
     * The vanguard scout walks out ahead of the line.
     */
    GHOST_SCOUT("ClusterScout"),
    /**
     * Bunch on the leader when nothing else owns the unit.
     */
    GATHER("ClusterMove"),
    /**
     * Run from a lost fight toward the retreat anchor.
     */
    FLEE("ClusterFlee"),
    /**
     * Walk home in formation when the fight is lost but nothing chases
     * (owner 2026-10-23): no visible pursuer means no panic scatter -
     * the same home rally as FLEE, shared by the whole cluster.
     */
    ORGANIZED_RETREAT("ClusterOrganizedRetreat"),
    /**
     * Collapse into the funnel mouth onto the bait.
     */
    COLLAPSE("ClusterCollapse"),
    /**
     * Leave a breached base instead of defending it.
     */
    BREACH_RETREAT("ClusterBreachRetreat"),
    /**
     * Bait the enemy into the funnel while the line holds.
     */
    BAIT("ClusterBait"),
    /**
     * Hold the chokepoint standoff line.
     */
    STANDOFF("ClusterStandoff");

    private final String tag;

    Behavior(String tag) {
        this.tag = tag;
    }

    /**
     * The journal/death-log tag this behavior renders as. Frozen - do not rename.
     */
    public String tag() {
        return tag;
    }

    @Override
    public String toString() {
        return tag;
    }
}
