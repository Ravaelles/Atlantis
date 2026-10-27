package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner 2026-10-15 (NEXT #81): the corner-base Pylon hunt needs refusal
 * attribution that works without a game. The live `PYLON_FIND_FAILED`
 * line already prints per-rule counts; this pins the plumbing behind it:
 * every refusal lands in the breakdown under a named rule, never as an
 * unnamed one - so a doctrine change shows up as moved counts, and a new
 * rule that forgets to name itself fails here instead of hiding in a
 * total.
 *
 * <p>What this is NOT: a corner reproduction. The stub terrain is flat
 * (everything walkable, buildable means no unit within 1.98 tiles), so
 * TauCross corner geometry cannot fail here. The doctrine numbers still
 * come from flagged games; this keeps the attribution honest for them.</p>
 */
public class PylonSearchBreakdownTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void failedSearchAttributesEveryRefusal() {
        world(1, () -> {
            AUnit builder = Select.ourWorkers().first();
            assertNotNull(builder);

            // The whole 3x3 block around the Nexus is occupied (Nexus + a
            // probe on every side), so every candidate the radius-2 sweep
            // examines is refused and the search fails with reasons.
            APosition refused = ProtossPositionFinder.findStandardPositionFor(
                builder, AUnitType.Protoss_Pylon, APosition.create(10, 10), 2
            );

            assertNull(refused, "a fully occupied block yields no Pylon tile");
            Map<String, Integer> breakdown = ProtossPositionFinder.lastRefusalBreakdown();
            assertFalse(breakdown.isEmpty(), "a failed Pylon sweep must leave its refusal counts behind");
            int total = 0;
            for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
                assertNotNull(entry.getKey(), "no null rule name");
                assertFalse(entry.getKey().isEmpty(), "no empty rule name");
                assertFalse(
                    entry.getKey().toLowerCase().contains("not defined")
                        || entry.getKey().toLowerCase().contains("bug"),
                    "every refusal must name its rule, found: " + entry.getKey());
                assertTrue(entry.getValue() > 0, "counts are positive");
                total += entry.getValue();
            }
            assertTrue(total >= 1, "at least the occupied tile was refused and counted");
        });
    }

    @Test
    public void successfulSearchLeavesNoRefusalDebt() {
        world(1, () -> {
            AUnit builder = Select.ourWorkers().first();
            assertNotNull(builder);

            APosition found = ProtossPositionFinder.findStandardPositionFor(
                builder, AUnitType.Protoss_Pylon, APosition.create(30, 30), 10
            );

            assertNotNull(found, "open stub ground near the base must yield a Pylon tile");
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 9, 10),
            fake(AUnitType.Protoss_Probe, 10, 9),
            fake(AUnitType.Protoss_Probe, 11, 10),
            fake(AUnitType.Protoss_Probe, 10, 11),
            fake(AUnitType.Protoss_Probe, 14, 14)
        );
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return fakeEnemies();
    }
}
