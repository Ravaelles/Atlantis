package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Early pylons stay home (owner report 2026-10-26: a Pylon landed in a
 * third region, neither main nor natural - the early spacing rule pushed
 * every sibling 4-6 tiles out, and the spiral walked the exclusion into
 * another region). Below the owner's line the search takes the nearest
 * valid tile instead of enforcing sibling distance.
 */
@Tag("production")
public class PylonStaysHomeEarlyTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void secondPylonMaySitNearItsSibling() {
        currentSupplyUsed = 30;
        currentSupplyTotal = 40;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 26, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 22, 20);

        world(5, fakeOurs(nexus, pylon, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            AUnit builder = Select.ourWorkers().first();
            assertNotNull(builder);

            APosition found = ProtossPositionFinder.findStandardPositionFor(
                builder, AUnitType.Protoss_Pylon, APosition.create(26, 20), 12);

            assertNotNull(found, "open stub ground must yield a Pylon tile");
            assertTrue(found.distTo(pylon.position()) < 4,
                "early pylon clusters with its sibling instead of pushed regions away: " + found);
        });
    }

    @Test
    public void lateGameSearchStillYields() {
        currentSupplyUsed = 100;
        currentSupplyTotal = 140;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 26, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 22, 20);

        world(5, fakeOurs(nexus, pylon, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            AUnit builder = Select.ourWorkers().first();
            assertNotNull(builder);

            APosition found = ProtossPositionFinder.findStandardPositionFor(
                builder, AUnitType.Protoss_Pylon, APosition.create(26, 20), 12);

            assertNotNull(found, "open stub ground must yield a Pylon tile late too");
        });
    }
}
