package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.PositionFulfillsAllConditions;
import atlantis.production.constructions.position.RefusedTiles;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.TileMapSource;

import static org.junit.jupiter.api.Assertions.*;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A row parked on an engine-refused tile must move instead of standing:
 * the builder walks, the engine says no, the row demotes or ages out,
 * and the re-arm walks back to the same cursed tile (measured live
 * 2026-11-03 vs Steamhammer: Gateway at [123,26], 82 s of refused
 * Builds, then the cancel churn with the bank full).
 *
 * <p>Pinned where the loop breaks: the refresh re-sites a refused row
 * and revokes the builder's stale orders (the travel chain never
 * re-issues on its own - NEXT #53), and the shared legacy chain
 * declines refused tiles so the spiral cannot re-serve them (the
 * catalogue already did). A clean tile keeps its row and never stops
 * its builder.</p>
 */
@Tag("production")
public class RefusedTileResitesRowTest extends WorldStubForTests {

    private static final APosition CURSED = APosition.create(40, 33);
    private static final APosition FRESH = APosition.create(50, 33);

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
        AbstractPositionFinder._STATUS = "Init";
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
        RefusedTiles.clearForTests();
    }

    @Test
    public void sameTileRefreshStaysSilent() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 60, 60);
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                currentMinerals = 300;
                RefusedTiles.note(CURSED, AUnitType.Protoss_Pylon);
                Construction row = new Construction(AUnitType.Protoss_Pylon) {
                    @Override
                    public APosition findPositionForNewBuilding() {
                        return CURSED;
                    }
                };
                row.setPositionToBuild(CURSED);
                row.setBuilder(builder);
                ConstructionRequests.addConstruction(row);

                int before = countRefreshedTwins();
                RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                    builder, row, AUnitType.Protoss_Pylon, CURSED, builder);

                assertEquals(CURSED, row.buildPosition(),
                    "same tile back is not a move");
                assertEquals(before, countRefreshedTwins(),
                    "no move, no twin, no log line - the 81-twins-per-Gateway spam ends here");
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    @Test
    public void movedTileTwinsOnce() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 60, 60);
        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                currentMinerals = 300;
                RefusedTiles.note(CURSED, AUnitType.Protoss_Pylon);
                Construction row = rowAt(CURSED, builder);

                int before = countRefreshedTwins();
                RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                    builder, row, AUnitType.Protoss_Pylon, CURSED, builder);

                assertEquals(FRESH, row.buildPosition(), "a better tile still moves");
                assertEquals(before + 1, countRefreshedTwins(),
                    "a real move twins exactly once, with both tiles named");
                assertTrue(atlantis.util.log.EventLog.toJsonLines().contains("\"prevTile\""),
                    "the twin names where the row came from: "
                        + atlantis.util.log.EventLog.toJsonLines());
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    private static int countRefreshedTwins() {
        String log = atlantis.util.log.EventLog.toJsonLines();
        int count = 0;
        int from = 0;
        while (true) {
            int at = log.indexOf("\"result\":\"refreshed\"", from);
            if (at < 0) return count;
            count++;
            from = at + 1;
        }
    }

    private static Construction rowAt(APosition site, FakeUnit builder) {
        Construction row = new Construction(AUnitType.Protoss_Pylon) {
            @Override
            public APosition findPositionForNewBuilding() {
                return FRESH;
            }
        };
        row.setPositionToBuild(site);
        if (builder != null) row.setBuilder(builder);
        ConstructionRequests.addConstruction(row);
        return row;
    }

    @Test
    public void refusedTileMovesRowAndStopsBuilder() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 40, 33);
        builder.lastCommand = "Move";

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                RefusedTiles.note(CURSED, AUnitType.Protoss_Pylon);
                Construction row = rowAt(CURSED, builder);

                APosition after = RefreshConstructionPosition.refreshIfNeeded(row);

                assertEquals(FRESH, after,
                    "a refused site is never kept: the row re-sites instead of standing");
                assertEquals(FRESH, row.buildPosition(),
                    "the resite lands on the row, not just in the return value");
                assertEquals(Actions.STOP, builder.action(),
                    "the stale travel order is revoked so the walk restarts at the fresh site (NEXT #53)");
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    @Test
    public void cleanTileKeepsRowAndLeavesBuilderAlone() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 40, 33);
        builder.lastCommand = "Move";

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                Construction row = rowAt(CURSED, builder);

                APosition after = RefreshConstructionPosition.refreshIfNeeded(row);

                assertEquals(CURSED, after,
                    "a healthy site never moves on refresh");
                assertNotEquals(Actions.STOP, builder.action(),
                    "nothing is revoked when there is nowhere to move to");
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    @Test
    public void sharedChainDeclinesRefusedTile() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 60, 60);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                RefusedTiles.note(CURSED, AUnitType.Protoss_Pylon);

                assertFalse(
                    PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                        builder, AUnitType.Protoss_Pylon, CURSED,
                        atlantis.map.position.APosition.create(20, 20)),
                    "the legacy spiral must not re-serve a refused tile");
                assertTrue(
                    AbstractPositionFinder._STATUS != null
                        && AbstractPositionFinder._STATUS.contains("refused"),
                    "the refusal names itself: " + AbstractPositionFinder._STATUS);
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    @Test
    public void sharedChainNeverNamesRefusedWithoutANote() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 60, 60);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                    builder, AUnitType.Protoss_Pylon, CURSED,
                    atlantis.map.position.APosition.create(20, 20));

                assertFalse(
                    AbstractPositionFinder._STATUS != null
                        && AbstractPositionFinder._STATUS.contains("refused"),
                    "no note, no refusal name - whatever else declines the tile: "
                        + AbstractPositionFinder._STATUS);
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }
}
