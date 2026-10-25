package atlantis.combat.escape;

/**
 * Escape scoring weights, tunable by the batch tuner (NEXT #172 step 5).
 *
 * <p>Same recipe as {@code CombatTuning}: the values live here, the tuner
 * installs a derived snapshot per game, and a fresh instance is impossible
 * (no setters, only {@code with...} copiers). When the tweaker is off the
 * holder stays at defaults, which equal the long-standing scoring
 * constants - registration alone changes no game.</p>
 *
 * <p>Deliberately NOT tuned: {@code BLOCKED_RAY_TILES} (defines what a
 * pocket wall is, not how much it costs) and the candidate
 * {@code RADII_TILES} (they set how many candidates exist, a structural
 * choice, not a weight).</p>
 */
public final class EscapeTuning {

    private double staticWeight = 10.0;
    private double cornerWeight = 8.0;
    private double pocketExtra = 20.0;
    private double enemyWeight = 5.0;
    private double staticRadiusTiles = 3.0;
    private double enemyRadiusTiles = 20.0;

    /**
     * Arc-retreat blend (NEXT #175): 0 = straight escape steps (today,
     * bit-identical), 1 = bend fully toward the squad center. Deliberately
     * NOT in the batch-tuner registry (like BLOCKED_RAY_TILES and
     * RADII_TILES): no sim efficacy proof yet, so no tuner dimension.
     */
    private double arcBlend = 0.0;

    private EscapeTuning() {
    }

    private static volatile EscapeTuning current = EscapeTuning.defaults();

    /** The live snapshot; the tuner replaces it per game. */
    public static EscapeTuning current() {
        return current;
    }

    /** Installs a derived snapshot - the tuner's only write path. */
    public static void install(EscapeTuning next) {
        if (next != null) current = next;
    }

    /** Engine-faithful defaults: the scoring constants as tuned by hand. */
    public static EscapeTuning defaults() {
        return new EscapeTuning();
    }

    /** Per-game reset next to the other static resets. */
    public static void resetToDefaults() {
        current = EscapeTuning.defaults();
    }

    private EscapeTuning(EscapeTuning other) {
        staticWeight = other.staticWeight;
        cornerWeight = other.cornerWeight;
        pocketExtra = other.pocketExtra;
        enemyWeight = other.enemyWeight;
        staticRadiusTiles = other.staticRadiusTiles;
        enemyRadiusTiles = other.enemyRadiusTiles;
        arcBlend = other.arcBlend;
    }

    public double getStaticWeight() {
        return staticWeight;
    }

    public double getCornerWeight() {
        return cornerWeight;
    }

    public double getPocketExtra() {
        return pocketExtra;
    }

    public double getEnemyWeight() {
        return enemyWeight;
    }

    public double getStaticRadiusTiles() {
        return staticRadiusTiles;
    }

    public double getEnemyRadiusTiles() {
        return enemyRadiusTiles;
    }

    public double getArcBlend() {
        return arcBlend;
    }

    public EscapeTuning withStaticWeight(double value) {
        EscapeTuning copy = new EscapeTuning(this);
        copy.staticWeight = value;

        return copy;
    }

    public EscapeTuning withCornerWeight(double value) {
        EscapeTuning copy = new EscapeTuning(this);
        copy.cornerWeight = value;

        return copy;
    }

    public EscapeTuning withPocketExtra(double value) {
        EscapeTuning copy = new EscapeTuning(this);
        copy.pocketExtra = value;

        return copy;
    }

    public EscapeTuning withEnemyWeight(double value) {
        EscapeTuning copy = new EscapeTuning(this);
        copy.enemyWeight = value;

        return copy;
    }

    public EscapeTuning withStaticRadiusTiles(double value) {
        EscapeTuning copy = new EscapeTuning(this);
        copy.staticRadiusTiles = value;

        return copy;
    }

    public EscapeTuning withEnemyRadiusTiles(double value) {
        EscapeTuning copy = new EscapeTuning(this);
        copy.enemyRadiusTiles = value;

        return copy;
    }

    public EscapeTuning withArcBlend(double value) {
        EscapeTuning copy = new EscapeTuning(this);
        copy.arcBlend = value;

        return copy;
    }
}
