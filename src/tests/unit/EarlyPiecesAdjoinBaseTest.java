package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.conditions.TooCloseToBaseLocation;
import atlantis.production.constructions.position.protoss.PylonPosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner: the first Pylon, Gateway and Cybernetics Core adjoin the Nexus -
 * the builder walks as little as possible, so early mining is not donated
 * to a layout. At or under 20 supply the too-close-to-base ring steps
 * aside (footprint overlap still refuses - adjoining is legal); past it
 * the spacing doctrine owns the layout again. The first-Pylon search
 * origin moves with it: two tiles out while young, seven once placed.
 */
@Tag("map")
public class EarlyPiecesAdjoinBaseTest extends AbstractTestWithWorld {

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
    public void earlyGatewayMayAdjoinBaseLocation() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            // FakeBaseLocations: base location at (7, 44), Protoss-shifted
            // to (9, 45) - tile (13, 45) sits 4 inside the Gateway ring.
            APosition near = APosition.create(13, 45);

            currentSupplyUsed = 20;
            assertFalse(
                TooCloseToBaseLocation.TooCloseAndOverlapsBaseLocation(
                    AUnitType.Protoss_Gateway, near),
                "supply 20: the first Gateway may adjoin");

            currentSupplyUsed = 21;
            assertTrue(
                TooCloseToBaseLocation.TooCloseAndOverlapsBaseLocation(
                    AUnitType.Protoss_Gateway, near),
                "past 20 the spacing ring owns the layout again");
        });
    }

    @Test
    public void earlyCoreMayAdjoinBaseLocation() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            APosition near = APosition.create(13, 45);

            currentSupplyUsed = 16;
            assertFalse(
                TooCloseToBaseLocation.TooCloseAndOverlapsBaseLocation(
                    AUnitType.Protoss_Cybernetics_Core, near),
                "supply 16: the first Core may adjoin");
        });
    }

    @Test
    public void firstPylonOriginStaysNextToBase() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            currentSupplyUsed = 12;
            APosition origin = PylonPosition.nearToPositionForFirstPylon().position();
            assertTrue(
                atlantis.units.select.Select.main().distTo(origin) <= 3,
                "young origin hugs the base, shortest walk: " + origin);
        });
    }
}
