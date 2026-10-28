package atlantis.combat.focus;

/**
 * Why the army goes where it goes (owner: decisions carry their reasons,
 * stored on the decision and twinned, never inferred from orders).
 *
 * <p>One variant per branch of {@link AttackDecider}: adding a branch
 * without a variant is a compile-adjacent lie the twin would tell, so
 * the enum and the decider change together, pinned by
 * {@code AttackDeciderTest}.</p>
 */
public enum AttackReason {

    /**
     * A known enemy base building stands - march it, do not search around.
     */
    ENEMY_BASE_SEEN,

    /**
     * No base in sight, but the enemy main latched - march the main.
     */
    ENEMY_MAIN_KNOWN,

    /**
     * Nothing known: march the nearest unexplored start (UAlbertaBot's
     * "explore until we find a target"). Sitting home on ATTACK played
     * 18 minutes to 0/0 - measured 2026-10-17.
     */
    EXPLORE_UNEXPLORED_START,

    /**
     * Baseless or fully blind: hold our own main (which may itself be
     * null on a baseless map - the null path the old chain already had).
     */
    FALLBACK_HOME,
}
