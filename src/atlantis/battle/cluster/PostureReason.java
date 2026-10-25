package atlantis.battle.cluster;

/**
 * Why the cluster holds home ground or marches out (NEXT #197, 02_COMBAT
 * tactical routines). The posture answers once, per frame, with a named
 * reason - new defend/attack conditions land here, never as inline ifs
 * elsewhere (CONVENTIONS §31).
 */
public enum PostureReason {

    /**
     * Dragoons boxed in the base against a zealot mass cannot kite and
     * die surrounded; the same force marching out meets the pack on open
     * ground and kites it (sim-measured: 4 goons vs 8 zealots, hold loses
     * 2, march-out loses 0; vs 4 the hold wins, so the mass line is 6).
     */
    TRAPPED_BY_MASS_ZEALOTS,

    /**
     * A big army sitting on a four-digit bank spends nothing by holding -
     * the bank is unspent army (old-code StrongAndBig/beBrave shape).
     * Names the spend pressure behind an attack the headcount already
     * owns; never flips a small army alone (no provenance for blind
     * small-army banked attacks).
     */
    BANKED_MINERALS,

    /** The standing rule: a big-enough army marches, a small one holds. */
    HEADCOUNT,

    /** No attack reason fired: hold the home ground. */
    HOLD_GROUND
}
