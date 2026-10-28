package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.ExistingItems;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.engine.LiveExistingItems;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The walk-window double-build (NEXT #219 remainder, live Cybernetics Core
 * warped twice from one At-22 row: dispatches f6139+f6281, warps
 * f6297+f6400 on different tiles). Between the builder's arrival and the
 * warp the construction is IN_PROGRESS with no unit yet - and the
 * scheduler's in-flight check only counted NOT_STARTED sites, so the
 * already-attempted build read as missing and the row re-fired
 * mid-walk. An attempted-but-unwarped building is in flight, never
 * missing.
 */
@Tag("production")
public class LiveExistingItemsTest extends WorldStubForTests {

    @AfterEach
    public void clearConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void inProgressWithoutUnitCountsAsInFlight() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 10)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            Construction construction = new Construction(AUnitType.Protoss_Cybernetics_Core);
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            ConstructionRequests.constructions.add(construction);
            try {
                int from = new LiveExistingItems(100).inFlightFrom(
                    UnitProducible.of(AUnitType.Protoss_Cybernetics_Core));

                assertTrue(from >= 0,
                    "an attempted-but-unwarped Core is in flight, never missing: " + from);
            } finally {
                ConstructionRequests.constructions.remove(construction);
            }
        });
    }

    @Test
    public void deadBuilderStopsSuppressing() {
        // The rescue half: a site whose builder died is not coming - it
        // must not suppress the row, or the goal never re-fires and the
        // bot sits on thousands of banked minerals waiting for a dead
        // unit to build.
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 12, 10);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 10),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            construction.setBuilder(builder);
            builder.setHp(0);
            ConstructionRequests.constructions.add(construction);
            try {
                int from = new LiveExistingItems(100).inFlightFrom(
                    UnitProducible.of(AUnitType.Protoss_Pylon));

                assertEquals(ExistingItems.MISSING, from,
                    "a dead builder builds nothing - the row must re-fire: " + from);
            } finally {
                ConstructionRequests.constructions.remove(construction);
            }
        });
    }

    @Test
    public void stuckBuilderStopsSuppressing() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 12, 10);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 10),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            construction.setBuilder(builder);
            builder.setStuckForTests(true);
            ConstructionRequests.constructions.add(construction);
            try {
                int from = new LiveExistingItems(100).inFlightFrom(
                    UnitProducible.of(AUnitType.Protoss_Pylon));

                assertEquals(ExistingItems.MISSING, from,
                    "a wedged builder builds nothing - the row must re-fire: " + from);
            } finally {
                ConstructionRequests.constructions.remove(construction);
            }
        });
    }

    @Test
    public void missingBuildingStaysMissing() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 10)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            int from = new LiveExistingItems(100).inFlightFrom(
                UnitProducible.of(AUnitType.Protoss_Cybernetics_Core));

            assertTrue(from == ExistingItems.MISSING,
                "nothing coming means the scheduler still wants it: " + from);
        });
    }
}
