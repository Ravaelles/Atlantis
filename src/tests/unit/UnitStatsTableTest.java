package tests.unit;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.UnitStats;
import org.junit.jupiter.api.Test;
import tests.fakes.UnitStatsTable;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the Brood War numbers the harness supplies, because outside a game
 * {@code bwapi} answers 0 or a plausible-looking fiction for every one of them
 * and nothing else in the suite would notice.
 *
 * <p>Three things are pinned here:</p>
 * <ol>
 *   <li>the values themselves, so a typo in the table cannot quietly change a
 *       hundred tests;</li>
 *   <li>that every unit type the sample world builds has an entry - a missing one
 *       is not a failure of a test but a hole in the data, and it has to be
 *       visible;</li>
 *   <li>how much is still missing, as an upper bound. Adding entries lowers it;
 *       inventing numbers without a source does not.</li>
 * </ol>
 */
public class UnitStatsTableTest extends AbstractTestWithUnits {

    @Test
    public void broodWarHitPointsAndShields() {
        // Terran
        assertEquals(45, UnitStats.hitPoints(AUnitType.Terran_Marine));
        assertEquals(45, UnitStats.hitPoints(AUnitType.Terran_SCV));
        assertEquals(150, UnitStats.hitPoints(AUnitType.Terran_Siege_Tank_Tank_Mode));
        assertEquals(150, UnitStats.hitPoints(AUnitType.Terran_Siege_Tank_Siege_Mode));
        assertEquals(400, UnitStats.hitPoints(AUnitType.Terran_Bunker));
        assertEquals(1500, UnitStats.hitPoints(AUnitType.Terran_Command_Center));

        // Protoss: hit points and shields are both part of a unit's health
        assertEquals(40, UnitStats.hitPoints(AUnitType.Protoss_Probe));
        assertEquals(0, UnitStats.shields(AUnitType.Protoss_Probe));
        assertEquals(100, UnitStats.hitPoints(AUnitType.Protoss_Zealot));
        assertEquals(60, UnitStats.shields(AUnitType.Protoss_Zealot));
        assertEquals(100, UnitStats.hitPoints(AUnitType.Protoss_Dragoon));
        assertEquals(60, UnitStats.shields(AUnitType.Protoss_Dragoon));
        assertEquals(100, UnitStats.hitPoints(AUnitType.Protoss_Pylon));
        assertEquals(50, UnitStats.shields(AUnitType.Protoss_Pylon));
        assertEquals(150, UnitStats.hitPoints(AUnitType.Protoss_Photon_Cannon));
        assertEquals(0, UnitStats.shields(AUnitType.Protoss_Photon_Cannon));
        assertEquals(600, UnitStats.hitPoints(AUnitType.Protoss_Nexus));

        // Zerg
        assertEquals(40, UnitStats.hitPoints(AUnitType.Zerg_Drone));
        assertEquals(35, UnitStats.hitPoints(AUnitType.Zerg_Zergling));
        assertEquals(80, UnitStats.hitPoints(AUnitType.Zerg_Hydralisk));
        assertEquals(125, UnitStats.hitPoints(AUnitType.Zerg_Lurker));
        assertEquals(300, UnitStats.hitPoints(AUnitType.Zerg_Ultralisk));
        assertEquals(200, UnitStats.hitPoints(AUnitType.Zerg_Overlord));
        assertEquals(50, UnitStats.shields(AUnitType.Zerg_Overlord));
        assertEquals(150, UnitStats.hitPoints(AUnitType.Zerg_Sunken_Colony));
        assertEquals(150, UnitStats.hitPoints(AUnitType.Zerg_Spore_Colony));
        assertEquals(600, UnitStats.hitPoints(AUnitType.Zerg_Creep_Colony));
        assertEquals(600, UnitStats.hitPoints(AUnitType.Zerg_Hatchery));
    }

    @Test
    public void broodWarWeaponNumbers() {
        // A Marine shoots 4 tiles for 6, a Dragoon 6 tiles for 8, a Hydralisk
        // 5 tiles for 8, a Lurker 8 tiles for 50.
        assertEquals(4, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Gauss_Rifle));
        assertEquals(6, UnitStats.weaponDamage(bwapi.WeaponType.Gauss_Rifle));
        assertEquals(6, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Phase_Disruptor));
        assertEquals(8, UnitStats.weaponDamage(bwapi.WeaponType.Phase_Disruptor));
        assertEquals(5, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Needle_Spines));
        assertEquals(8, UnitStats.weaponDamage(bwapi.WeaponType.Needle_Spines));
        assertEquals(8, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Subterranean_Spines));
        assertEquals(50, UnitStats.weaponDamage(bwapi.WeaponType.Subterranean_Spines));

        // Melee sits below one tile, which is why groundWeaponRange() answers 0
        // for a Zealot.
        assertEquals(0, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Psi_Blades));
        assertEquals(8, UnitStats.weaponDamage(bwapi.WeaponType.Psi_Blades));
    }

    @Test
    public void everyUnitInTheSampleWorldHasAnEntry() {
        for (boolean ours : new boolean[] {true, false}) {
            for (AUnit unit : UnitTest.generateUnitsList(ours)) {
                AUnitType type = unit.type();
                if (UnitStatsTable.isNotCovered(type) || type.isSpell()) {
                    continue;
                }
                assertTrue(UnitStatsTable.hasEntry(type),
                    type.ut().name() + " has no hit points in UnitStatsTable, so "
                        + "every test that builds it measures the engine placeholder");
            }
        }
    }

    /**
     * How much of the game is still a placeholder. Measured: 133 unit types and
     * 74 weapons have no entry. Both numbers only ever go down, and each one that
     * disappears has to come with a source - the table is transcribed by hand
     * because the game's own data is inside an encrypted archive no tool here can
     * read (NEXT.md #29).
     */
    @Test
    public void howMuchIsStillMissing() {
        Set<String> missingTypes = UnitStatsTable.typesWithoutEntry();
        Set<String> missingWeapons = UnitStatsTable.weaponsWithoutEntry();

        assertTrue(missingTypes.size() <= 133,
            "unit types without an entry: " + missingTypes);
        assertTrue(missingWeapons.size() <= 74,
            "weapons without an entry: " + missingWeapons);
    }

    /**
     * Every name in the table has to match something the game knows. A typo
     * there is the quietest possible bug: the entry is never read, the type keeps
     * the placeholder, and the table looks complete.
     */
    @Test
    public void everyEntryMatchesARealTypeOrWeapon() {
        Set<String> knownTypes = new TreeSet<>();
        for (AUnitType type : AUnitType.getAllUnitTypes()) {
            knownTypes.add(type.ut().name());
        }
        for (String name : UnitStatsTable.unitEntries()) {
            assertTrue(knownTypes.contains(name), "no unit type is called " + name);
        }

        Set<String> knownWeapons = new TreeSet<>();
        for (bwapi.WeaponType weapon : bwapi.WeaponType.values()) {
            knownWeapons.add(weapon.name());
        }
        for (String name : UnitStatsTable.weaponEntries()) {
            assertTrue(knownWeapons.contains(name), "no weapon is called " + name);
        }
    }

    /**
     * The table is installed by the harness and must be visible to the code that
     * asks for numbers - otherwise everything above would be testing the engine
     * placeholder and passing by accident.
     */
    @Test
    public void theTableIsInstalled() {
        assertTrue(UnitStats.hasSource(), "UnitStatsTable.install() did not run");
        assertEquals(45, AUnitType.Terran_Marine.maxHp(),
            "and the wrapper has to read it, not cache the engine's answer");
    }
}