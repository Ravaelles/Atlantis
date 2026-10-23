package tests.unit;

import atlantis.production.SupplyBlockedTwin;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.engine.GameStateSnapshot;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * SUPPLY_BLOCKED on the legacy path (NEXT #150 follow-up): the twin
 * lived in the V2 snapshot only, so OFF games blocked at 90/90 with
 * zero rows. Same rows, same throttle, whoever played.
 */
@Tag("production")
public class SupplyBlockedTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void legacySampleNamesTheInflightProvider() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(300, fakeOurs(probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) {
                initSupply(36, 40);
                ConstructionRequests.constructions.clear();
                EventLog.clearForTests();
                ConstructionRequests.constructions.add(new Construction(AUnitType.Protoss_Pylon));

                return;
            }
            if (atlantis.game.A.now() != 300) return;

            AUnitType provider = SupplyBlockedTwin.legacyProvider();
            SupplyBlockedTwin.sample(
                provider, atlantis.game.A.supplyFree(),
                GameStateSnapshot.supplyProvidersComing(provider));

            String log = EventLog.toJsonLines();
            assertTrue(log.contains("\"key\":\"SUPPLY_BLOCKED\",\"lvl\":\"MED\",\"type\":\"Pylon\",\"result\":\"sample\""),
                "sample names the provider: " + log);
            assertTrue(log.contains("\"free\":4") && log.contains("\"coming\":1"),
                "sample carries the block state: " + log);
            assertTrue(log.contains("\"result\":\"blocker-construction\"")
                && log.contains("\"status\":\"NOT_STARTED\""),
                "unstarted provider named as the blocker: " + log);
        });
    }

    @Test
    public void quietGameSamplesWithoutBlockers() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(300, fakeOurs(probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 1) {
                initSupply(30, 40);
                ConstructionRequests.constructions.clear();
                EventLog.clearForTests();

                return;
            }
            if (atlantis.game.A.now() != 300) return;

            AUnitType provider = SupplyBlockedTwin.legacyProvider();
            SupplyBlockedTwin.sample(
                provider, atlantis.game.A.supplyFree(),
                GameStateSnapshot.supplyProvidersComing(provider));

            String log = EventLog.toJsonLines();
            assertTrue(log.contains("\"result\":\"sample\""), "sample always rows: " + log);
            assertTrue(!log.contains("\"result\":\"blocker"),
                "nothing coming, nothing named: " + log);
        });
    }
}
