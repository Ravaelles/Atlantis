package atlantis.units;

import bwapi.WeaponType;

/**
 * Hit points and shields of a unit type, which Brood War keeps in
 * {@code units.dat}.
 *
 * <p>In a game these numbers come from the engine, through
 * {@link bwapi.UnitType}. Outside a game the engine is not there: every type
 * answers 0, and the wrapper papers over that with placeholders. A test that
 * depends on how much damage a unit can take therefore measures a fiction -
 * hit points are what decides whether a Dragoon finishes a Sunken Colony or a
 * Creep Colony first, and the fake world had the Creep Colony at 400 against the
 * Sunken Colony's 300, the exact opposite of the game. See {@code _AI/NEXT.md}
 * #29.</p>
 *
 * <p>A {@link Source} installed by whoever builds the world replaces the engine
 * answer. Production installs nothing, so in a game this class is a pure
 * delegation to the engine and nothing else changes.</p>
 *
 * <p>Weapon numbers come from {@code weapons.dat} and are asked for the same
 * way. They matter as much as hit points: outside a game
 * {@link bwapi.WeaponType#damageAmount()} is 0 for every weapon in the game, so
 * "does this unit have a weapon", "how far can it shoot" and "how much does it
 * do" all answered 0 and every combat evaluation was arithmetic on zeros.</p>
 *
 * <p>A {@link Source} that does not know a type or a weapon returns -1. Falling
 * back to the engine is correct then, because a placeholder and a real number are
 * both available; guessing is not. The types and weapons a test touches without
 * an entry are listed by {@code UnitStatsTableTest} instead of being quietly
 * invented.</p>
 */
public class UnitStats {
    public interface Source {
        /**
         * @return maximum hit points of the type, or -1 when unknown.
         */
        int hitPoints(AUnitType type);

        /**
         * @return maximum shields of the type, or -1 when unknown.
         */
        int shields(AUnitType type);

        /**
         * @return maximum range of the weapon in pixels, or -1 when unknown.
         */
        int weaponRange(WeaponType weapon);

        /**
         * @return damage of one shot, or -1 when unknown.
         */
        int weaponDamage(WeaponType weapon);

        /**
         * @return damage multiplier of the weapon, or -1 when unknown.
         */
        int weaponDamageFactor(WeaponType weapon);
    }

    private static Source source = null;

    public static void useSource(Source newSource) {
        source = newSource;
    }

    public static void useEngine() {
        source = null;
    }

    public static boolean hasSource() {
        return source != null;
    }

    public static int hitPoints(AUnitType type) {
        if (source != null) {
            int hitPoints = source.hitPoints(type);
            if (hitPoints >= 0) {
                return hitPoints;
            }
        }

        return type.ut().maxHitPoints();
    }

    public static int shields(AUnitType type) {
        if (source != null) {
            int shields = source.shields(type);
            if (shields >= 0) {
                return shields;
            }
        }

        return type.ut().maxShields();
    }

    public static int weaponRange(WeaponType weapon) {
        if (source != null) {
            int range = source.weaponRange(weapon);
            if (range >= 0) {
                return range;
            }
        }

        return weapon.maxRange();
    }

    /**
     * @return weapon range in tiles, which is what every range check in the bot
     * compares against. {@link bwapi.WeaponType#maxRange()} is in pixels.
     */
    public static int weaponRangeInTiles(WeaponType weapon) {
        return weaponRange(weapon) / 32;
    }

    public static int weaponDamage(WeaponType weapon) {
        if (source != null) {
            int damage = source.weaponDamage(weapon);
            if (damage >= 0) {
                return damage;
            }
        }

        return weapon.damageAmount();
    }

    public static int weaponDamageFactor(WeaponType weapon) {
        if (source != null) {
            int factor = source.weaponDamageFactor(weapon);
            if (factor >= 0) {
                return factor;
            }
        }

        return weapon.damageFactor();
    }

    /**
     * Damage of one shot with the weapon's own multiplier applied - what the bot
     * compares and sums everywhere, and what {@code weapons.dat} describes.
     */
    public static int weaponDamageNormalized(WeaponType weapon) {
        return weaponDamage(weapon) * weaponDamageFactor(weapon);
    }
}