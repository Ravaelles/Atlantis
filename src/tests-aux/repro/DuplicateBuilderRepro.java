package tests.aux.repro;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.constructions.position.DefineExactPositionForNewConstruction;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;
import tests.unit.sim.SimArena;
import tests.unit.sim.SimWorld;

/**
 * TEMPORARY repro harness: NOT part of any test suite - plain main()
 * under src/tests-aux, never selected by run-tests.sh (it only selects
 * tests.unit.*, tests.architecture, tests.acceptance and tests.e2e).
 * Delete once the give-up path is pinned by a real game + BuilderLog.
 *
 * Shape under test (owner live report: second Gateway never warped,
 * two workers walked to one site, both gave up to minerals): a
 * request that dies as a duplicate must not steal the live row's
 * builder on its way out. Exclusive ownership unlinks the old row,
 * which re-arms with a second worker - two probes, one site.
 *
 * Setup: a live Pylon row at a fixed tile with its mason, then a
 * second Pylon request at the same tile with siting pinned to that
 * tile (both placement seams mocked - this harness owns the builder
 * handover, not the finder). Oracle: the duplicate is refused AND
 * the live row still names its mason.
 *
 * Exit codes: 0 = HOLDS (refused, no steal), 1 = STEAL (bug
 * reproduced: refused but the live row lost its builder), 2 =
 * INCONCLUSIVE (no duplicate produced - siting moved, not a pass).
 */
public final class DuplicateBuilderRepro {

    private static final int TILE_X = 40;
    private static final int TILE_Y = 33;

    public static void main(String[] args) throws Exception {
        org.mockito.MockedStatic<atlantis.game.AGame> aGame =
            org.mockito.Mockito.mockStatic(atlantis.game.AGame.class);
        aGame.when(atlantis.game.AGame::now).thenReturn(100);
        aGame.when(atlantis.game.AGame::supplyUsed).thenReturn(20);
        aGame.when(atlantis.game.AGame::supplyTotal).thenReturn(33);
        aGame.when(atlantis.game.AGame::supplyFree).thenReturn(13);
        aGame.when(atlantis.game.AGame::minerals).thenReturn(400);
        aGame.when(atlantis.game.AGame::gas).thenReturn(100);

        org.mockito.MockedStatic<main.Main> ourRace =
            org.mockito.Mockito.mockStatic(main.Main.class);
        ourRace.when(main.Main::ourRace).thenReturn("Protoss");

        APosition fixed = APosition.create(TILE_X, TILE_Y);
        org.mockito.MockedStatic<DefineExactPositionForNewConstruction> siting =
            org.mockito.Mockito.mockStatic(DefineExactPositionForNewConstruction.class);
        siting.when(() -> DefineExactPositionForNewConstruction.exactPositionForNewConstruction(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> {
                // The real finder sets the tile on the construction as a
                // side effect; the mock pins the tile, so it must do the
                // same or the row below reads null.
                Construction c = inv.getArgument(2);
                c.setPositionToBuild(fixed);
                return fixed;
            });

        org.mockito.MockedStatic<CatalogueSiteAdapter> resite =
            org.mockito.Mockito.mockStatic(CatalogueSiteAdapter.class);
        resite.when(() -> CatalogueSiteAdapter.siteFor(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any())).thenReturn(fixed);

        int verdict = 2;
        SimWorld world = null;
        try {
            if (Queue.get() != null) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
            }
            ConstructionRequests.constructions.clear();

            FakeUnit mason = new FakeUnit(AUnitType.Protoss_Probe, 38, 31);
            FakeUnit spare = new FakeUnit(AUnitType.Protoss_Probe, 39, 32);
            FakeUnit nexus = new FakeUnit(AUnitType.Protoss_Nexus, 20, 20);
            world = new SimWorld(
                new FakeUnit[]{ nexus, mason, spare }, new FakeUnit[]{},
                new SimArena(100 * 32, 60 * 32));

            Construction live = new Construction(AUnitType.Protoss_Pylon);
            live.setPositionToBuild(APosition.create(TILE_X, TILE_Y));
            live.setBuilder(mason);
            ConstructionRequests.addConstruction(live);

            ProductionOrder dupOrder = new ProductionOrder(AUnitType.Protoss_Pylon, 27);
            Construction refused = NewConstructionRequest.requestConstructionOf(
                AUnitType.Protoss_Pylon, APosition.create(TILE_X, TILE_Y), dupOrder);

            System.out.println("=== DuplicateBuilderRepro ===");
            System.out.println("  second request: "
                + (refused == null ? "refused (duplicate)" : "survived at " + refused.buildPosition()));
            System.out.println("  live builder after: "
                + (live.builder() == null ? "NONE" : live.builder().typeWithUnitId()));

            if (refused != null) {
                System.out.println("RESULT: INCONCLUSIVE - no duplicate produced, siting moved");
                verdict = 2;
            }
            else if (mason.equals(live.builder())) {
                System.out.println("RESULT: HOLDS - refused duplicate stole nothing");
                verdict = 0;
            }
            else {
                System.out.println("RESULT: STEAL - refused duplicate took the live row's builder");
                verdict = 1;
            }
        }
        finally {
            if (world != null) world.close();
            if (Queue.get() != null) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
            }
            ConstructionRequests.constructions.clear();
            resite.close();
            siting.close();
            ourRace.close();
            aGame.close();
        }
        System.exit(verdict);
    }
}
