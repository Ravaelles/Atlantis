package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.constructions.position.DefineExactPositionForNewConstruction;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A refused duplicate must not steal: the request path assigned the
 * optimal builder BEFORE asking whether the row already exists, so a
 * duplicate stole the live row's builder on its way to being
 * cancelled - the old row re-armed with a second worker and two
 * probes walked to one site (owner live game: second Gateway never
 * warped, both workers gave up to minerals). Only a surviving row
 * may take a builder.
 *
 * <p>Pinned behaviorally, not structurally: a live Pylon row holds
 * its mason, siting is pinned to the same tile (both placement seams
 * mocked - this test owns the builder handover, not the finder), and
 * the duplicate request must come back refused with the live row
 * untouched. Red on the old order (the live row ends builderless),
 * green since the assignment moved behind both duplicate checks.
 * Wiring pin: no decision predicate covers who-takes-whom on a
 * refused row, so the sim-harness shape is the pin.</p>
 */
@Tag("production")
public class DuplicateRequestKeepsOldBuilderTest extends WorldStubForTests {

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

    @BeforeEach
    public void hermetic() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void refusedDuplicateDoesNotStealTheLiveBuilders() {
        FakeUnit mason = fake(AUnitType.Protoss_Probe, 38, 31);
        FakeUnit spare = fake(AUnitType.Protoss_Probe, 39, 32);

        options.set("supplyUsed", 20);
        options.set("supplyTotal", 33);
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            mason, spare
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            currentMinerals = 400;
            initSupply(20, 33);

            Construction live = new Construction(AUnitType.Protoss_Pylon);
            APosition tile = APosition.create(40, 33);
            live.setPositionToBuild(tile);
            live.setBuilder(mason);
            ConstructionRequests.addConstruction(live);
            int before = ConstructionRequests.constructions.size();

            org.mockito.MockedStatic<DefineExactPositionForNewConstruction> siting =
                Mockito.mockStatic(DefineExactPositionForNewConstruction.class);
            org.mockito.MockedStatic<CatalogueSiteAdapter> resite =
                Mockito.mockStatic(CatalogueSiteAdapter.class);
            try {
                siting.when(() -> DefineExactPositionForNewConstruction.exactPositionForNewConstruction(
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenAnswer(inv -> {
                        Construction c = inv.getArgument(2);
                        c.setPositionToBuild(tile);
                        return tile;
                    });
                resite.when(() -> CatalogueSiteAdapter.siteFor(
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                    .thenReturn(tile);

                // minSupply above the banked supply: the old order reaches
                // the optimal-builder seam (the keep-random-builder
                // early-out does not fire), which is exactly where the
                // steal used to happen.
                ProductionOrder dupOrder = new ProductionOrder(AUnitType.Protoss_Pylon, 27);
                Construction refused = NewConstructionRequest.requestConstructionOf(
                    AUnitType.Protoss_Pylon, APosition.create(40, 33), dupOrder);

                assertNull(refused, "the same Pylon at the same tile is a duplicate, not a second site");
                assertEquals(before, ConstructionRequests.constructions.size(),
                    "no duplicate construction for one need");
                assertEquals(mason, live.builder(),
                    "the refused duplicate must not steal the live row's builder "
                        + "(it did on the way to its own cancel, and the old row "
                        + "re-armed with a second worker)");
            }
            finally {
                resite.close();
                siting.close();
            }
        });
    }
}
