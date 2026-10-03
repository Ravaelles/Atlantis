package tests.acceptance;

import atlantis.protoss.shuttle.ProtossShuttleEmptyAvoidEnemies;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * `ProtossShuttleEmptyAvoidEnemies` used to compute its safety margin as
 * {@code 1.5 + unit.shieldWoundPercent() / 25.0}, and
 * {@code shieldWoundPercent()} is {@code NaN} for a unit that cannot have
 * shields. {@code canAttack(unit, NaN)} compares {@code dist <= range + NaN},
 * which is false for every distance, so the selection came back empty and the
 * manager never applied - a shuttle would have stood next to a firing line
 * believing nobody could reach it.
 *
 * <p>The subject here is a Terran Marine rather than a Shuttle on purpose: the
 * manager only ever runs for a Shuttle, and whether a Shuttle has shields in
 * Brood War is not something this project can source (see
 * {@code tests/fakes/UnitStatsTable}). A Marine certainly has none, so it is
 * the unit that exercises the undefined value.</p>
 */
public class ProtossShuttleEmptyAvoidEnemiesTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Terran;
    }

    @Test
    public void aUnitWithoutShieldsStillSeesTheEnemyThatCanReachIt() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);

        world(1, fakeOurs(marine), fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk, 13)
        ), () -> {
            // A Hydralisk shoots 5 tiles and this one is 3 away, so it is well
            // inside even the base margin. The manager has to see it.
            assertTrue(new ProtossShuttleEmptyAvoidEnemies(marine).applies(),
                "a Hydralisk 3 tiles away can reach a Marine, and the manager has to see it");
        });
    }

    @Test
    public void anEnemyOutOfReachIsNotAnEnemyToAvoid() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);

        world(1, fakeOurs(marine), fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk, 18)
        ), () -> {
            // 8 tiles away, 5 of range plus the 1.5 margin: out of reach, so
            // there is nothing to avoid and the manager must not fire.
            assertFalse(new ProtossShuttleEmptyAvoidEnemies(marine).applies());
        });
    }

    /**
     * The margin exists so that a unit whose shields are nearly gone leaves more
     * room: a Marine has none at all, and "no shields" must not be read as "no
     * margin" either - the base 1.5 tiles still apply.
     */
    @Test
    public void aUnitWithoutShieldsKeepsTheBaseMargin() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);

        world(1, fakeOurs(marine), fakeEnemies(
            fake(AUnitType.Zerg_Zergling, 11.2)
        ), () -> {
            // Zergling claws reach 1 tile. 11.2 is 1.2 tiles away: out of reach
            // without the margin, inside it with the base 1.5.
            assertTrue(new ProtossShuttleEmptyAvoidEnemies(marine).applies());
        });
    }
}