package atlantis.battle;

/**
 * Immutable snapshot of the combat coefficients (NEXT #158).
 *
 * <p>These used to be {@code public static} mutable fields on
 * {@code BattleCommander}: writable by anyone, no range checks, and shared
 * across tests in one JVM. Now the values live here, the tuner installs a
 * derived snapshot per game ({@code BattleCommander.installTuning}), and
 * readers see one stable set per frame - in-place mutation of the shared
 * instance is impossible (no setters, only {@code with...} copiers).
 * Per-field range validation is the open follow-up; the tuner's
 * {@code applyOrRandomize} ranges still own the bounds.
 *
 * <p>Defaults are the engine-faithful values (PurpleWave margins, jar
 * ranges), pinned by {@code TweakerDefaultsTest}. The full rationale per
 * coefficient lives in {@code _AI/CHALLENGES/MicroKiting.md}.
 */
public final class CombatTuning {

    private double kiteEnemyTiles = 5.0;
    private double kiteGlueTiles = 0.5;
    private double kiteStepTiles = 2.0;
    private double kiteMinStepGainTiles = 0.3;
    private double kiteHuntedTiles = 0.5;
    private double kiteFacedTiles = 0.5;
    private double kiteMarginBaseTiles = 1.0;
    private double kiteMarginDyingTiles = 4.0;
    private double kiteMarginPerHitTiles = 0.25;
    private double kiteZealotPassDamage = 16.0;
    private double kiteStandoffInsetTiles = 0.5;
    private double kiteGlideDeadbandTiles = 0.5;
    private double kiteMeleeCloserTiles = 1.0;
    private double kiteMeleeEdgeTiles = 2.0;
    private double kiteWoundedHitsLeft = 6.0;
    private double kiteWoundedEdgeInsetTiles = 0.4;
    private double kiteStickyChaseTiles = 1.0;
    private int dragoonLowHp = 45;
    private double dragoonLowHpHoldTiles = 3.2;
    private int dragoonLowHpFireCooldown = 5;
    private int dragoonSurroundedMeleeCount = 3;
    private int dragoonSurroundedStandCooldown = 6;
    private double dragoonSurroundedStandDistTiles = 3.1;
    private int kiteLatencyFrames = 4;
    private int kiteRideFrames = 30;
    private int kiteRestepsEveryFrames = 10;
    private int breachRetreatHp = 40;
    private int breachRetreatCooldown = 5;
    private double breachRallyTiles = 3;
    private int umsNoRetreatFrames = 15 * 24;
    private double regroupThreatTiles = 15.0;
    private double regroupRallyTiles = 6.0;

    private CombatTuning() {
    }

    private CombatTuning(CombatTuning other) {
        kiteEnemyTiles = other.kiteEnemyTiles;
        kiteGlueTiles = other.kiteGlueTiles;
        kiteStepTiles = other.kiteStepTiles;
        kiteMinStepGainTiles = other.kiteMinStepGainTiles;
        kiteHuntedTiles = other.kiteHuntedTiles;
        kiteFacedTiles = other.kiteFacedTiles;
        kiteMarginBaseTiles = other.kiteMarginBaseTiles;
        kiteMarginDyingTiles = other.kiteMarginDyingTiles;
        kiteMarginPerHitTiles = other.kiteMarginPerHitTiles;
        kiteZealotPassDamage = other.kiteZealotPassDamage;
        kiteStandoffInsetTiles = other.kiteStandoffInsetTiles;
        kiteGlideDeadbandTiles = other.kiteGlideDeadbandTiles;
        kiteMeleeCloserTiles = other.kiteMeleeCloserTiles;
        kiteMeleeEdgeTiles = other.kiteMeleeEdgeTiles;
        kiteWoundedHitsLeft = other.kiteWoundedHitsLeft;
        kiteWoundedEdgeInsetTiles = other.kiteWoundedEdgeInsetTiles;
        kiteStickyChaseTiles = other.kiteStickyChaseTiles;
        dragoonLowHp = other.dragoonLowHp;
        dragoonLowHpHoldTiles = other.dragoonLowHpHoldTiles;
        dragoonLowHpFireCooldown = other.dragoonLowHpFireCooldown;
        dragoonSurroundedMeleeCount = other.dragoonSurroundedMeleeCount;
        dragoonSurroundedStandCooldown = other.dragoonSurroundedStandCooldown;
        dragoonSurroundedStandDistTiles = other.dragoonSurroundedStandDistTiles;
        kiteLatencyFrames = other.kiteLatencyFrames;
        kiteRideFrames = other.kiteRideFrames;
        kiteRestepsEveryFrames = other.kiteRestepsEveryFrames;
        breachRetreatHp = other.breachRetreatHp;
        breachRetreatCooldown = other.breachRetreatCooldown;
        breachRallyTiles = other.breachRallyTiles;
        umsNoRetreatFrames = other.umsNoRetreatFrames;
        regroupThreatTiles = other.regroupThreatTiles;
        regroupRallyTiles = other.regroupRallyTiles;
    }

    public static CombatTuning defaults() {
        return new CombatTuning();
    }

    /** Enemies inside this range count as kiting threats (PurpleWave threatsInPixels(160)). */
    public double getKiteEnemyTiles() {
        return kiteEnemyTiles;
    }

    /** At or under this threat distance the duel is glue: bodies overlap, no step opens. */
    public double getKiteGlueTiles() {
        return kiteGlueTiles;
    }

    /** How far the kiting step carries (tiles). */
    public double getKiteStepTiles() {
        return kiteStepTiles;
    }

    /** A step candidate has to open at least this much beyond the current distance (anti-jitter). */
    public double getKiteMinStepGainTiles() {
        return kiteMinStepGainTiles;
    }

    /** Threat committed to exactly this unit shrinks the margin by this much (tiles). */
    public double getKiteHuntedTiles() {
        return kiteHuntedTiles;
    }

    /** Threat facing this unit shrinks the margin by this much (tiles). */
    public double getKiteFacedTiles() {
        return kiteFacedTiles;
    }

    /** Margin a full-health Dragoon demands before yielding (tiles). */
    public double getKiteMarginBaseTiles() {
        return kiteMarginBaseTiles;
    }

    /** Margin a dying Dragoon demands before yielding (tiles). */
    public double getKiteMarginDyingTiles() {
        return kiteMarginDyingTiles;
    }

    /** Each survived Zealot pass lowers the demanded margin by this much (tiles). */
    public double getKiteMarginPerHitTiles() {
        return kiteMarginPerHitTiles;
    }

    /** One Zealot pass deals this much damage (2x8 swings; jar reports 8x1). */
    public double getKiteZealotPassDamage() {
        return kiteZealotPassDamage;
    }

    /** Firing point sits this far inside max range (tiles). */
    public double getKiteStandoffInsetTiles() {
        return kiteStandoffInsetTiles;
    }

    /** Glide deadband around the firing point (tiles). */
    public double getKiteGlideDeadbandTiles() {
        return kiteGlideDeadbandTiles;
    }

    /** Hold this many tiles inside max range when the threat is outranged by 2+ tiles. */
    public double getKiteMeleeCloserTiles() {
        return kiteMeleeCloserTiles;
    }

    /** Range edge for the melee-closer term (Stardust: targetRange <= myRange - 64px). */
    public double getKiteMeleeEdgeTiles() {
        return kiteMeleeEdgeTiles;
    }

    /** At or under this many survived Zealot passes the Dragoon counts as wounded. */
    public double getKiteWoundedHitsLeft() {
        return kiteWoundedHitsLeft;
    }

    /** Wounded edge inset: hold this far inside max range instead of the melee-closer. */
    public double getKiteWoundedEdgeInsetTiles() {
        return kiteWoundedEdgeInsetTiles;
    }

    /** Committed target sticks while inside weapon range plus this chase allowance (tiles). */
    public double getKiteStickyChaseTiles() {
        return kiteStickyChaseTiles;
    }

    /** Below this health (hp + shields) a Dragoon holds fire unless the shot is safe. */
    public int getDragoonLowHp() {
        return dragoonLowHp;
    }

    /** Hold-fire Dragoon shoots only at this distance or more (tiles). */
    public double getDragoonLowHpHoldTiles() {
        return dragoonLowHpHoldTiles;
    }

    /** Hold-fire exception: the weapon fires within this many frames. */
    public int getDragoonLowHpFireCooldown() {
        return dragoonLowHpFireCooldown;
    }

    /** Melee enemies inside 3 tiles that pin a Dragoon in place (escape impossible). */
    public int getDragoonSurroundedMeleeCount() {
        return dragoonSurroundedMeleeCount;
    }

    /** Surrounded Dragoon stands when the shot lands within this many frames. */
    public int getDragoonSurroundedStandCooldown() {
        return dragoonSurroundedStandCooldown;
    }

    /** Surrounded Dragoon stands when the nearest enemy is at least this far (tiles). */
    public double getDragoonSurroundedStandDistTiles() {
        return dragoonSurroundedStandDistTiles;
    }

    /** Latency frames assumed for cooldown-relative decisions (Stardust reads engine latency). */
    public int getKiteLatencyFrames() {
        return kiteLatencyFrames;
    }

    /** A reposition keeps riding this long before the attack re-arms (frames). */
    public int getKiteRideFrames() {
        return kiteRideFrames;
    }

    /** A step or glide re-issues at most this often (frames, Stardust latency+6 cadence). */
    public int getKiteRestepsEveryFrames() {
        return kiteRestepsEveryFrames;
    }

    /** Breach retreat hatch: at or under this hp a unit may leave a breached base. */
    public int getBreachRetreatHp() {
        return breachRetreatHp;
    }

    /** Breach retreat only with at least this much cooldown left (no shot to give). */
    public int getBreachRetreatCooldown() {
        return breachRetreatCooldown;
    }

    /** Rally stands this far from the main toward the main choke (tiles). */
    public double getBreachRallyTiles() {
        return breachRallyTiles;
    }

    /** UMS opening stand-ground window: no retreat for the first 15 seconds (frames). */
    public int getUmsNoRetreatFrames() {
        return umsNoRetreatFrames;
    }

    /** Threat radius the regroup rally reads: the fight the vanguard answers. */
    public double getRegroupThreatTiles() {
        return regroupThreatTiles;
    }

    /** Regroup rally distance from the threat: clear of ground range, close enough to re-engage. */
    public double getRegroupRallyTiles() {
        return regroupRallyTiles;
    }

    public CombatTuning withKiteEnemyTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteEnemyTiles = value;
        return copy;
    }

    public CombatTuning withKiteGlueTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteGlueTiles = value;
        return copy;
    }

    public CombatTuning withKiteStepTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteStepTiles = value;
        return copy;
    }

    public CombatTuning withKiteMinStepGainTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteMinStepGainTiles = value;
        return copy;
    }

    public CombatTuning withKiteHuntedTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteHuntedTiles = value;
        return copy;
    }

    public CombatTuning withKiteFacedTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteFacedTiles = value;
        return copy;
    }

    public CombatTuning withKiteMarginBaseTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteMarginBaseTiles = value;
        return copy;
    }

    public CombatTuning withKiteMarginDyingTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteMarginDyingTiles = value;
        return copy;
    }

    public CombatTuning withKiteMarginPerHitTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteMarginPerHitTiles = value;
        return copy;
    }

    public CombatTuning withKiteZealotPassDamage(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteZealotPassDamage = value;
        return copy;
    }

    public CombatTuning withKiteStandoffInsetTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteStandoffInsetTiles = value;
        return copy;
    }

    public CombatTuning withKiteGlideDeadbandTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteGlideDeadbandTiles = value;
        return copy;
    }

    public CombatTuning withKiteMeleeCloserTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteMeleeCloserTiles = value;
        return copy;
    }

    public CombatTuning withKiteMeleeEdgeTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteMeleeEdgeTiles = value;
        return copy;
    }

    public CombatTuning withKiteWoundedHitsLeft(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteWoundedHitsLeft = value;
        return copy;
    }

    public CombatTuning withKiteWoundedEdgeInsetTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteWoundedEdgeInsetTiles = value;
        return copy;
    }

    public CombatTuning withKiteStickyChaseTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteStickyChaseTiles = value;
        return copy;
    }

    public CombatTuning withDragoonLowHp(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.dragoonLowHp = value;
        return copy;
    }

    public CombatTuning withDragoonLowHpHoldTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.dragoonLowHpHoldTiles = value;
        return copy;
    }

    public CombatTuning withDragoonLowHpFireCooldown(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.dragoonLowHpFireCooldown = value;
        return copy;
    }

    public CombatTuning withDragoonSurroundedMeleeCount(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.dragoonSurroundedMeleeCount = value;
        return copy;
    }

    public CombatTuning withDragoonSurroundedStandCooldown(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.dragoonSurroundedStandCooldown = value;
        return copy;
    }

    public CombatTuning withDragoonSurroundedStandDistTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.dragoonSurroundedStandDistTiles = value;
        return copy;
    }

    public CombatTuning withKiteLatencyFrames(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteLatencyFrames = value;
        return copy;
    }

    public CombatTuning withKiteRideFrames(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteRideFrames = value;
        return copy;
    }

    public CombatTuning withKiteRestepsEveryFrames(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.kiteRestepsEveryFrames = value;
        return copy;
    }

    public CombatTuning withBreachRetreatHp(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.breachRetreatHp = value;
        return copy;
    }

    public CombatTuning withBreachRetreatCooldown(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.breachRetreatCooldown = value;
        return copy;
    }

    public CombatTuning withBreachRallyTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.breachRallyTiles = value;
        return copy;
    }

    public CombatTuning withUmsNoRetreatFrames(int value) {
        CombatTuning copy = new CombatTuning(this);
        copy.umsNoRetreatFrames = value;
        return copy;
    }

    public CombatTuning withRegroupThreatTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.regroupThreatTiles = value;
        return copy;
    }

    public CombatTuning withRegroupRallyTiles(double value) {
        CombatTuning copy = new CombatTuning(this);
        copy.regroupRallyTiles = value;
        return copy;
    }
}
