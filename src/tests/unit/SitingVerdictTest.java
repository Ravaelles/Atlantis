package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.constructions.position.RefusedTiles;
import atlantis.production.constructions.position.SitingVerdict;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * V3 S1-S2: the single siting verdict, pinned per gate x building type.
 * One function owns refused / power / geyser-minerals / worker-cover
 * (S1) and the map edge (S2: 4 tiles everywhere), so no path can serve
 * a tile another would decline. Includes the live Steamhammer shape
 * (Gateway [123,26] vs warping Pylon [121,22]) and the funnel geyser
 * twin (a new decline the funnel never named before).
 */
@Tag("placement")
public class SitingVerdictTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void refusedTileWinsOverEverything() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            RefusedTiles.clearForTests();
            try {
                APosition tile = APosition.create(30, 30);
                RefusedTiles.note(tile, AUnitType.Protoss_Pylon);

                SitingVerdict verdict = SitingVerdict.check(AUnitType.Protoss_Pylon, tile);

                assertFalse(verdict.accepted(), "a refused tile is never servable");
                assertEquals("refused-tile", verdict.twin);
                assertEquals("Tile refused by engine", verdict.reason);
            } finally {
                RefusedTiles.clearForTests();
            }
        });
    }

    @Test
    public void unpoweredGatewayRefusedPylonExempt() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            APosition tile = APosition.create(30, 30);

            SitingVerdict gateway = SitingVerdict.check(AUnitType.Protoss_Gateway, tile);
            assertFalse(gateway.accepted(), "no Pylon anywhere: a Gateway has no power");
            assertEquals("unpowered", gateway.twin);
            assertEquals("Not powered by a Pylon", gateway.reason);

            SitingVerdict pylon = SitingVerdict.check(AUnitType.Protoss_Pylon, tile);
            assertTrue(pylon.accepted(), "Pylons need no power: the gate is skipped for them");
        });
    }

    @Test
    public void mineralCrowdingRefusedGasExempt() {
        neutralInWorld = new FakeUnit[]{fake(AUnitType.Resource_Mineral_Field, 60, 50)};

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            SitingVerdict crowded = SitingVerdict.check(
                AUnitType.Protoss_Pylon, APosition.create(59, 50));
            assertFalse(crowded.accepted(), "a Pylon one tile from minerals crowds the line");
            assertEquals("mineral-gas-too-close", crowded.twin);

            SitingVerdict far = SitingVerdict.check(
                AUnitType.Protoss_Pylon, APosition.create(80, 80));
            assertTrue(far.accepted(), "far from minerals: the rule must not fire everywhere");

            SitingVerdict gas = SitingVerdict.check(
                AUnitType.Protoss_Assimilator, APosition.create(59, 50));
            assertTrue(gas.accepted(), "a refinery stands on gas by design: exempt");
        });
    }

    @Test
    public void standingWorkersCoverTheTile() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 21, 24),
            probe,
            fake(AUnitType.Protoss_Probe, 28, 25),
            fake(AUnitType.Protoss_Probe, 25, 28),
            fake(AUnitType.Protoss_Probe, 31, 26),
            fake(AUnitType.Protoss_Probe, 26, 31)
        ), fakeEnemies(), () -> {
            SitingVerdict covered = SitingVerdict.check(
                AUnitType.Protoss_Gateway, APosition.create(25, 25));
            assertFalse(covered.accepted(), "4 workers within 12 cover the tile");
            assertEquals("worker-covered", covered.twin);
        });
    }

    @Test
    public void liveSteamhammerShapeStaysRefused() {
        FakeUnit warpingPylon = fake(AUnitType.Protoss_Pylon, 121, 22);
        warpingPylon.setCompleted(false);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 114, 11),
            fake(AUnitType.Protoss_Probe, 115, 12),
            warpingPylon
        ), fakeEnemies(), () -> {
            SitingVerdict verdict = SitingVerdict.check(
                AUnitType.Protoss_Gateway, APosition.create(123, 26));

            assertFalse(verdict.accepted(),
                "footprint center dy=176 is outside the psi field: "
                    + "serving it walks a builder into refused Builds");
            assertEquals("unpowered", verdict.twin);
        });
    }

    @Test
    public void funnelNamesMineralCrowdingWithItsOwnTwin() {
        neutralInWorld = new FakeUnit[]{fake(AUnitType.Resource_Mineral_Field, 60, 50)};

        FakeUnit probe = fake(AUnitType.Protoss_Probe, 54, 50);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);
            construction.setNearTo(APosition.create(57, 50));
            ScriptedPlanner planner = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.success(57, 50, 0));

            APosition site = CatalogueSiteAdapter.siteFor(
                planner, probe, AUnitType.Protoss_Pylon, construction);

            assertNull(site, "a mineral-crowding tile never leaves the funnel - "
                + "the legacy spiral answers instead");
            assertTrue(EventLog.toJsonLines().contains(
                    "\"type\":\"Pylon\",\"result\":\"declined\",\"reason\":\"mineral-gas-too-close\""),
                "the refusal names itself in the twin: " + EventLog.toJsonLines());
        });
    }

    private static void stubMap128() {
        bwapi.Game game = atlantis.Atlantis.game();
        org.mockito.Mockito.when(game.mapWidth()).thenReturn(128);
        org.mockito.Mockito.when(game.mapHeight()).thenReturn(128);
    }

    @Test
    public void edgeMarginIsFourTilesEverywhere() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 60, 60),
            fake(AUnitType.Protoss_Probe, 61, 61)
        ), fakeEnemies(), () -> {
            stubMap128();

            assertTrue(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Pylon, APosition.create(1, 60)),
                "x=1 is inside the 4-tile margin");
            assertTrue(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Pylon, APosition.create(3, 60)),
                "x=3 is still inside the 4-tile margin");
            assertFalse(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Pylon, APosition.create(4, 60)),
                "x=4 is the first legal column");
            assertFalse(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Pylon, APosition.create(122, 60)),
                "a 2x2 Pylon at x=122 ends at 124, the last legal edge");
            assertTrue(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Pylon, APosition.create(126, 60)),
                "a 2x2 Pylon at x=126 ends past the margin");
            assertTrue(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Gateway, APosition.create(60, 0)),
                "y=0 is inside the top margin, whatever the building");
            assertFalse(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Gateway, APosition.create(60, 60)),
                "the middle of the map is never near an edge");
        });
    }

    @Test
    public void edgeExemptsBasesGasAndOtherRaces() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 60, 60),
            fake(AUnitType.Protoss_Probe, 61, 61)
        ), fakeEnemies(), () -> {
            stubMap128();

            assertFalse(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Nexus, APosition.create(0, 0)),
                "bases stand where they stand - fixed positions, never judged");
            assertFalse(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Assimilator, APosition.create(0, 0)),
                "gas stands on its geyser by design - exempt like minerals");
            assertFalse(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Terran_Barracks, APosition.create(0, 0)),
                "a Protoss edge rule never judges a Terran building");
        });
    }

    @Test
    public void verdictNamesTheEdgeWithItsOwnTwin() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 60, 60),
            fake(AUnitType.Protoss_Probe, 61, 61)
        ), fakeEnemies(), () -> {
            stubMap128();

            SitingVerdict verdict = SitingVerdict.check(
                AUnitType.Protoss_Pylon, APosition.create(1, 60));

            assertFalse(verdict.accepted(), "an edge Pylon is unservable (S2)");
            assertEquals("too-close-to-edge", verdict.twin);
            assertEquals("Too close to map edge", verdict.reason);
        });
    }

    @Test
    public void snapshotEdgeRuleHoldsOnTauCrossGround() {
        // V3 S2, verdict-level (the V2-direct loop deliberately skips no
        // edge tile - the frozen-warp recovery needs one in sim, and sim
        // supply reads are stubbed so the loop cannot tell emergency from
        // doctrine). What the rule owns is pinned here instead, on real
        // TauCross ground: the three measured S3 block slots accept, an
        // edge tile by base 0 refuses.
        atlantis.map.snapshot.MapSnapshot snap = tests.fakes.SnapshotTerrain.tauCross();
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 60, 60),
            fake(AUnitType.Protoss_Probe, 61, 61)
        ), fakeEnemies(), () -> {
            stubMap128();

            int[][] servedSlots = {{13, 49}, {115, 14}, {95, 116}};
            for (int[] slot : servedSlots) {
                assertFalse(SitingVerdict.isTooCloseToEdge(
                        AUnitType.Protoss_Pylon, APosition.create(slot[0], slot[1])),
                    "the served block slot [" + slot[0] + "," + slot[1] + "] respects the margin");
            }
            assertTrue(SitingVerdict.isTooCloseToEdge(
                    AUnitType.Protoss_Pylon, APosition.create(1, 44)),
                "an edge tile by base 0 refuses - the Steamhammer shape stays unservable");
        });
    }

    private static final class ScriptedPlanner implements atlantis.production.placement.PlacementPlanner {
        private final atlantis.production.placement.PlacementReservation answer;

        ScriptedPlanner(atlantis.production.placement.PlacementReservation answer) {
            this.answer = answer;
        }

        @Override
        public void startPass() {
        }

        @Override
        public atlantis.production.placement.PlacementReservation reservePlacement(
            atlantis.production.producibles.Producible building,
            atlantis.production.placement.TargetPlacement constraint, int targetFrame
        ) {
            return answer;
        }
    }
}
