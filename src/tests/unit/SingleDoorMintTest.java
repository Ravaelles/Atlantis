package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The single minting door (NEXT #57, V3): one tile holds one building,
 * so a second live row on the identical tile is always a duplicate,
 * never legit concurrency (which mints on its own tiles - Pylon pairs,
 * double Gateways). Every creation path funnels through
 * {@code ConstructionRequests.addConstruction}, so the refusal lives
 * there once instead of in every path's own dialect.
 */
@Tag("production")
public class SingleDoorMintTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    private static Construction rowAt(AUnitType type, int tx, int ty) {
        Construction row = new Construction(type);
        row.setPositionToBuild(APosition.create(tx, ty));
        return row;
    }

    @Test
    public void sameTileSecondMintIsRefused() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            ConstructionRequests.constructions.clear();

            assertTrue(ConstructionRequests.addConstruction(
                rowAt(AUnitType.Protoss_Cybernetics_Core, 40, 40)));
            assertFalse(ConstructionRequests.addConstruction(
                rowAt(AUnitType.Protoss_Cybernetics_Core, 40, 40)),
                "same type, same tile, live row: a duplicate, never a second demand");
            assertEquals(1, ConstructionRequests.countAllOfType(AUnitType.Protoss_Cybernetics_Core));
        });
    }

    @Test
    public void neighbourTileStillMints() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            ConstructionRequests.constructions.clear();

            assertTrue(ConstructionRequests.addConstruction(
                rowAt(AUnitType.Protoss_Pylon, 40, 40)));
            assertTrue(ConstructionRequests.addConstruction(
                rowAt(AUnitType.Protoss_Pylon, 44, 44)),
                "a different tile is a different demand - Pylon pairs keep minting");
            assertEquals(2, ConstructionRequests.countAllOfType(AUnitType.Protoss_Pylon));
        });
    }

    @Test
    public void nullSiteRowsAreUntouched() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            ConstructionRequests.constructions.clear();

            assertTrue(ConstructionRequests.addConstruction(
                rowAt(AUnitType.Protoss_Gateway, 40, 40)));
            Construction siteless = new Construction(AUnitType.Protoss_Gateway);
            assertTrue(ConstructionRequests.addConstruction(siteless),
                "no tile, no claim: the gate only guards placed rows");
        });
    }
}
