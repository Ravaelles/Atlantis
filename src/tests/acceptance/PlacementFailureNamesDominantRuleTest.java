package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.constructions.position.PositionFulfillsAllConditions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A failed placement names the rule that killed the search, not the tile
 * the rings ended on: the sweep ends wherever it ends, so the final
 * `_STATUS` names an edge tile while the bulk died elsewhere (measured:
 * 8x Gateway "Not powered by a Pylon" with the real question unanswered).
 * The shared chain records per-rule counts for every building now; the
 * twin carries the dominant one. The request-level twin itself only fires
 * live (the stub harness answers every placement "this one will do").
 */
public class PlacementFailureNamesDominantRuleTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void occupiedTileRecordsPhysicalRefusal() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            AUnit builder = Select.ourWorkers().first();

            PositionFulfillsAllConditions.clearRefusalBreakdown();
            boolean ok = PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                builder, AUnitType.Protoss_Gateway, APosition.create(10, 10),
                APosition.create(10, 10));

            assertFalse(ok, "the Nexus tile refuses a Gateway");
            Map<String, Integer> breakdown =
                PositionFulfillsAllConditions.lastRefusalBreakdown();
            assertFalse(breakdown.isEmpty(), "the refusal lands in the breakdown");
            int total = 0;
            for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
                assertTrue(entry.getKey() != null && !entry.getKey().isEmpty(), "named rules only");
                total += entry.getValue();
            }
            assertTrue(total >= 1, "at least the occupied tile was refused and counted");
        });
    }

    @Test
    public void dominantWinsOverFinalStatus() {
        Map<String, Integer> breakdown = new HashMap<String, Integer>();
        breakdown.put("Not powered by a Pylon", 2);
        breakdown.put("Can't physically build here", 41);

        assertEquals(
            "Can't physically build here x41 (of 43)",
            NewConstructionRequest.dominantRefusal(breakdown, "edge tile"),
            "the bulk names the rule, not the ring's end");
        assertEquals(
            "fallback",
            NewConstructionRequest.dominantRefusal(new HashMap<String, Integer>(), "fallback"),
            "empty breakdown falls back to the status");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
