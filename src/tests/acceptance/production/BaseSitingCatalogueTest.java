package tests.acceptance.production;

import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.base.FindPositionForBase;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.Options;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Migration A4c (owner #146): base siting is pure catalogue. The legacy
 * spiral asserted exact tiles ([14,13]); the catalogue answers a
 * validated tile in the natural neighbourhood instead - the twin names
 * it, the game proves it. No fallback: a declined base retries through
 * the demand, never through the spiral.
 */
public class BaseSitingCatalogueTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private static Construction constructionForNexus(FakeUnit builder) {
        Construction construction = new Construction(AUnitType.Protoss_Nexus);
        construction.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Nexus, 30));
        construction.setBuilder(builder);
        return construction;
    }

    @Test
    public void naturalBaseIsAnsweredNearTheNatural() {
        options = Options.create().set("supplyUsed", 18);
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44)
        ), fakeEnemies(), () -> {
            FakeUnit builder = fake(AUnitType.Protoss_Probe, 9, 46);
            APosition natural = BaseLocations.natural();
            assertNotNull(natural, "the stub world has a natural - else this test proves nothing");

            APosition site = FindPositionForBase.forNewBase(
                builder, AUnitType.Protoss_Nexus, constructionForNexus(builder), null);

            assertNotNull(site, "migration A4c: the catalogue answers base siting - twin: "
                + EventLog.toJsonLines());
            assertTrue(site.distTo(natural) <= 20,
                "the answer sits in the natural neighbourhood (legacy spiral window was 25-50), "
                    + "not on the exact legacy tile and not anywhere: " + site);
            assertTrue(EventLog.toJsonLines().contains(
                "\"type\":\"Nexus\",\"result\":\"answered\""),
                "the answer lands in the twin: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void baseAnswerSurvivesTheDemandCarrier() {
        options = Options.create().set("supplyUsed", 18);
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 7, 44)
        ), fakeEnemies(), () -> {
            FakeUnit builder = fake(AUnitType.Protoss_Probe, 9, 46);
            Construction construction = constructionForNexus(builder);
            construction.setDemand(atlantis.production.demand.Demand.of(AUnitType.Protoss_Nexus));

            APosition site = FindPositionForBase.forNewBase(
                builder, AUnitType.Protoss_Nexus, construction, null);

            assertNotNull(site, "order or demand - the funnel answers both");
        });
    }
}
