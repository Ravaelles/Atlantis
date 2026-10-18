package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.protoss.ProtossWarping;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The oldest-waiting fallback adoption must name itself: when a warp lands
 * outside every reservation's 2.5-tile radius, the fallback adopts the
 * oldest unfinished construction of the type - and until the twin below,
 * nothing recorded WHICH construction got adopted or how many candidates
 * lost. Mis-adoption (right building, wrong construction) is invisible
 * without it (NEXT #150, still open).
 *
 * <p>The test pins the twin and the no-leak outcome, not which sibling the
 * rule prefers: two unfinished constructions, warp far from both, the
 * oldest must be finished and its twin row must name the warp tile, the
 * adopted tile and the candidate count.</p>
 */
public class AdoptedConstructionTwinTest extends AbstractTestWithWorld {

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

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
    }

    private static Construction waitingAt(AUnitType type, int x, int y) {
        Construction construction = new Construction(type);
        construction.setPositionToBuild(APosition.create(x, y));
        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
        ConstructionRequests.constructions.add(construction);
        return construction;
    }

    @Test
    public void fallbackAdoptionNamesWarpTileAdoptedTileAndCandidates() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            EventLog.clearForTests();
            Construction older = waitingAt(AUnitType.Protoss_Gateway, 10, 10);
            Construction newer = waitingAt(AUnitType.Protoss_Gateway, 50, 50);

            // 28+ tiles from both reservations: the positional lookup
            // (2.5 radius) cannot reach, so only the fallback can adopt.
            FakeUnit warpedFar = fake(AUnitType.Protoss_Gateway, 70, 70);
            ProtossWarping.updateNewBuildingJustWarped(warpedFar);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"ADOPTED_CONSTRUCTION\""),
                    "the fallback adoption must be named, got: " + rows);
            assertTrue(rows.contains("[70,70]"),
                    "the twin must name the warp tile, got: " + rows);
            assertTrue(rows.contains("[10,10]"),
                    "the twin must name the adopted site, got: " + rows);
            assertTrue(rows.contains("\"candidates\":2"),
                    "the twin must name how many constructions lost, got: " + rows);
            assertFalse(
                    ConstructionRequests.constructions.contains(older),
                    "the adopted construction is finished and removed, not leaked");
            assertTrue(
                    ConstructionRequests.constructions.contains(newer),
                    "the losing sibling stays tracked");
        });
    }

    @Test
    public void positionalMatchEmitsNoFallbackTwin() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            EventLog.clearForTests();
            waitingAt(AUnitType.Protoss_Gateway, 12, 10);

            FakeUnit onItsTile = fake(AUnitType.Protoss_Gateway, 12, 10);
            ProtossWarping.updateNewBuildingJustWarped(onItsTile);

            assertFalse(EventLog.toJsonLines().contains("ADOPTED_CONSTRUCTION"),
                    "a positional hit is the normal path - the fallback twin must stay silent");
        });
    }

    @Test
    public void overlappingWarpsAdoptInTurnThenHitTheCannotBranch() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            EventLog.clearForTests();
            Construction older = waitingAt(AUnitType.Protoss_Gateway, 10, 10);
            Construction newer = waitingAt(AUnitType.Protoss_Gateway, 50, 50);

            // First warp lands on the older reservation: positional hit,
            // no fallback twin, older finished and removed.
            ProtossWarping.updateNewBuildingJustWarped(fake(AUnitType.Protoss_Gateway, 10, 10));
            assertFalse(
                    ConstructionRequests.constructions.contains(older),
                    "the positionally matched construction is finished, not leaked");
            assertFalse(EventLog.toJsonLines().contains("ADOPTED_CONSTRUCTION"),
                    "a positional hit must not fire the fallback twin");

            // Second warp on the SAME tile: the older construction is gone
            // and the newer one is 40 tiles away, so only the fallback can
            // adopt - naming the single remaining candidate.
            EventLog.clearForTests();
            ProtossWarping.updateNewBuildingJustWarped(fake(AUnitType.Protoss_Gateway, 10, 10));
            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"ADOPTED_CONSTRUCTION\""),
                    "the overlapping warp must be named, got: " + rows);
            assertTrue(rows.contains("\"candidates\":1"),
                    "one construction lost, got: " + rows);
            assertFalse(
                    ConstructionRequests.constructions.contains(newer),
                    "the fallback finishes the adopted construction");

            // Third warp with nothing left: the CANNOT branch exits
            // without adopting anything and without throwing.
            EventLog.clearForTests();
            ProtossWarping.updateNewBuildingJustWarped(fake(AUnitType.Protoss_Gateway, 70, 70));
            assertTrue(
                    ConstructionRequests.constructions.isEmpty(),
                    "no construction may be invented for a building nobody ordered");
            assertFalse(EventLog.toJsonLines().contains("ADOPTED_CONSTRUCTION"),
                    "with no candidates the twin must stay silent");
        });
    }
}
