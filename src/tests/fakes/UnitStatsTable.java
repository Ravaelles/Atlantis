package tests.fakes;

import atlantis.units.AUnitType;
import atlantis.units.UnitStats;
import bwapi.WeaponType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Corrections to the engine's unit data, installed into {@link UnitStats} by
 * {@code AbstractTestWithUnits}.
 *
 * <p><b>The engine is the source, not this table.</b> The vendored
 * {@code JBWAPI-Rav.jar} carries the real Brood War dataset - verified field
 * by field against BWAPI's own reference tests
 * ({@code bwapi/BWAPILIBTest/unitTypesTest.cpp}: Marine 40 hit points, Ghost
 * 45, Vulture 80, Goliath 125, Siege Tank 150, SCV 60, Dragoon 100 + 80
 * shields, Photon Cannon 100 + 100, Sunken Colony 300, Creep Colony 400,
 * Subterranean Tentacle 224 px / 40 damage, Arclite Shock Cannon 384 px / 70
 * damage, Glaive Wurm 96 px / 9 damage, Spider Mine 20 hit points / 125
 * damage, and thirty more just like them). An earlier version of this file
 * had it backwards: it carried 56 hand-transcribed types and 30 weapons that
 * contradicted the engine on some twenty values (Marine 45, Sunken Colony
 * 150, Phase Disruptor 8 damage, Siege Tank at 5 and 6 tiles, Mutalisk at 6,
 * Lurker at 8 tiles for 50, Ghost 125 hit points, Goliath 250, Battlecruiser
 * 600, Carrier 800 with no shields, a Terran <i>Banshee</i>), and the suite
 * was green against that fiction. The {@code git} history keeps it as a
 * warning.</p>
 *
 * <p>So this table holds only what the engine gets wrong, one entry per
 * wrong field, each with the evidence. Everything else answers -1 and
 * {@link UnitStats} falls back to the engine. How to verify the engine from
 * a checkout, without a game:</p>
 * <pre>
 * CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
 * javac -cp "$CP" -d /tmp/probe Probe.java && java -cp "/tmp/probe:$CP" Probe
 * </pre>
 * <p>where {@code Probe} prints {@code maxHitPoints/maxShields/isFlyer} for
 * {@code UnitType} and {@code maxRange/damageAmount/damageFactor/damageType}
 * for {@code WeaponType}.</p>
 *
 * <p>Base values versus upgrades: the engine reports <i>unupgraded</i> stats.
 * A Dragoon shoots 4 tiles until Singularity Charge (then 6) and a Hydralisk
 * 4 tiles until Grooved Spines (then 5) - that is why the engine answers 128
 * px for both weapons, and it is correct. Upgrade-aware callers
 * ({@code OurDragoonRange}, {@code EnemyDragoonWeaponRange}) exist
 * separately; hardcoding the upgraded numbers here would erase the
 * distinction, which is exactly what the previous version of this table did.
 * The stub world researches nothing, so tests measure base stats.</p>
 */
public class UnitStatsTable implements UnitStats.Source {
    /**
     * bwapi weapon name to {range in pixels, damage of one shot, damage
     * multiplier}. {@code -1} in a field means "the engine is right, ask it".
     * ACorrection (not a guess) is the only thing that may sit here: each
     * entry states what the engine answers and why it is wrong.
     */
    private static final Map<String, int[]> WEAPONS = new HashMap<>();

    static {
        // No corrections right now. The two historical candidates turned out
        // to be base-vs-upgraded confusion (see the class javadoc), and
        // Psi_Blades' factor of 1 is compensated where the bot folds
        // maxGroundHits into the weapon (WeaponUtil, Zealot x2).
    }

    /**
     * bwapi enum name ({@code type.ut().name()}) to {hit points, shields}.
     * {@code -1} means "the engine is right, ask it". Same admission rule as
     * {@link #WEAPONS}.
     */
    private static final Map<String, int[]> NUMBERS = new HashMap<>();

    static {
        // No corrections right now.
    }

    public static void install() {
        UnitStats.useSource(new UnitStatsTable());
    }

    @Override
    public int hitPoints(AUnitType type) {
        int[] numbers = NUMBERS.get(type.ut().name());
        return numbers == null ? -1 : numbers[0];
    }

    @Override
    public int shields(AUnitType type) {
        int[] numbers = NUMBERS.get(type.ut().name());
        return numbers == null ? -1 : numbers[1];
    }

    @Override
    public int weaponRange(WeaponType weapon) {
        int[] numbers = WEAPONS.get(weapon.name());
        return numbers == null ? -1 : numbers[0];
    }

    @Override
    public int weaponDamage(WeaponType weapon) {
        int[] numbers = WEAPONS.get(weapon.name());
        return numbers == null ? -1 : numbers[1];
    }

    @Override
    public int weaponDamageFactor(WeaponType weapon) {
        int[] numbers = WEAPONS.get(weapon.name());
        return numbers == null ? -1 : numbers[2];
    }

    /**
     * Weapons with a correction in this table. Empty means the engine answers
     * every weapon question - which is the point: a correction here must be
     * earned, not typed.
     */
    public static Set<String> weaponsWithoutEntry() {
        Set<String> missing = new TreeSet<>();
        for (WeaponType weapon : WeaponType.values()) {
            if (weapon == WeaponType.None || weapon == WeaponType.Unknown) continue;
            if (!WEAPONS.containsKey(weapon.name())) {
                missing.add(weapon.name());
            }
        }
        return missing;
    }

    public static boolean hasEntry(AUnitType type) {
        return NUMBERS.containsKey(type.ut().name());
    }

    /**
     * Every unit type name in the table. A name here that no {@link AUnitType}
     * answers to is a dead entry: it would never be used and would look like
     * coverage, so {@code UnitStatsTableTest} checks each one resolves.
     */
    public static Set<String> unitEntries() {
        return new TreeSet<>(NUMBERS.keySet());
    }

    /** Every weapon name in the table, with the same reason as {@link #unitEntries()}. */
    public static Set<String> weaponEntries() {
        return new TreeSet<>(WEAPONS.keySet());
    }

    /**
     * Every unit type without a correction. That is the normal state now: the
     * engine answers, and this table only patches what it gets wrong.
     */
    public static Set<String> typesWithoutEntry() {
        Set<String> missing = new TreeSet<>();
        for (AUnitType type : AUnitType.getAllUnitTypes()) {
            if (isRealType(type) && !NUMBERS.containsKey(type.ut().name())) {
                missing.add(type.ut().name());
            }
        }
        return missing;
    }

    private static boolean isRealType(AUnitType type) {
        String name = type.ut().name();
        return !name.startsWith("Hero_") && !name.startsWith("Unknown") && !name.startsWith("Spell_");
    }
}
