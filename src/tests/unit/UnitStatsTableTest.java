package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.UnitStats;
import org.junit.jupiter.api.Test;
import tests.fakes.UnitStatsTable;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the numbers the harness supplies - which is the engine, not the
 * table. Outside a game {@code bwapi} is the only data this project has, so
 * this class pins what it answers for the values the suite depends on: if the
 * vendored jar ever changes under us, these fail first, loudly, instead of a
 * hundred combat tests drifting silently.
 *
 * <p>History that motivates the shape: an earlier {@code UnitStatsTable}
 * carried 56 hand-transcribed types that contradicted the engine on some
 * twenty values, and the suite was green against that fiction. So the table
 * now holds only corrections, and every correction needs an entry here that
 * states what the engine answers and why it is wrong. An empty table is the
 * healthy state.</p>
 */
public class UnitStatsTableTest extends AbstractTestWithUnits {

    /**
     * What the engine answers for the numbers the suite depends on. These are
     * {@code bwapi} values read through {@link UnitStats} with the table
     * installed - every one of them cross-checked against BWAPI's own
     * reference tests ({@code bwapi/BWAPILIBTest/unitTypesTest.cpp}).
     */
    @Test
    public void engineHitPointsAndShields() {
        // Terran
        assertEquals(40, UnitStats.hitPoints(AUnitType.Terran_Marine));
        assertEquals(60, UnitStats.hitPoints(AUnitType.Terran_SCV));
        assertEquals(150, UnitStats.hitPoints(AUnitType.Terran_Siege_Tank_Tank_Mode));
        assertEquals(150, UnitStats.hitPoints(AUnitType.Terran_Siege_Tank_Siege_Mode));
        assertEquals(350, UnitStats.hitPoints(AUnitType.Terran_Bunker));

        // Protoss: hit points and shields are both part of a unit's health
        assertEquals(20, UnitStats.hitPoints(AUnitType.Protoss_Probe));
        assertEquals(20, UnitStats.shields(AUnitType.Protoss_Probe));
        assertEquals(100, UnitStats.hitPoints(AUnitType.Protoss_Zealot));
        assertEquals(60, UnitStats.shields(AUnitType.Protoss_Zealot));
        assertEquals(100, UnitStats.hitPoints(AUnitType.Protoss_Dragoon));
        assertEquals(80, UnitStats.shields(AUnitType.Protoss_Dragoon));
        assertEquals(100, UnitStats.hitPoints(AUnitType.Protoss_Photon_Cannon));
        assertEquals(100, UnitStats.shields(AUnitType.Protoss_Photon_Cannon));

        // Zerg has no shields
        assertEquals(40, UnitStats.hitPoints(AUnitType.Zerg_Drone));
        assertEquals(35, UnitStats.hitPoints(AUnitType.Zerg_Zergling));
        assertEquals(80, UnitStats.hitPoints(AUnitType.Zerg_Hydralisk));
        assertEquals(125, UnitStats.hitPoints(AUnitType.Zerg_Lurker));
        assertEquals(400, UnitStats.hitPoints(AUnitType.Zerg_Ultralisk));
        assertEquals(200, UnitStats.hitPoints(AUnitType.Zerg_Overlord));
        assertEquals(0, UnitStats.shields(AUnitType.Zerg_Overlord));
        assertEquals(300, UnitStats.hitPoints(AUnitType.Zerg_Sunken_Colony));
        assertEquals(400, UnitStats.hitPoints(AUnitType.Zerg_Creep_Colony));
    }

    @Test
    public void engineWeaponNumbers() {
        // Base (unupgraded) stats: a Dragoon shoots 4 tiles until Singularity
        // Charge and a Hydralisk 4 tiles until Grooved Spines.
        assertEquals(4, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Gauss_Rifle));
        assertEquals(6, UnitStats.weaponDamage(bwapi.WeaponType.Gauss_Rifle));
        assertEquals(4, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Phase_Disruptor));
        assertEquals(20, UnitStats.weaponDamage(bwapi.WeaponType.Phase_Disruptor));
        assertEquals(4, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Needle_Spines));
        assertEquals(10, UnitStats.weaponDamage(bwapi.WeaponType.Needle_Spines));
        assertEquals(6, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Subterranean_Spines));
        assertEquals(20, UnitStats.weaponDamage(bwapi.WeaponType.Subterranean_Spines));
        assertEquals(7, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Subterranean_Tentacle));
        assertEquals(40, UnitStats.weaponDamage(bwapi.WeaponType.Subterranean_Tentacle));
        assertEquals(7, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Arclite_Cannon));
        assertEquals(30, UnitStats.weaponDamage(bwapi.WeaponType.Arclite_Cannon));
        assertEquals(12, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Arclite_Shock_Cannon));
        assertEquals(70, UnitStats.weaponDamage(bwapi.WeaponType.Arclite_Shock_Cannon));
        assertEquals(3, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Glave_Wurm));
        assertEquals(9, UnitStats.weaponDamage(bwapi.WeaponType.Glave_Wurm));

        // Melee sits below one tile, which is why groundWeaponRange() answers 0
        // for a Zealot.
        assertEquals(0, UnitStats.weaponRangeInTiles(bwapi.WeaponType.Psi_Blades));
        assertEquals(8, UnitStats.weaponDamage(bwapi.WeaponType.Psi_Blades));
    }

    /**
     * The table holds corrections, and right now there are none: the engine
     * answers every weapon and every type. If an entry appears here without a
     * test below that states what the engine answers and why it is wrong, it
     * is a guess wearing a lab coat.
     */
    @Test
    public void tableHoldsNoUnjustifiedCorrections() {
        assertTrue(UnitStatsTable.unitEntries().isEmpty(),
            "unit corrections without evidence: " + UnitStatsTable.unitEntries());
        assertTrue(UnitStatsTable.weaponEntries().isEmpty(),
            "weapon corrections without evidence: " + UnitStatsTable.weaponEntries());
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
        assertEquals(40, AUnitType.Terran_Marine.maxHp(),
            "and the wrapper has to read it, not cache the engine's answer");
    }
}
