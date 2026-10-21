package atlantis.units;

/**
 * Shield predicates, extracted from {@link AUnit} (NEXT #11): pure shield
 * arithmetic over {@code shields()/maxShields()} with no engine calls -
 * the exact analogue of the moved wound family in {@code AUnitHealth}.
 * {@code AUnit} keeps one-line delegates. The NaN contract travels with
 * the code, not just the name: it is the documented reason Terran and
 * Zerg units read false on both guard shapes.
 */
public final class AUnitShield {

    private AUnitShield() {
    }

    public static boolean shieldHealthy(AUnit unit) {
        return unit.shields() >= unit.maxShields();
    }

    /**
     * How much of this unit's shield is still up, as a percentage.
     *
     * <p><b>Undefined for a unit that cannot have shields</b> (every Terran and
     * Zerg unit, and Protoss units before the shield battery is researched): the
     * answer is {@code NaN}, and every comparison against {@code NaN} is false.
     * That is deliberate. All three shield-based rules in production are written
     * as guards of the shape {@code shieldWound() <= x} ("my shields are nearly
     * intact, so back off") or {@code shieldWound() >= x} ("wounded badly
     * enough to run"), and {@code NaN} is the only value that makes *both* read
     * as "this rule does not apply to me".</p>
     *
     * <p>The alternatives were measured and are worse: returning {@code 0} would
     * make {@code shieldWound() <= x} true for a Terran unit and switch off rules
     * that Terran air and workers currently rely on (e.g.
     * {@code PreventAirCornerStucking}); returning {@code 100} would make
     * {@code shieldWound() >= x} true and switch on rules Terran units should
     * never see. Neither can be justified without a game run, and neither is
     * covered by a test.</p>
     *
     * <p>So: guard a call with {@code maxShields()} if you need a number rather
     * than a decision. {@code AUnitTest.shieldsOnAUnitThatHasNone} is the
     * pin.</p>
     */
    public static double shieldPercent(AUnit unit) {
        return (double) (100 * unit.shields()) / unit.maxShields();
    }

    /**
     * {@code shieldPercent()}'s wound-side twin: the share of the shield that is gone.
     * {@code shieldWound()} already answers in percent, so this is it, not
     * a second scaling (the old spelling multiplied by {@code 100/maxShields}
     * again - 83 for a half shield - and nothing ever called it).
     */
    public static double shieldWoundPercent(AUnit unit) {
        return shieldWound(unit);
    }

    /**
     * How much of this unit's shield is gone, as a percentage. Undefined
     * ({@code NaN}) for a unit without shields - see {@code #shieldPercent()}
     * for why that is the contract rather than an accident.
     */
    public static double shieldWound(AUnit unit) {
        return 100 - ((double) (100 * unit.shields()) / unit.maxShields());
    }

    public static boolean shieldWounded(AUnit unit) {
        return unit.shields() < unit.maxShields();
    }

    public static boolean shieldDamageAtMost(AUnit unit, int maxDamage) {
        if (!atlantis.util.We.protoss()) return false;

        return unit.shields() + maxDamage >= unit.maxShields();
    }

    public static boolean shieldDamageAtLeast(AUnit unit, int minDamage) {
        if (!atlantis.util.We.protoss()) return false;

        return unit.shields() + minDamage <= unit.maxShields();
    }
}
