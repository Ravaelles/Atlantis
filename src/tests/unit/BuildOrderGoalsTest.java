package tests.unit;


import atlantis.production.v2.Producible;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.BuildOrderGoals;
import atlantis.production.v2.goals.BuildOrderProgress;
import atlantis.production.v2.goals.BuildOrderRow;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
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
@Tag("production")
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

    /**
     * <b>The opening Pylon must outrank the workers</b> - the fix for the measured livelock
     * (`NEXT.md` #52), where the worker goal was scheduled first every pass, ate the bank on
     * the tail of the timeline, and the Pylon's start moved +1 per frame forever.
     *
     * <p>
     * Asserted against {@code PRIORITY_WORKERS} rather than against the constant's literal
     * value: the claim is about the ORDERING (the Pylon is planned first), not about what
     * number someone chose.
     * </p>
     */
    @Test
    public void theSupplyProviderAtTheOpeningIsPrioritisedAboveWorkers() {
        List<ProductionGoal> goals = BuildOrderGoals.from(coreOpening(), new Progress(8));

        ProductionGoal pylon = goals.stream()
                .filter(g -> g.item().id().equals(PYLON.id()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the opening Pylon must be planned at supply 8"));

        assertTrue(
                pylon.priority() < ProductionGoal.PRIORITY_WORKERS,
                "the Pylon that unlocks production must be allocated BEFORE the workers "
                        + "(priority " + pylon.priority() + " must beat " + ProductionGoal.PRIORITY_WORKERS
                        + ") - otherwise its start slides and it is never built");
    }

    /**
     * The other half, and the reason the rule is about the OPENING rather than the building
     * type: a Pylon asked for later in the order is an ordinary depot and must stay at
     * {@code PRIORITY_DEPOTS}. Raising every Pylon would starve the workers for the whole
     * game, which is a different bug from the one being fixed.
     */
    @Test
    public void aLaterSupplyProviderIsStillAnOrdinaryDepot() {
        // A bank of 16 with the opening already built, so the row under test is the supply-16
        // one: the supply-8 Pylon is counted as produced, which is why `emittedTypes` lets the
        // later row through at all.
        Progress progress = new Progress(16).with(PYLON, 1);

        List<ProductionGoal> goals = BuildOrderGoals.from(coreOpening(), progress);

        ProductionGoal laterPylon = goals.stream()
                .filter(g -> g.item().id().equals(PYLON.id()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the supply-16 Pylon must be planned at supply 16"));

        assertEquals(
                ProductionGoal.PRIORITY_DEPOTS, laterPylon.priority(),
                "a supply provider asked for at supply 16 is a depot, not the opening - only the "
                        + "building that unlocks production gets the higher band");
    }

    /**
     * The opening rule reads the ROW's supply gate, not its position in the file. A build
     * order may list its Pylon anywhere; with line-number priority a Pylon on line 5 would
     * have silently landed in a lower band and the livelock would come back for that file
     * only - the kind of defect that shows up as \"that one build order is broken\".
     */
    @Test
    public void theOpeningRuleDoesNotDependOnThePositionInTheFile() {
        List<BuildOrderRow> pylonLate = Arrays.asList(
                BuildOrderRow.of(GATEWAY, 9),
                BuildOrderRow.of(ZEALOT, 10),
                BuildOrderRow.of(PYLON, 8));

        List<ProductionGoal> goals = BuildOrderGoals.from(pylonLate, new Progress(8));

        ProductionGoal anywhere = goals.stream()
                .filter(g -> g.item().id().equals(PYLON.id()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the opening Pylon must be planned wherever it sits"));

        assertTrue(
                anywhere.priority() < ProductionGoal.PRIORITY_WORKERS,
                "the opening Pylon keeps its band on line 3 as well as on line 1 - the rule is "
                        + "about what the row IS, not where it is written");
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
                atlantis.battle.cluster.Missions.ATTACK, 18));

        List<BuildOrderRow> rows = BuildOrderRow.fromLegacy(legacy);

        assertEquals(2, rows.size(), "the mission row is not production");
        assertEquals(3, rows.get(0).multiplicity());
        assertEquals(null, rows.get(0).positionModifier());
        assertEquals(1, rows.get(1).multiplicity());
        assertEquals("MAIN", rows.get(1).positionModifier());
    }
}
