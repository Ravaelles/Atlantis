package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossForbiddenByStreetGrid;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A hard supply block yields the street grid for Pylons.
 *
 * <p>
 * Measured on Wine: ~1600 grid refusals on a crowded base while capped
 * 49/49, no Pylon ever again. The grid is a spacing preference, not physics -
 * with zero free supply the game needs a Pylon on any buildable tile. Other
 * conditions (minerals, occupancy, power) still apply; only the grid steps
 * aside, and only for Pylons at zero free supply.
 * </p>
 */
public class GridYieldsWhenSupplyBlockedTest extends WorldStubForTests {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void blockedSupplySkipsTheGridForPylons() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);

        initSupply(33, 33);

        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    probe, AUnitType.Protoss_Pylon, APosition.create(22, 30)),
                "tx%11==0 is normally forbidden, but a hard supply block needs a Pylon anywhere");
        });
    }

    @Test
    public void openSupplyKeepsTheGrid() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);

        initSupply(28, 33);

        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    probe, AUnitType.Protoss_Pylon, APosition.create(22, 30)),
                "with 5 free supply the layout preference stays in charge");
        });
    }
}
