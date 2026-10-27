package atlantis.combat.focus;

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

    /**
     * The standing rule: a big-enough army marches, a small one holds.
     */
    HEADCOUNT,

    /**
     * No attack reason fired: hold the home ground.
     */
    HOLD_GROUND,

    /**
     * Owner order 2026-10-26: 3+ units at 140%+ relative strength march
     * instead of holding.
     */
    EARLY_STRENGTH,

    /**
     * Owner order 2026-10-27: vs Zerg, zealots leave the base only at
     * 150%+ strength or with a Dragoon - unsupported zealots donate
     * against lings and sunks.
     */
    ZEALOTS_HOLD_VS_ZERG,

    /**
     * Owner order 2026-10-26: a dragoon pair kites instead of holding -
     * always vs Zerg, vs Protoss only with no known ranged enemy (an
     * unscouted map counts as ranged: never march blind into hydras).
     * The intent is pressure from alive dragoons, not the pair as such.
     */
    EARLY_DRAGOONS_OK_SO_PRESSURE,

    /**
     * Owner order 2026-10-27: vs Terran we pressure early - the race
     * stays aggressive from 2 units or 1 Dragoon under the shared
     * strength floor.
     */
    EARLY_TERRAN_PRESSURE,

    /**
     * Owner order 2026-10-27: a discovered enemy Dragoon mass outguns
     * a small line - below 4 own Dragoons and below 1.3 eval the army
     * comes home; at 1.3+ or 4+ Dragoons the composition decides.
     */
    ENEMY_DRAGOONS_SO_COME_HOME,

    /**
     * Owner order 2026-10-26: the attack found a stronger army - at most
     * one dragoon left at 120% or less means coming home instead of
     * feeding. Ends attacks, never prevents them.
     */
    OUTGUNNED_SO_COME_HOME
}
