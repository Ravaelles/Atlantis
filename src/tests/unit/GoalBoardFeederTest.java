package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.production.goalboard.GoalBoardFeeder;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #91 feeder (board -> queue): active goals nobody owns get exactly
 * one order, then silence. Queue rows don't spend (readiness, builders
 * and the engine still decide), so feeding is safe by construction.
 * Workers/army/expansion/detection stay out (pump, strategy, site
 * approval and response system own them).
 */
@Tag("production")
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
            atlantis.production.orders.production.queue.ReservedResources.reset();

            String fed = GoalBoardFeeder.ensure(blockedState());

            assertTrue(fed.contains("supply-pylon"), "fed ids name the goal, got: " + fed);
            boolean topPylon = false;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().equals(AUnitType.Protoss_Pylon)
                    && order.priority() == ProductionOrderPriority.TOP) {
                    topPylon = true;
                    break;
                }
            }
            assertTrue(topPylon, "a blocked supply feeds one TOP Pylon row");
        });
    }

    @Test
    public void healthySupplyFeedsNoPylon() {        currentSupplyUsed = 25;
        currentSupplyTotal = 33;
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.production.orders.production.queue.ReservedResources.reset();

            BoardState ample = BoardState.builder()
                .minerals(500).supply(25, 33)
                .workers(10).bases(2).gateways(2).army(4)
                .seconds(400)
                .build();
            String fed = GoalBoardFeeder.ensure(ample);

            assertFalse(fed.contains("supply-pylon"),
                "free 8 at 25/33 is no block - the game-2 #30 shape must not feed: " + fed);
            boolean pylonRow = false;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().equals(AUnitType.Protoss_Pylon)) {
                    pylonRow = true;
                    break;
                }
            }
            assertFalse(pylonRow, "healthy supply feeds no Pylon row");
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
            atlantis.production.orders.production.queue.ReservedResources.reset();

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
    public void coveredGoalIsSilent() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.production.orders.production.queue.ReservedResources.reset();

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
            atlantis.production.orders.production.queue.ReservedResources.reset();

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
        // Bank 300 covers the higher-ranked wants first (gateway 150):
        // the wallet funds top-down, so the posture row needs the
        // remainder to still cover a Zealot. The cannon goal is held off
        // (one standing) so this pins the posture funding, not the
        // cannon ladder above it.
        BoardState state = BoardState.builder()
            .minerals(300).gas(0).supply(14, 25)
            .workers(12).bases(1).gateways(1).gatewaysTraining(0).army(8)
            .cannons(1)
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
            atlantis.production.orders.production.queue.ReservedResources.reset();

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
            atlantis.production.orders.production.queue.ReservedResources.reset();

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
            assertFalse(fed.contains("sustained-dragoon"), "an acting pump always wins over the posture default");
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
            atlantis.production.orders.production.queue.ReservedResources.reset();
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
            atlantis.production.orders.production.queue.ReservedResources.reset();
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

    @Test
    public void hiddenThreatFeedsForgeRow() {        // B1 Forge family: the V2 home must serve what the deleted legacy
        // producer served - DT evidence with no Forge funds a Forge row.
        // The stub base holds a Pylon: the queue door rightly refuses
        // power-needing buildings with no power anywhere (EnforcePylonFirst).
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Pylon, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.production.orders.production.queue.ReservedResources.reset();
            ConstructionRequests.constructions.clear();

            BoardState dtThreat = BoardState.builder()
                .minerals(500).gas(200).supply(45, 50)
                .workers(10).bases(1).gateways(2).army(8)
                .forge(false).hiddenUnits(true)
                .seconds(400)
                .build();
            String fed = GoalBoardFeeder.ensure(dtThreat);
            assertTrue(GoalBoardFeeder.wantedGoal("forge"),
                "DT evidence must want the Forge goal");
            assertTrue(GoalBoardFeeder.adoptionAllowed(AUnitType.Protoss_Forge),
                "the Forge goal must be funded, not just wanted");

            assertTrue(fed.contains("forge"), "fed ids name the goal, got: '" + fed
                + "' queue=" + Queue.get().allOrders().list()
                + " | lastStatus=" + atlantis.production.orders.production.queue.QueueLastStatus.status());
            boolean forgeRow = false;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().isForge()) {
                    forgeRow = true;
                    break;
                }
            }
            assertTrue(forgeRow, "DT evidence with no Forge feeds one Forge row");
        });
    }

    @Test
    public void gatewayExistsFeedsCoreRow() {
        // B1 Core family: the V2 home must serve what the deleted legacy
        // producer served - a standing Gateway with no Core funds a Core row.
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Pylon, 22, 22),
            fake(AUnitType.Protoss_Gateway, 24, 24)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.production.orders.production.queue.ReservedResources.reset();
            ConstructionRequests.constructions.clear();

            BoardState opening = BoardState.builder()
                .minerals(400).supply(20, 25)
                .workers(10).bases(1).gateways(1).army(4)
                .forge(false)
                .seconds(300)
                .build();
            String fed = GoalBoardFeeder.ensure(opening);
            assertTrue(GoalBoardFeeder.wantedGoal("core"),
                "a Gateway with no Core wants the Core goal");

            boolean coreRow = false;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().isCyberneticsCore()) {
                    coreRow = true;
                    break;
                }
            }
            assertTrue(coreRow, "a standing Gateway funds one Core row, got: " + fed);
        });
    }

    @Test
    public void nearCompletionCoreFeedsAssimilatorRow() {
        // B1 Assimilator timing: the legacy producer queued gas once the
        // Core was nearly done (under 80 warp seconds left) - the V2 goal
        // must fire on the same sight, not only after completion.
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Pylon, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.production.orders.production.queue.ReservedResources.reset();
            ConstructionRequests.constructions.clear();

            BoardState finishing = BoardState.builder()
                .minerals(400).gas(100).supply(30, 32)
                .workers(10).bases(1).gateways(2).army(6)
                .assimilator(false).coreRemainingSeconds(40)
                .seconds(400)
                .build();
            String fed = GoalBoardFeeder.ensure(finishing);
            assertTrue(GoalBoardFeeder.wantedGoal("assimilator"),
                "Core 40 warp seconds out wants the Assimilator");
            assertTrue(fed.contains("assimilator"), "fed names the goal, got: " + fed);

            boolean assimRow = false;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().isGasBuilding()) {
                    assimRow = true;
                    break;
                }
            }
            assertTrue(assimRow, "near-completion Core funds one Assimilator row");
        });
    }

    @Test
    public void mutalisksFundCannonRow() {
        // B1 Cannon family: the V2 home must fund what the legacy
        // matrix wanted - scouted mutalisks with no cannons. Competitor
        // goals are held off by the state (built core/assim, small calm
        // army, no forge trigger) so the pin names the cannon funding,
        // not wallet arithmetic.
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 22),
            fake(AUnitType.Protoss_Forge, 24, 24),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.production.orders.production.queue.ReservedResources.reset();
            ConstructionRequests.constructions.clear();

            BoardState mutas = BoardState.builder()
                .minerals(600).gas(100).supply(50, 60)
                .workers(10).bases(1).gateways(2).army(4)
                .core(true).assimilator(true).cannons(0)
                .enemyRace("Zerg").enemyUnits(0, 0, 6, false)
                .seconds(500)
                .build();
            String fed = GoalBoardFeeder.ensure(mutas);
            assertTrue(GoalBoardFeeder.wantedGoal("cannon"),
                "6 mutas with no cannons wants the Cannon goal");
            assertTrue(fed.contains("cannon"), "fed names the goal, got: " + fed);

            int cannons = 0;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().isCannon()) {
                    cannons++;
                }
            }
            assertTrue(cannons >= 1, "mutalisks with no cannons fund a Cannon row, got: " + fed);
        });
    }
}
