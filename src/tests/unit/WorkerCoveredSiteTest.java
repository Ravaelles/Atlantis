package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
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

/**
 * Standing-cover siting (NEXT #145): a catalogue tile 4+ of our workers
 * stand on dies builderless (the builder assignment refuses it, the
 * "looks bugged" cancel kills it, the goal re-fires on the same tile -
 * measured 3988 CONSTRUCTION_CANCELs on the catalogue A/B arm). The
 * adapter declines such tiles up front with a "worker-covered" twin row
 * and the legacy search answers instead. Three workers is not cover.
 */
@Tag("production")
public class WorkerCoveredSiteTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private static Construction gatewayOrderAt(APosition tile) {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, tile, 10);
        order.markAsUsingExactPosition();
        Construction construction = new Construction(AUnitType.Protoss_Gateway);
        construction.setProductionOrder(order);

        return construction;
    }

    @Test
    public void coveredTileIsDeclined() {
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
            APosition tile = APosition.create(25, 25);
            ScriptedPlanner yes = new ScriptedPlanner(
                atlantis.production.v2.PlacementReservation.success(25, 25, 0));

            assertNull(CatalogueSiteAdapter.siteFor(
                    yes, probe, AUnitType.Protoss_Gateway, gatewayOrderAt(tile)),
                "4 workers within 12 stand on the tile - the builder would"
                    + " be refused, so the catalogue must decline, never serve it");
            assertTrue(EventLog.toJsonLines().contains(
                    "\"type\":\"Gateway\",\"result\":\"declined\",\"reason\":\"worker-covered\""),
                "the refusal names itself in the twin: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void threeWorkersAreNotCover() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 21, 24),
            probe,
            fake(AUnitType.Protoss_Probe, 28, 25),
            fake(AUnitType.Protoss_Probe, 25, 28)
        ), fakeEnemies(), () -> {
            APosition tile = APosition.create(25, 25);
            ScriptedPlanner yes = new ScriptedPlanner(
                atlantis.production.v2.PlacementReservation.success(25, 25, 0));

            APosition site = CatalogueSiteAdapter.siteFor(
                yes, probe, AUnitType.Protoss_Gateway, gatewayOrderAt(tile));

            assertNotNull(site, "3 workers do not cover the tile - the catalogue answers: "
                + EventLog.toJsonLines());
            assertEquals(tile.tx(), site.tx());
            assertEquals(tile.ty(), site.ty());
        });
    }

    private static final class ScriptedPlanner implements atlantis.production.v2.PlacementPlanner {
        private final atlantis.production.v2.PlacementReservation answer;

        ScriptedPlanner(atlantis.production.v2.PlacementReservation answer) {
            this.answer = answer;
        }

        @Override
        public void startPass() {
        }

        @Override
        public atlantis.production.v2.PlacementReservation reservePlacement(
            atlantis.production.v2.Producible building,
            atlantis.production.v2.TargetPlacement constraint, int targetFrame
        ) {
            return answer;
        }
    }
}
