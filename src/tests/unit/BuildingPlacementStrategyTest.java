package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.strategy.BuildingPlacementStrategies;
import atlantis.placement.strategy.BuildingPlacementStrategy;
import atlantis.placement.strategy.DefaultPlacementStrategy;
import atlantis.placement.strategy.GatewayPlacementStrategy;
import atlantis.placement.strategy.PylonPlacementStrategy;
import atlantis.placement.strategy.StreetCells;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Building placement strategies (owner 2026-11-09): Pylons own range
 * on street cells, Gateways sit on existing power nearest-Pylon
 * first (never unpowered - null instead), everything else follows
 * the street grid with the mint-time power predicate for
 * power-needing buildings.
 *
 * <p>Pure shape, no production writes.</p>
 */
@Tag("placement")
public class BuildingPlacementStrategyTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private static BuildingPlacementStrategy.PlacementQuery query(
        AUnitType type, int tx, int ty, java.util.Set<String> excluded) {
        return new BuildingPlacementStrategy.PlacementQuery(
            type, APosition.create(tx, ty), -1, excluded, (tile, t) -> false);
    }

    @Test
    public void dispatcherRoutesByGroup() {
        assertTrue(BuildingPlacementStrategies.forType(AUnitType.Protoss_Pylon)
            instanceof PylonPlacementStrategy, "pylons own range");
        assertTrue(BuildingPlacementStrategies.forType(AUnitType.Protoss_Gateway)
            instanceof GatewayPlacementStrategy, "gateways sit on power");
        assertTrue(BuildingPlacementStrategies.forType(AUnitType.Protoss_Forge)
            instanceof DefaultPlacementStrategy, "the rest follows the grid");
        assertTrue(BuildingPlacementStrategies.forType(AUnitType.Protoss_Photon_Cannon)
            instanceof DefaultPlacementStrategy, "cannons are grid too");
    }

    @Test
    public void pylonLandsOnStrictCell() {
        world(2, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            APosition tile = new PylonPlacementStrategy().find(
                query(AUnitType.Protoss_Pylon, 20, 20, new HashSet<>()));

            assertNotNull(tile, "a pylon always lands somewhere");
            assertTrue(StreetCells.isPylonCell(tile.tx(), tile.ty()),
                "strict pylon cells first: " + tile);
        });
    }

    @Test
    public void pylonHonoursExcludedTiles() {
        world(2, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            PylonPlacementStrategy strategy = new PylonPlacementStrategy();
            APosition first = strategy.find(
                query(AUnitType.Protoss_Pylon, 20, 20, new HashSet<>()));
            assertNotNull(first, "fixture check: the first pick serves");

            java.util.Set<String> excluded = new HashSet<>();
            excluded.add(first.tx() + ":" + first.ty());
            APosition second = strategy.find(
                query(AUnitType.Protoss_Pylon, 20, 20, excluded));

            assertNotNull(second, "a refused tile is skipped, not fatal");
            assertTrue(!first.equals(second), "the exclusion moves the pick");
        });
    }

    @Test
    public void gatewaySitsOnStandingPower() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 30, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            APosition tile = new GatewayPlacementStrategy().find(
                query(AUnitType.Protoss_Gateway, 20, 20, new HashSet<>()));

            assertNotNull(tile, "power stands: the gateway sits");
            assertTrue(
                atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon
                    .check(tile, AUnitType.Protoss_Gateway),
                "the mint-time predicate agrees: " + tile);
            assertTrue(StreetCells.isGatewayCell(tile.tx(), tile.ty()),
                "on a gateway street cell: " + tile);
        });
    }

    @Test
    public void gatewayAnswersNullWhenDark() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertNull(new GatewayPlacementStrategy().find(
                    query(AUnitType.Protoss_Gateway, 20, 20, new HashSet<>())),
                "no pylon anywhere: never an unpowered tile, null instead");
        });
    }

    @Test
    public void gatewayPrefersNearestPylon() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 60, 20),
            fake(AUnitType.Protoss_Pylon, 28, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            APosition tile = new GatewayPlacementStrategy().find(
                query(AUnitType.Protoss_Gateway, 20, 20, new HashSet<>()));

            assertNotNull(tile, "power stands: the gateway sits");
            assertTrue(tile.distTo(APosition.create(28, 20)) < tile.distTo(APosition.create(60, 20)),
                "the nearer pylon owns the gateway: " + tile);
        });
    }

    @Test
    public void forgeNeedsPowerToo() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 30, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            APosition tile = new DefaultPlacementStrategy().find(
                query(AUnitType.Protoss_Forge, 20, 20, new HashSet<>()));

            assertNotNull(tile, "power stands: the forge sits");
            assertTrue(
                atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon
                    .check(tile, AUnitType.Protoss_Forge),
                "non-pylon, non-base, non-gas: powered, always: " + tile);
        });
    }

    @Test
    public void darkForgeAnswersNull() {
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertNull(new DefaultPlacementStrategy().find(
                    query(AUnitType.Protoss_Forge, 20, 20, new HashSet<>())),
                "no pylon anywhere: never an unpowered tile, null instead");
        });
    }
}
