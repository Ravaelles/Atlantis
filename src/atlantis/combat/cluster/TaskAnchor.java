package atlantis.combat.cluster;

/**
 * Where a task belongs, as a kind - never a tile. The rally code
 * resolves kinds to standoff points (ClusterRally), so the task table
 * pins without a game and the geometry stays in one place.
 */
public enum TaskAnchor {
    /** The main base itself. */
    MAIN_BASE,
    /** The main-choke standoff. */
    MAIN_CHOKE,
    /** The natural-choke standoff. */
    NATURAL_CHOKE,
    /** The middle-choke staging point. */
    MIDDLE_CHOKE,
    /** The known enemy base. */
    ENEMY_BASE
}
