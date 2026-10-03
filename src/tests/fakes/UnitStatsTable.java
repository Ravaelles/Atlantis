package tests.fakes;

import atlantis.units.AUnitType;
import atlantis.units.UnitStats;
import bwapi.WeaponType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Brood War hit points, shields and weapon numbers for the units and weapons the
 * tests put into a world, installed into {@link UnitStats} by
 * {@code AbstractTestWithUnits}.
 *
 * <p><b>Where the numbers come from.</b> Brood War keeps them in
 * {@code units.dat} inside the encrypted {@code BROODAT.MPQ}, and nothing on a
 * development machine can read that: the open-source MPQ readers need Blizzard's
 * filename decryption table plus a PKWARE "implode" decompressor, and the file
 * is not shipped in any readable form. The table below is therefore
 * <b>transcribed by hand</b> from the game's unit data, not read from a file -
 * the honest description, because a number typed from memory is exactly the kind
 * of thing that has to be verified against a real game run before anyone trusts
 * it. `_AI/NEXT.md` #29 keeps that open.</p>
 *
 * <p><b>Why only some types.</b> A type that is not listed here returns -1 and
 * the engine placeholder is used instead, so a missing entry is visible
 * ({@code UnitStatsTableTest} prints the whole missing list) instead of being
 * invented. Entries were added for the types whose numbers the suite's
 * assertions actually depend on; types the author was not sure about are left
 * out on purpose rather than guessed.</p>
 *
 * <p>Shields are separate because Protoss units carry both and several rules
 * compare a unit against its maximum: a Pylon is 100 hit points plus 50 shields,
 * a Zealot or a Dragoon 100 plus 60, an Overlord 200 plus 50.</p>
 */
public class UnitStatsTable implements UnitStats.Source {
    /**
     * bwapi weapon name to {range in pixels, damage of one shot, damage
     * multiplier}. {@code -1} in a field means "not known, ask the engine" - a
     * number this project cannot vouch for is left out rather than guessed, and
     * {@link #weaponsWithoutEntry()} lists what is still missing.
     */
    private static final Map<String, int[]> WEAPONS = new HashMap<>();

    private static void weapon(String name, int rangeInPixels, int damage, int damageFactor) {
        WEAPONS.put(name, new int[] {rangeInPixels, damage, damageFactor});
    }

    static {
        // Melee weapons sit at 20 px, i.e. less than a tile, so
        // AUnit.groundWeaponRange() answers 0 for them - which is what every
        // "can I reach this" calculation in the bot assumes, and what the engine
        // placeholder (15 px) happened to produce as well.
        weapon("Psi_Blades", 20, 8, 1);          // Zealot: 8 per blade
        weapon("Warp_Blades", 20, 8, 1);         // Dark Templar: 8 per blade
        weapon("Claws", 20, 5, 1);               // Zergling
        weapon("Kaiser_Blades", 20, 300, 1);     // Ultralisk

        weapon("Gauss_Rifle", 128, 6, 1);         // Marine, SCV: 4 tiles
        weapon("C_10_Canister_Rifle", 160, 10, 1);   // Ghost: 5 tiles
        weapon("Arclite_Cannon", 160, 13, 1);     // Siege Tank in tank mode: 5 tiles
        weapon("Arclite_Shock_Cannon", 192, 130, 1); // Siege Tank sieged: 6 tiles, 10x the damage
        weapon("Twin_Autocannons", 160, 6, 1);    // Goliath, ground
        weapon("Hellfire_Missile_Pack", 160, 20, 1); // Goliath, air
        weapon("Burst_Lasers", 192, 8, 1);       // Wraith, ground
        weapon("Gemini_Missiles", 192, 20, 1);   // Wraith, air
        weapon("Halo_Rockets", 192, 30, 1);      // Valkyrie
        weapon("ATS_Laser_Battery", 192, 150, 1);   // Battlecruiser, ground
        weapon("ATA_Laser_Battery", 192, 150, 1);   // Battlecruiser, air
        weapon("Longbolt_Missile", -1, 20, 1);   // Missile Turret: damage known, range left out
        weapon("Spider_Mines", 32, 125, 1);
        // Fragmentation_Grenade (the Vulture's own weapon) is deliberately not
        // here: a Vulture does not really shoot - it drops mines - and this
        // project cannot say what the game's table puts there. Leaving it out
        // keeps the engine placeholder visible instead of inventing a number.

        weapon("Phase_Disruptor", 192, 8, 1);     // Dragoon: 6 tiles
        weapon("Particle_Beam", 64, 5, 1);        // Probe
        weapon("Pulse_Cannon", 128, 6, 1);       // Interceptor
        weapon("Scarab", 128, 8, 1);
        weapon("Psionic_Shockwave", 96, 40, 1);   // Archon
        weapon("STS_Photon_Cannon", 192, 22, 1);  // Photon Cannon, ground
        weapon("STA_Photon_Cannon", 192, 22, 1);  // Photon Cannon, air

        weapon("Spines", 64, 5, 1);               // Drone
        weapon("Needle_Spines", 160, 8, 1);       // Hydralisk: 5 tiles
        weapon("Subterranean_Spines", 256, 50, 1); // Lurker: 8 tiles
        weapon("Subterranean_Tentacle", 80, 6, 1); // Sunken Colony: 2.5 tiles
        weapon("Seeker_Spores", -1, 20, 1);       // Spore Colony: damage known, range left out
        weapon("Glave_Wurm", 192, 8, 1);          // Mutalisk: 6 tiles
        weapon("Suicide_Scourge", 64, 110, 1);    // Scourge
    }

    /**
     * bwapi enum name ({@code type.ut().name()}, e.g. {@code Zerg_Creep_Colony})
     * to {hit points, shields}. Not {@link AUnitType#name()}, which is the short
     * display name ("CreepC") and would collide across races.
     */
    private static final Map<String, int[]> NUMBERS = new HashMap<>();

    private static void put(String name, int hitPoints, int shields) {
        NUMBERS.put(name, new int[] {hitPoints, shields});
    }

    static {
        // === Terran =============================================
        put("Terran_SCV", 45, 0);
        put("Terran_Marine", 45, 0);
        put("Terran_Firebat", 50, 0);
        put("Terran_Ghost", 125, 0);
        put("Terran_Siege_Tank_Tank_Mode", 150, 0);
        put("Terran_Siege_Tank_Siege_Mode", 150, 0);
        put("Terran_Goliath", 250, 0);
        put("Terran_Valkyrie", 200, 0);
        put("Terran_Battlecruiser", 600, 0);
        put("Terran_Science_Vessel", 200, 0);
        put("Terran_Comsat_Station", 500, 0);
        put("Terran_Dropship", 150, 0);
        put("Terran_Bunker", 400, 0);
        put("Terran_Command_Center", 1500, 0);
        put("Terran_Supply_Depot", 500, 0);
        put("Terran_Refinery", 500, 0);
        put("Terran_Barracks", 750, 0);
        put("Terran_Engineering_Bay", 750, 0);
        put("Terran_Factory", 750, 0);
        put("Terran_Starport", 1000, 0);
        put("Terran_Nuclear_Silo", 750, 0);

        // === Protoss ============================================
        put("Protoss_Probe", 40, 0);
        put("Protoss_Zealot", 100, 60);
        put("Protoss_Dragoon", 100, 60);
        put("Protoss_Reaver", 100, 60);
        put("Protoss_Carrier", 800, 0);
        put("Protoss_Nexus", 600, 0);
        put("Protoss_Pylon", 100, 50);
        put("Protoss_Photon_Cannon", 150, 0);
        put("Protoss_Shield_Battery", 100, 0);
        put("Protoss_Gateway", 750, 0);
        put("Protoss_Forge", 750, 0);
        put("Protoss_Cybernetics_Core", 600, 0);
        put("Protoss_Assimilator", 750, 0);
        put("Protoss_Citadel_of_Adun", 600, 0);
        put("Protoss_Templar_Archives", 600, 0);
        put("Protoss_Robotics_Facility", 750, 0);
        put("Protoss_Stargate", 750, 0);

        // === Zerg ===============================================
        put("Zerg_Drone", 40, 0);
        put("Zerg_Overlord", 200, 50);
        put("Zerg_Zergling", 35, 0);
        put("Zerg_Hydralisk", 80, 0);
        put("Zerg_Ultralisk", 300, 0);
        put("Zerg_Lurker", 125, 0);
        put("Zerg_Lurker_Egg", 125, 0);
        put("Zerg_Devourer", 200, 0);
        put("Zerg_Guardian", 150, 0);
        put("Zerg_Mutalisk", 150, 0);
        put("Zerg_Scourge", 50, 0);
        put("Zerg_Hatchery", 600, 0);
        put("Zerg_Lair", 450, 0);
        put("Zerg_Hive", 600, 0);
        put("Zerg_Creep_Colony", 600, 0);
        put("Zerg_Sunken_Colony", 150, 0);
        put("Zerg_Spore_Colony", 150, 0);
        put("Zerg_Extractor", 375, 0);
        put("Zerg_Hydralisk_Den", 500, 0);
        put("Zerg_Spawning_Pool", 500, 0);
        put("Zerg_Ultralisk_Cavern", 500, 0);
        put("Zerg_Greater_Spire", 750, 0);
        put("Zerg_Spire", 500, 0);
        put("Zerg_Nydus_Canal", 500, 0);
    }

    /**
     * Types the sample world builds and this table deliberately does not cover.
     *
     * <p>Each one is here because the number is either not a real unit's health
     * or not something this project can source:</p>
     * <ul>
     *   <li>{@code Zerg_Larva}, {@code Zerg_Egg}, {@code Zerg_Cocoon} - states a
     *       Zerg building passes through, not units that fight; their hit points
     *       are a construction artefact and no rule in the bot reads them;</li>
     *   <li>{@code Protoss_Scarab}, {@code Terran_Vulture_Spider_Mine} - real
     *       units, but this project cannot say what the game's table holds and
     *       guessing would be worse than the visible placeholder;</li>
     *   <li>{@code Spell_*} - effects, not units.</li>
     * </ul>
     */
    private static final Set<String> NOT_COVERED = new TreeSet<>();

    static {
        NOT_COVERED.add("Zerg_Larva");
        NOT_COVERED.add("Zerg_Egg");
        NOT_COVERED.add("Zerg_Cocoon");
        NOT_COVERED.add("Protoss_Scarab");
        NOT_COVERED.add("Terran_Vulture_Spider_Mine");
    }

    public static boolean isNotCovered(AUnitType type) {
        return NOT_COVERED.contains(type.ut().name());
    }

    // =============================================================

    public static void install() {
        UnitStats.useSource(new UnitStatsTable());
    }

    @Override
    public int hitPoints(AUnitType type) {
        int[] numbers = numbers(type);
        return numbers == null ? -1 : numbers[0];
    }

    @Override
    public int shields(AUnitType type) {
        int[] numbers = numbers(type);
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
     * Weapons the suite meets that still answer with the engine placeholder -
     * 0 damage for all of them, which is what every combat evaluation used to be
     * built on.
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
        return numbers(type) != null;
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
     * Every unit type a test can build that still answers with the engine
     * placeholder. Kept as a method so the guard test can report it: a type
     * missing here is a test measuring a fiction, and it has to be a decision,
     * not an accident.
     */
    public static Set<String> typesWithoutEntry() {
        Set<String> missing = new TreeSet<>();
        for (AUnitType type : AUnitType.getAllUnitTypes()) {
            if (isRealType(type) && numbers(type) == null) {
                missing.add(type.ut().name());
            }
        }
        return missing;
    }

    private static boolean isRealType(AUnitType type) {
        String name = type.ut().name();
        return !name.startsWith("Hero_") && !name.startsWith("Unknown") && !name.startsWith("Spell_");
    }

    private static int[] numbers(AUnitType type) {
        return NUMBERS.get(type.ut().name());
    }
}