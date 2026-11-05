package tests.unit;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.execution.GameOrderDirector;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import bwapi.Race;
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
 * Single-demand tech mints once: a second concurrent Cybernetics Core
 * (or Citadel, Forge, Archives...) is always a duplicate, never legit
 * concurrency like a Pylon pair or two Gateways. The V2 mint door owns
 * this (the queue-side wall cannot see V2 mints): a second mint at a
 * different tile while the first row lives is refused as handled.
 * A stuck row still heals - overdue cancels remove it and the next
 * frame mints again.
 */
@Tag("production")
public class TechDuplicateMintTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    private static int liveOf(AUnitType type) {
        return ConstructionRequests.notFinishedOfType(type).size();
    }

    @Test
    public void secondCoreAtAnotherTileIsRefused() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(25, 33);
            currentMinerals = 400;
            ConstructionRequests.constructions.clear();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Cybernetics_Core),
                PlacementReservation.success(30, 30, 100), 22));
            assertEquals(1, liveOf(AUnitType.Protoss_Cybernetics_Core));

            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Cybernetics_Core),
                PlacementReservation.success(40, 40, 100), 22),
                "handled, not minted: the live row already carries the demand");
            assertEquals(1, liveOf(AUnitType.Protoss_Cybernetics_Core),
                "no 2x Core: the second mint at another tile is the duplicate");
        });
    }

    @Test
    public void secondPylonWaitsForFirstToFinish() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(25, 33);
            currentMinerals = 400;
            ConstructionRequests.constructions.clear();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(30, 30, 100), 8));
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(40, 40, 100), 8),
                "dedup reports handled, but must not mint while the first Pylon is unfinished");
            assertEquals(1, liveOf(AUnitType.Protoss_Pylon),
                "one Pylon in flight below 40 supply (legacy cap, ported to the mint door)");

            ConstructionRequests.removeOrder(
                ConstructionRequests.notFinishedOfType(AUnitType.Protoss_Pylon).get(0));
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Pylon),
                PlacementReservation.success(40, 40, 100), 8));
            assertEquals(1, liveOf(AUnitType.Protoss_Pylon),
                "no deadlock: once the first is gone the second mints");
        });
    }

    @Test
    public void gatewayPairStillMintsTwice() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            initSupply(25, 33);
            currentMinerals = 400;
            ConstructionRequests.constructions.clear();

            GameOrderDirector director = new GameOrderDirector();
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                PlacementReservation.success(30, 30, 100), 10));
            assertTrue(director.buildAt(
                UnitProducible.of(AUnitType.Protoss_Gateway),
                PlacementReservation.success(46, 46, 100), 12));
            assertEquals(2, liveOf(AUnitType.Protoss_Gateway),
                "legit concurrency (double Gateway) keeps minting on distinct tiles");
        });
    }
}
