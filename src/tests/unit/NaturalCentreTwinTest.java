package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.Producible;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The V2 natural anchor is adjudication-critical and otherwise
 * invisible: a far-away Nexus warp must name whether the anchor or
 * the serving was wrong (measured live GAME_68F87352: Nexus minted at
 * [116,9] with no funnel twin anywhere). One row per anchor, never
 * per frame.
 *
 * <p>Runs on the stub base list (the same FakeBaseLocations every
 * siting test reads), so the expected centre comes from the same
 * resolution the planner used - the pin is on the twin wiring, not
 * on map truth.</p>
 */
@Tag("placement")
public class NaturalCentreTwinTest extends WorldStubForTests {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @AfterEach
    public void cleanLog() {
        EventLog.clearForTests();
        atlantis.production.constructions.position.RefusedTiles.clearForTests();
    }

    @Test
    public void v2BaseRequestTwinsItsAnchor() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);

        world(3, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            EventLog.clearForTests();
            tests.fakes.SnapshotTerrain terrain = new tests.fakes.SnapshotTerrain(
                tests.fakes.SnapshotTerrain.tauCross());
            atlantis.placement.engine.CataloguePlacementPlanner planner =
                new atlantis.placement.engine.CataloguePlacementPlanner(
                    new atlantis.placement.core.TileAvailabilityGrid(terrain), null, null, true);
            planner.startPass();
            Producible base = UnitProducible.of(AUnitType.Protoss_Nexus);
            planner.reservePlacement(base, TargetPlacement.namedArea("NATURAL"), 100);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"result\":\"centre\""),
                "a named-area base request names its anchor: " + rows);

            APosition expected = null;
            try {
                expected = atlantis.map.base.BaseLocations.natural();
            } catch (Exception | Error ignored) {
            }
            assertNotNull(expected, "the stub world resolves a natural");
            assertTrue(
                rows.contains("\"tx\":" + expected.tx())
                    && rows.contains("\"ty\":" + expected.ty()),
                "the twin names the actually resolved anchor: " + rows
                    + " natural=" + expected);
        });
    }
}
