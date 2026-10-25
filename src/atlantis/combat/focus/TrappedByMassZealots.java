package atlantis.combat.focus;


/**
 * One concrete case with one home (NEXT #197, owner: group by reasons):
 * dragoons boxed in the base against a zealot mass cannot kite and die
 * surrounded, while the same force marching out meets the pack on open
 * ground and kites it. Sim-bracketed: holding loses dragoons at 6 and 8
 * zealots, wins at 4 - so the mass line is 6, not a guess. The dragoon
 * floor and the early-game window ride the live-proven old changer
 * (GoWithGoonz shape).
 */
public final class TrappedByMassZealots {

    private TrappedByMassZealots() {
    }

    /** A line that can trade on open ground instead of donating boxed in. */
    public static final int MIN_COMBAT_UNITS = 4;

    /** Enough dragoons to hold a kite line once out. */
    public static final int MIN_DRAGOONS = 2;

    /** A zealot mass the base cannot kite around (sim-bracketed: 4 no, 6 yes). */
    public static final int MIN_ENEMY_ZEALOTS = 6;

    /** Early game, single-base economies with no room to kite at home. */
    public static final int MAX_GAME_SECONDS = 650;

    /** True when marching out beats holding the pocket. */
    public static boolean applies(int ours, int ourDragoons, int enemyZealots, int gameSeconds) {
        return ours >= MIN_COMBAT_UNITS
            && ourDragoons >= MIN_DRAGOONS
            && enemyZealots >= MIN_ENEMY_ZEALOTS
            && gameSeconds <= MAX_GAME_SECONDS;
    }
}
