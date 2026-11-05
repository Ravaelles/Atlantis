package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A builderless row with a covering bank past 10 s is stuck, not
 * waiting (measured live TauCross: a natural Nexus stood builderless
 * 4000f+ on a hoarding bank while the zombie row suppressed its own
 * demand - second base never came). The status loop re-arms it on the
 * spot and names it; a young row, a staffed row and a broke row stay
 * silent.
 */
@Tag("placement")
public class BuilderlessRowRearmTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        ConstructionStatusChanger.clearStuckForTests();
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        ConstructionStatusChanger.clearStuckForTests();
    }

    private static Construction builderlessRow() {
        Construction row = new Construction(AUnitType.Protoss_Gateway);
        row.setPositionToBuild(APosition.create(30, 30));
        row.setStatus(ConstructionOrderStatus.NOT_STARTED);
        row.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Gateway, 10));
        ConstructionRequests.constructions.add(row);

        return row;
    }

    @Test
    public void stuckRowRearmsAndNamesItself() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentMinerals = 400;
            Construction row = builderlessRow();

            atlantis.game.A.setNow(300, 10);
            assertTrue(ConstructionStatusChanger.rearmBuilderlessRow(row),
                "banked and builderless past 10 s: a free probe must be assigned");
            assertNotNull(row.builder(), "re-arm assigns the builder");
            assertTrue(row.builder().isAlive());

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BUILDER_STUCK\"")
                && rows.contains("re-armed"),
                "the re-arm names itself: " + rows);
        });
    }

    @Test
    public void youngRowStaysSilent() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentMinerals = 400;
            Construction row = builderlessRow();

            assertFalse(ConstructionStatusChanger.rearmBuilderlessRow(row),
                "a fresh row may still be dispatching: no twin, no grab");
            assertNull(row.builder());
            assertFalse(EventLog.toJsonLines().contains("BUILDER_STUCK"),
                "young rows stay silent: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void brokeRowStaysSilent() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            currentMinerals = 10;
            Construction row = builderlessRow();

            atlantis.game.A.setNow(300, 10);
            assertFalse(ConstructionStatusChanger.rearmBuilderlessRow(row),
                "10 minerals cannot pay a Gateway: waiting for bank is legitimate");
            assertFalse(EventLog.toJsonLines().contains("BUILDER_STUCK"),
                "broke rows stay silent: " + EventLog.toJsonLines());
        });
    }
}
