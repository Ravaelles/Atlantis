package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.production.GoalBoardFeeder;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #91 feeder (board -> queue): active goals nobody owns get exactly
 * one order, then silence. Queue rows don't spend (readiness, builders
 * and the engine still decide), so feeding is safe by construction.
 * Workers/army/expansion/detection stay out (pump, strategy, site
 * approval and response system own them).
 */
public class GoalBoardFeederTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private BoardState blockedState() {
        return BoardState.builder()
            .minerals(150).supply(9, 9)
            .workers(6).bases(1).gateways(1).army(2)
            .seconds(120)
            .build();
    }

    @Test
    public void blockedSupplyFeedsTopPylon() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            String fed = GoalBoardFeeder.ensure(blockedState());

            assertTrue(fed.contains("supply-pylon"), "fed ids name the goal, got: " + fed);
            boolean topPylon = false;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().equals(AUnitType.Protoss_Pylon)
                    && order.priority() == ProductionOrderPriority.TOP) {
                    topPylon = true;
                }
            }
            assertTrue(topPylon, "a blocked supply feeds one TOP Pylon row");
        });
    }

    @Test
    public void demandGateSeams() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            GoalBoardFeeder.ensure(blockedState());

            assertTrue(GoalBoardFeeder.boardFresh(), "a ranking just ran");
            assertTrue(GoalBoardFeeder.wantedGoal("supply-pylon"), "blocked supply is wanted");
            assertFalse(GoalBoardFeeder.wantedGoal("wall-in"),
                "no Zerg, no core, no wall pieces counted - the wall stays quiet");

            BoardState wallState = BoardState.builder()
                .minerals(400).supply(30, 40)
                .workers(20).bases(2).gateways(2).army(6)
                .enemyRace("Zerg").core(true).wallPieces(0)
                .forgeExpand(true)
                .seconds(400)
                .build();
            GoalBoardFeeder.ensure(wallState);

            assertTrue(GoalBoardFeeder.wantedGoal("wall-in"), "Zerg + core + FE + bare choke is wanted");
            assertFalse(GoalBoardFeeder.wantedGoal("no-such-goal"), "unknown ids are never wanted");
        });
    }

    @Test
    public void coveredGoalIsSilent() {        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 8));
            Queue.get().clearCache();
            int before = Queue.get().allOrders().list().size();

            GoalBoardFeeder.ensure(blockedState());

            assertEquals(before, Queue.get().allOrders().list().size(),
                "covered supply-pylon adds nothing");
        });
    }

    @Test
    public void gatewayFedWorkersArmyExpansionSkipped() {
        BoardState state = BoardState.builder()
            .minerals(300).supply(10, 17)
            .workers(6).bases(1).gateways(0).army(2)
            .seconds(200)
            .build();

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            GoalBoardFeeder.ensure(state);

            int gateways = 0;
            int others = 0;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() == null) {
                    continue;
                }
                if (order.unitType().equals(AUnitType.Protoss_Gateway)) {
                    gateways++;
                    assertEquals(ProductionOrderPriority.STANDARD, order.priority());
                }
                else if (order.unitType().equals(AUnitType.Protoss_Probe)
                    || order.unitType().equals(AUnitType.Protoss_Zealot)
                    || order.unitType().equals(AUnitType.Protoss_Nexus)
                    || order.unitType().equals(AUnitType.Protoss_Forge)) {
                    others++;
                }
            }
            assertEquals(1, gateways, "exactly one STANDARD Gateway row");
            assertEquals(0, others, "workers/army/expansion/detection stay with their owners");
        });
    }

    @Test
    public void stalledGatewaysFeedOneZealotRow() {
        // Bank 300 covers the higher-ranked wants first (workers 50,
        // gateway 150): the wallet funds top-down, so the posture row
        // needs the remainder to still cover a Zealot.
        BoardState state = BoardState.builder()
            .minerals(300).gas(0).supply(14, 25)
            .workers(12).bases(1).gateways(1).gatewaysTraining(0).army(8)
            .seconds(400)
            .build();

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            String fed = GoalBoardFeeder.ensure(state);

            assertTrue(fed.contains("sustained-zealot"), "fed ids name the goal, got: " + fed);
            int zealots = 0;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().equals(AUnitType.Protoss_Zealot)) {
                    zealots++;
                    assertEquals(ProductionOrderPriority.STANDARD, order.priority());
                }
            }
            assertEquals(1, zealots, "one stalled Gateway yields exactly one STANDARD Zealot row");
        });
    }

    @Test
    public void pumpRowSilencesSustainedFeed() {
        BoardState state = BoardState.builder()
            .minerals(200).gas(120).supply(20, 33)
            .workers(14).bases(1).gateways(2).gatewaysTraining(0).army(8)
            .core(true)
            .seconds(500)
            .build();

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Dragoon, 20));
            Queue.get().clearCache();

            String fed = GoalBoardFeeder.ensure(state);

            int dragoons = 0;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().equals(AUnitType.Protoss_Dragoon)) {
                    dragoons++;
                }
            }
            assertEquals(1, dragoons, "the pump's own row stands alone - no feeder duplicate");
            assertTrue(
                !fed.contains("sustained-dragoon"),
                "an acting pump always wins over the posture default");
        });
    }

    /**
     * Wallet enforcement at the single construction door (NEXT #93): a
     * fresh builder claim for a board-owned type needs funding. Supply
     * blocked at 24/25 with 120 bank: the Pylon rescue funds (top), the
     * Gateway does not fit the remainder - so its adoption waits instead
     * of holding a worker for a building the bank cannot cover yet.
     * Inactive demand, goalless types and stale funding stay open.
     */
    @Test
    public void unfundedActiveGatewayRefusesAdoption() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            currentMinerals = 120;
            currentGas = 0;
            currentSupplyUsed = 24;
            currentSupplyTotal = 25;

            BoardState state = BoardState.builder()
                .minerals(120).gas(0).supply(24, 25)
                .workers(30).bases(1).gateways(0).army(8)
                .seconds(400)
                .build();
            GoalBoardFeeder.ensure(state);

            ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            assertFalse(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, gateway),
                "gateway wanted but unfunded - the builder keeps mining");
            assertTrue(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Gateway).isEmpty(),
                "no claim, no construction");
        });
    }

    @Test
    public void fundedStaleAndGoallessAdoptionsProceed() {
        world(60, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21)
        ), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            currentMinerals = 120;
            currentGas = 0;
            currentSupplyUsed = 24;
            currentSupplyTotal = 25;

            if (now == 3) {
                Queue.get().allOrders().list().clear();
                Queue.get().clearCache();
                ConstructionRequests.constructions.clear();
                BoardState state = BoardState.builder()
                    .minerals(120).gas(0).supply(24, 25)
                    .workers(30).bases(1).gateways(0).army(8)
                    .seconds(400)
                    .build();
                GoalBoardFeeder.ensure(state);
            }

            if (now == 40) {
                // Funding published at frame 3 is stale past the 30-frame
                // cadence: fail open, the claim proceeds. The Pylon in the
                // fixture keeps the power-not-funded door open - the point
                // here is the stale wallet, not power.
                ProductionOrder gateway = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
                assertTrue(
                    ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, gateway),
                    "stale funding fails open");

                // Forge has no board goal: legacy owns it outright.
                ConstructionRequests.constructions.clear();
                ProductionOrder forge = new ProductionOrder(AUnitType.Protoss_Forge, 10);
                assertTrue(
                    ProduceBuilding.produceBuilding(AUnitType.Protoss_Forge, forge),
                    "goalless types never touch the wallet");
            }
        });
    }

    /**
     * Commitment wiring: board-owned demand with a live queue row or
     * construction counts as in flight, so `GoalBoard.top` keeps started
     * work instead of flapping. Empty world: nothing is.
     */
    @Test
    public void inFlightIdsMirrorLiveDemand() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            assertTrue(
                GoalBoardFeeder.inFlightIds().isEmpty(),
                "empty queue and no constructions: nothing in flight");

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Gateway, 10));
            Queue.get().clearCache();
            assertTrue(
                GoalBoardFeeder.inFlightIds().contains("gateway"),
                "a live Gateway row is the gateway goal in flight");
            assertFalse(
                GoalBoardFeeder.inFlightIds().contains("core"),
                "nothing Core-ish is going on");
        });
    }
}
