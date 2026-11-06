package tests.aux.repro;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.config.env.ProductionV2Mode;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.PsiField;
import atlantis.placement.core.ReservedSlots;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.fakes.FakeUnit;
import tests.unit.sim.SimConfig;
import tests.unit.sim.SimDriver;
import tests.unit.sim.SimMap;

/**
 * One-sentence README: replays the live second-Gateway starvation (Wine
 * 20261106_175548, TauCross base 1) in StarEngine and checks the powered
 * layout slot is served to its committed builder.
 *
 * Shape under test: the layout's Gateway slot is powered (completed Pylon)
 * and free, but a saturated base puts 4+ workers within 12 tiles of every
 * base tile, so the worker-cover decline used to fire forever (1511x
 * {@code declined worker-covered slot} in the game) while the assigned
 * builder spun mine/manager - buildings.json shows one Gateway all game.
 * Exit codes: 0 = SERVED (slot answered, powered - the fix holds),
 * 1 = STARVED (bug reproduced: no site despite builder, bank and power),
 * 2 = INCONCLUSIVE (setup failed).
 */
public final class GatewayCoveredSiteRepro {

    private static final class Bank {
        int frame = 8000;
        int minerals = 800;
        int gas = 100;
        int supplyUsed = 20;
        int supplyTotal = 33;
    }

    public static void main(String[] args) throws Exception {
        Bank bank = new Bank();
        MockedStatic<AGame> aGame = Mockito.mockStatic(AGame.class);
        MockedStatic<main.Main> ourRace = Mockito.mockStatic(main.Main.class);
        int verdict = 2;
        SimDriver driver = null;
        try {
            aGame.when(AGame::now).thenAnswer(inv -> bank.frame);
            aGame.when(() -> AGame.everyNthGameFrame(Mockito.anyInt()))
                .thenAnswer(inv -> bank.frame % (Integer) inv.getArgument(0) == 0);
            aGame.when(() -> AGame.notNthGameFrame(Mockito.anyInt()))
                .thenAnswer(inv -> bank.frame % (Integer) inv.getArgument(0) != 0);
            aGame.when(AGame::minerals).thenAnswer(inv -> bank.minerals);
            aGame.when(AGame::gas).thenAnswer(inv -> bank.gas);
            aGame.when(AGame::supplyUsed).thenAnswer(inv -> bank.supplyUsed);
            aGame.when(AGame::supplyTotal).thenAnswer(inv -> bank.supplyTotal);
            aGame.when(AGame::supplyFree)
                .thenAnswer(inv -> Math.max(0, bank.supplyTotal - bank.supplyUsed));
            ourRace.when(main.Main::ourRace).thenReturn("Protoss");

            if (Atlantis.game() == null) {
                bwapi.Game game = tests.unit.UnitTest.newGameMock(bank.frame);
                Mockito.when(game.mapWidth()).thenReturn(202);
                Mockito.when(game.mapHeight()).thenReturn(203);
                Mockito.when(game.isWalkable(Mockito.any(bwapi.WalkPosition.class)))
                    .thenReturn(true);
                Atlantis.getInstance().setGame(game);
            }
            A.setNow(bank.frame, bank.frame / 30);

            Env.setProductionV2ForTest(ProductionV2Mode.LIVE);
            Env.setPlacementCatalogueForTest(true);

            SimMap map = SimMap.load("out/map-snapshots/sscai_(3)TauCross.scx.json");
            map.install();
            driver = new SimDriver(map, SimConfig.fast().withStride(60),
                new SimDriver.SimFields() {
                    @Override
                    public void setMinerals(int minerals) {
                        bank.minerals = minerals;
                    }

                    @Override
                    public void setGas(int gas) {
                        bank.gas = gas;
                    }

                    @Override
                    public void setSupply(int used, int total) {
                        bank.supplyUsed = used;
                        bank.supplyTotal = total;
                    }
                }, 50);
            driver.spawnResources();
            driver.spawnBase(1, 24);
            for (int i = 0; i < 8; i++) {
                driver.units().spawn(
                    AUnitType.Protoss_Probe, 100 + (i % 4), 33 + (i / 4));
            }
            FakeUnit pylon = driver.units().spawn(AUnitType.Protoss_Pylon, 107, 16);
            FakeUnit builder = driver.units().spawn(AUnitType.Protoss_Probe, 112, 14);

            ReservedSlots.setSlotsForTests(
                java.util.Collections.singletonList(
                    new ReservedSlots.Slot("G", AUnitType.Protoss_Gateway,
                        APosition.create(113, 17))));

            Construction construction = new Construction(AUnitType.Protoss_Gateway);
            construction.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Gateway, 12));
            construction.setBuilder(builder);

            System.out.println("=== GatewayCoveredSiteRepro ===");
            System.out.println("  workers=" + atlantis.units.select.Select.ourWorkers().count()
                + " pylons=" + atlantis.units.select.Select
                    .ourOfType(AUnitType.Protoss_Pylon).count()
                + " pylonCompleted=" + pylon.isCompleted());
            try {
                atlantis.units.AUnit mainBase = atlantis.units.select.Select.mainOrAnyBuilding();
                bwapi.Game g = atlantis.Atlantis.game();
                System.out.println("  main=" + (mainBase == null ? "-" : mainBase.position())
                    + " reachable=" + (mainBase == null ? "-" : atlantis.map.ReachableTiles.between(
                        mainBase.position(), APosition.create(113, 17)))
                    + " gameNull=" + (g == null)
                    + " mapWH=" + atlantis.map.AMap.getMapWidthInTiles()
                    + "x" + atlantis.map.AMap.getMapHeightInTiles());
            } catch (Exception | Error e) {
                System.out.println("  reachability check failed: " + e);
            }

            APosition site = null;
            boolean crashed = false;
            try {
                site = CatalogueSiteAdapter.siteFor(
                    new CataloguePlacementPlanner(), builder,
                    AUnitType.Protoss_Gateway, construction);
            } catch (Exception | Error e) {
                // Harness gap, not the bug: without BWEM the hunt fallback
                // cannot resolve chokes (the JUnit harness carries region
                // data, a bare main does not). The slot decline twin below
                // is still the evidence the bug fires - the demand never
                // gets past the slot on cover.
                crashed = true;
                System.out.println("  hunt unavailable in harness: " + e);
            }

            String twins = atlantis.util.log.EventLog.toJsonLines();
            boolean slotDeclinedOnCover = twins.contains(
                "\"result\":\"declined\",\"reason\":\"worker-covered\"");

            boolean powered = site != null && PsiField.covers(
                pylon.tx(), pylon.ty(), site.tx(), site.ty(),
                AUnitType.Protoss_Gateway.getTilesWidth(),
                AUnitType.Protoss_Gateway.getTilesHeights());
            System.out.println("  site=" + site + " powered=" + powered);

            if (site != null && site.tx() == 113 && site.ty() == 17 && powered) {
                System.out.println("RESULT: SERVED - the committed builder gets its powered slot");
                verdict = 0;
            } else if (site == null && (slotDeclinedOnCover || crashed)) {
                System.out.println("RESULT: STARVED - no site for a powered slot"
                    + " (slotDeclinedOnCover=" + slotDeclinedOnCover
                    + " huntCrashed=" + crashed
                    + " - live: 1511 slot declines, one Gateway all game)");
                verdict = 1;
            } else {
                System.out.println("RESULT: INCONCLUSIVE - site=" + site
                    + " powered=" + powered + " crashed=" + crashed);
                verdict = 2;
            }
        } catch (Exception | Error e) {
            System.out.println("RESULT: INCONCLUSIVE - setup failed: " + e);
            e.printStackTrace(System.out);
            verdict = 2;
        } finally {
            ReservedSlots.clearForTests();
            ConstructionRequests.constructions.clear();
            if (driver != null) driver.close();
            try {
                ourRace.close();
            } catch (Exception | Error ignored) {
            }
            try {
                aGame.close();
            } catch (Exception | Error ignored) {
            }
        }
        System.exit(verdict);
    }
}
