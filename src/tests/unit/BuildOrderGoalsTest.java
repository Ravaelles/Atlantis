package tests.unit;

import atlantis.production.v2.Producible;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.BuildOrderGoals;
import atlantis.production.v2.goals.BuildOrderProgress;
import atlantis.production.v2.goals.BuildOrderRow;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Build-order rows as stateless goals: a row is the N-th occurrence of its
 * item and is emitted only while fewer than N exist (completed, queued or
 * pending), and only once its supply gate is open.
 */
public class BuildOrderGoalsTest {

    private static final Producible PYLON = UnitProducible.of(AUnitType.Protoss_Pylon);
    private static final Producible GATEWAY = UnitProducible.of(AUnitType.Protoss_Gateway);
    private static final Producible ZEALOT = UnitProducible.of(AUnitType.Protoss_Zealot);

    private static final class Progress implements BuildOrderProgress {
        int supply;
        final Map<String, Integer> produced = new HashMap<>();

        Progress(int supply) {
            this.supply = supply;
        }

        Progress with(Producible item, int count) {
            produced.put(item.id(), count);
            return this;
        }

        @Override
        public int supplyUsed() {
            return supply;
        }

        @Override
        public int produced(Producible item) {
            return produced.getOrDefault(item.id(), 0);
        }
    }

    private static List<BuildOrderRow> coreOpening() {
        return Arrays.asList(
                BuildOrderRow.of(PYLON, 8),
                BuildOrderRow.of(GATEWAY, 10),
                BuildOrderRow.of(PYLON, 16));
    }

    @Test
    public void theSameRowIsEmittedEveryFrameUntilItIsProduced() {
        Progress progress = new Progress(8);

        List<ProductionGoal> frame1 = BuildOrderGoals.from(coreOpening(), progress);
        List<ProductionGoal> frame2 = BuildOrderGoals.from(coreOpening(), progress);

        assertEquals(1, frame1.size());
        assertEquals(PYLON.id(), frame1.get(0).item().id());
        assertEquals(1, frame1.get(0).count());
        assertEquals(frame1.toString(), frame2.toString(), "stateless: same input, same goals");
    }

    @Test
    public void aRowAlreadyQueuedOrUnderConstructionIsNotEmittedAgain() {
        // The first Pylon exists (pending, building or done - the port counts all).
        Progress progress = new Progress(10).with(PYLON, 1);

        List<ProductionGoal> goals = BuildOrderGoals.from(coreOpening(), progress);

        assertEquals(1, goals.size(), "only the Gateway is missing: " + goals);
        assertEquals(GATEWAY.id(), goals.get(0).item().id());
    }

    @Test
    public void theSecondOccurrenceOfAnItemNeedsTwoInTheGame() {
        Progress progress = new Progress(16).with(PYLON, 1).with(GATEWAY, 1);

        List<ProductionGoal> goals = BuildOrderGoals.from(coreOpening(), progress);

        assertEquals(1, goals.size());
        assertEquals(PYLON.id(), goals.get(0).item().id(), "the 16-supply Pylon is the second Pylon");
    }

    @Test
    public void everythingProducedMeansNoGoals() {
        Progress progress = new Progress(30).with(PYLON, 2).with(GATEWAY, 1);

        assertTrue(BuildOrderGoals.from(coreOpening(), progress).isEmpty());
    }

    @Test
    public void supplyGateUsesTheLegacyLookahead() {
        // Below 19 supply the row may start one supply early; from 19 on, three.
        List<BuildOrderRow> rows = Arrays.asList(BuildOrderRow.of(ZEALOT, 12));
        assertTrue(BuildOrderGoals.from(rows, new Progress(10)).isEmpty());
        assertEquals(1, BuildOrderGoals.from(rows, new Progress(11)).size());

        List<BuildOrderRow> late = Arrays.asList(BuildOrderRow.of(ZEALOT, 24));
        assertTrue(BuildOrderGoals.from(late, new Progress(20)).isEmpty());
        assertEquals(1, BuildOrderGoals.from(late, new Progress(21)).size());
    }

    @Test
    public void laterRowsWaitForTheEarliestUnproducedOccurrenceOfTheirType() {
        // Once supply 16 is reached, the build order has asked for its second
        // Pylon, but the first one is still missing. The same type must not emit
        // both rows as simultaneous goals; otherwise LIVE schedules duplicate
        // Pylons on the same tile (NEXT #48, measured on OpenBW).
        Progress progress = new Progress(16);

        List<ProductionGoal> goals = BuildOrderGoals.from(coreOpening(), progress);

        assertEquals(2, goals.size(), "first Pylon and Gateway rows are due: " + goals);
        assertEquals(PYLON.id(), goals.get(0).item().id());
        assertEquals(1, goals.get(0).count());
        assertEquals(GATEWAY.id(), goals.get(1).item().id());
        assertEquals(1, goals.get(1).count());
    }

    @Test
    public void multiplierRowsBecomeOneGoalWithTheMissingCount() {
        List<BuildOrderRow> rows = Arrays.asList(new BuildOrderRow(ZEALOT, 20, 3, null));

        assertEquals(3, BuildOrderGoals.from(rows, new Progress(20)).get(0).count());
        assertEquals(1, BuildOrderGoals.from(rows, new Progress(20).with(ZEALOT, 2)).get(0).count(),
                "two of three exist - one more is missing");
    }

    @Test
    public void positionModifierBecomesANamedArea() {
        List<BuildOrderRow> rows = Arrays.asList(new BuildOrderRow(PYLON, 8, 1, "NATURAL_CHOKE"));

        TargetPlacement placement = BuildOrderGoals.from(rows, new Progress(8)).get(0).placement();

        assertEquals(TargetPlacement.Mode.NAMED_AREA, placement.mode());
        assertEquals("NATURAL_CHOKE", placement.areaName());
    }

    @Test
    public void legacyAdapterParsesMultipliersAndDropsMissions() {
        List<atlantis.production.orders.production.queue.order.ProductionOrder> legacy = new ArrayList<>();
        atlantis.production.orders.production.queue.order.ProductionOrder tanks = new atlantis.production.orders.production.queue.order.ProductionOrder(
                AUnitType.Terran_Siege_Tank_Tank_Mode, 40);
        tanks.setModifier("x3");
        legacy.add(tanks);
        atlantis.production.orders.production.queue.order.ProductionOrder bunker = new atlantis.production.orders.production.queue.order.ProductionOrder(
                AUnitType.Terran_Bunker, 21);
        bunker.setModifier("@MAIN");
        legacy.add(bunker);
        legacy.add(new atlantis.production.orders.production.queue.order.ProductionOrder(
                atlantis.combat.missions.Missions.ATTACK, 18));

        List<BuildOrderRow> rows = BuildOrderRow.fromLegacy(legacy);

        assertEquals(2, rows.size(), "the mission row is not production");
        assertEquals(3, rows.get(0).multiplicity());
        assertEquals(null, rows.get(0).positionModifier());
        assertEquals(1, rows.get(1).multiplicity());
        assertEquals("MAIN", rows.get(1).positionModifier());
    }
}
