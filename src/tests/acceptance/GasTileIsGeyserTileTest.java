package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.builders.GasBuildingFix;
import atlantis.production.constructions.position.FindPosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A refinery is built on its geyser's OWN tile - the engine accepts nothing
 * else ({@code getTilePosition() == position}). Both the initial finder and
 * the issue-time fix must therefore answer the geyser tile, never a
 * center-derived neighbor (NEXT #62: a 2-off tile reads fine everywhere except
 * at the engine, then dies as "took too long / buildable:false" and - without
 * a requeue - never comes back).
 *
 * <p>FakeUnit positions are tile-exact, so the stub pins the contract: for a
 * 4x2 geyser centered at (86,114) the answer is its top-left (84,113).</p>
 */
public class GasTileIsGeyserTileTest extends WorldStubForTests {

    private static final int GX = 86;
    private static final int GY = 114;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void finderReturnsGeyserTile() {
        FakeUnit base = fake(AUnitType.Protoss_Nexus, 80, 110);
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 79, 109);
        neutralInWorld = new FakeUnit[]{ fake(AUnitType.Resource_Vespene_Geyser, GX, GY) };

        world(1, units(base, worker), units(), () -> {
            APosition tile = FindPosition.findForBuilding(
                worker, AUnitType.Protoss_Assimilator, null, base.position(), 30
            );

            assertEquals(GX - 2, tile.tx(), "refinery tile x");
            assertEquals(GY - 1, tile.ty(), "refinery tile y");
        });
    }

    @Test
    public void fixSnapsFarPositionToGeyserTile() {
        FakeUnit base = fake(AUnitType.Protoss_Nexus, 80, 110);
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 79, 109);
        FakeUnit geyser = fake(AUnitType.Resource_Vespene_Geyser, GX, GY);
        neutralInWorld = new FakeUnit[]{ geyser };

        world(1, units(base, worker), units(), () -> {
            // Request tile near the geyser but not on it: the fix must snap to
            // the geyser's own tile (the engine accepts nothing else).
            APosition fixed = new GasBuildingFix(worker).applyGasBuildingFixIfNeeded(
                APosition.create(80, 110), AUnitType.Protoss_Assimilator
            );

            assertEquals(GX - 2, fixed.tx(), "snapped tile x");
            assertEquals(GY - 1, fixed.ty(), "snapped tile y");
        });
    }
}
