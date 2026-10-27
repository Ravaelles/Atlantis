package atlantis.combat.retreating;

/**
 * Why a FLEEing unit marches, steps or stays to fight (NEXT #188, #195;
 * 02_COMBAT tactical routines). One method owns the classification
 * ({@link RetreatFromLostFight#decide}) - every new flee condition lands
 * here as a named reason, never as an inline if elsewhere
 * (CONVENTIONS §31).
 */
public enum FleeReason {

    /**
     * Tactic is not FLEE: the fight owns the unit.
     */
    STAY_TACTIC,

    /**
     * Baseless UMS map: no home to run to, fall through and fight.
     */
    STAY_BASELESS,

    /**
     * Mid-swing: a move order now cancels the shot.
     */
    STAY_MID_SWING,

    /**
     * Melee holding an enemy in weapon range with cooldown ready.
     */
    STAY_MELEE_GRAB,

    /**
     * Cut off but able to hit back: fight instead of donating a march.
     */
    FIGHT_CUTOFF,

    /**
     * Cut off and outranged: step away locally, never march far.
     */
    STEP_CUTOFF,

    /**
     * Cut off with nowhere to step: fight in place.
     */
    CORNERED_CUTOFF,

    /**
     * March to the home ground (or choke standoff for big armies).
     */
    RALLY,

    /**
     * The road home ran through the enemy: rally to the farthest clear
     * choke instead (owner 2026-10-26, tested loss vs Protoss - fleeing
     * through a dozen zealots donated the army on its own march).
     */
    ESCAPE_RALLY,

    /**
     * No point to march to: fall through and fight in place.
     */
    NOWHERE_TO_RUN
}
