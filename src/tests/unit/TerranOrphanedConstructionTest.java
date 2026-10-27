package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.terran.TerranNewBuilding;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Terran twin of the Protoss orphan adoption (NEXT.md #56): a building
 * that goes up away from its reservation must still be linked to the waiting
 * construction instead of leaking it.
 *
 * <p>
 * The assertions are deliberately the OPPOSITE of the Protoss twin's: a
 * Terran building that just appeared has just STARTED (the SCV is still
 * constructing it), so adoption keeps {@code IN_PROGRESS} with the live
 * builder retained. {@code FINISHED} plus release would evict the tracking
 * mid-build and unblock duplicate requests - the Protoss shape, wrong here.
 * </p>
 */
@Tag("production")
public class TerranOrphanedConstructionTest extends AbstractTestWithWorld {

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
        return Race.Terran;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
    }

    @Test
    public void aBarracksBuiltAwayFromItsReservationIsAdoptedButStaysInProgress() {
        FakeUnit commandCenter = fake(AUnitType.Terran_Command_Center, 10);
        FakeUnit scv = fake(AUnitType.Terran_SCV, 11);

        world(30, units(commandCenter, scv), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            // The construction reserved [12,10] ...
            Construction construction = new Construction(AUnitType.Terran_Barracks);
            construction.setBuilder(scv);
            construction.setPositionToBuild(APosition.create(12, 10));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            // ... and the Barracks went up eight tiles away, outside the
            // positional lookup's radius.
            FakeUnit builtElsewhere = fake(AUnitType.Terran_Barracks, 20);

            // NOTE: the race guard reads hardcoded Main.ourRace(), so no stub
            // world can be Terran - the seam under test is the linking rule.
            TerranNewBuilding.linkBuildingToConstruction(builtElsewhere);

            assertEquals(8.0, construction.buildPosition().distTo(builtElsewhere), 0.01,
                "the fixture must really be outside the positional lookup radius");

            assertTrue(
                ConstructionRequests.constructions.contains(construction),
                "the adopted construction stays tracked (IN_PROGRESS), not finished");
            assertEquals(
                ConstructionOrderStatus.IN_PROGRESS, construction.status(),
                "a Terran building that just appeared has just STARTED - FINISHED"
                    + " would evict the tracking while the SCV is mid-build");
            assertSame(
                scv, construction.builder(),
                "the live builder stays attached (Protoss releases; Terran must not)");
        });
    }

    @Test
    public void aBarracksOnItsReservationIsAdoptedTheSameWay() {
        FakeUnit commandCenter = fake(AUnitType.Terran_Command_Center, 10);
        FakeUnit scv = fake(AUnitType.Terran_SCV, 11);

        world(30, units(commandCenter, scv), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            Construction construction = new Construction(AUnitType.Terran_Barracks);
            construction.setBuilder(scv);
            construction.setPositionToBuild(APosition.create(12, 10));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            FakeUnit onItsTile = fake(AUnitType.Terran_Barracks, 12, 10);

            // Same seam note as above: race guard is untestable, rule is not.
            TerranNewBuilding.linkBuildingToConstruction(onItsTile);

            assertTrue(
                ConstructionRequests.constructions.contains(construction),
                "positional hit adopts without the fallback, same outcome");
            assertEquals(
                ConstructionOrderStatus.IN_PROGRESS, construction.status(),
                "started, not finished - completion belongs downstream");
            assertSame(
                scv, construction.builder(),
                "the live builder stays attached");
        });
    }

    @Test
    public void fallbackAdoptionNamesWarpTileAdoptedTileAndCandidates() {
        FakeUnit commandCenter = fake(AUnitType.Terran_Command_Center, 10);
        FakeUnit scv = fake(AUnitType.Terran_SCV, 11);

        world(30, units(commandCenter, scv), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            EventLog.clearForTests();
            Construction older = new Construction(AUnitType.Terran_Barracks);
            older.setBuilder(scv);
            older.setPositionToBuild(APosition.create(10, 10));
            older.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(older);

            Construction newer = new Construction(AUnitType.Terran_Barracks);
            newer.setPositionToBuild(APosition.create(50, 50));
            newer.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(newer);

            // 28+ tiles from both reservations: the positional lookup
            // (2.5 radius) cannot reach, so only the fallback can adopt.
            FakeUnit builtFar = fake(AUnitType.Terran_Barracks, 70, 70);
            TerranNewBuilding.linkBuildingToConstruction(builtFar);

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"ADOPTED_CONSTRUCTION\""),
                "the Terran fallback adopts through the same twin, got: " + rows);
            assertTrue(rows.contains("[70,70]"),
                "the twin must name the warp tile, got: " + rows);
            assertTrue(rows.contains("[10,10]"),
                "the twin must name the adopted site, got: " + rows);
            assertTrue(rows.contains("\"candidates\":2"),
                "the twin must name how many constructions lost, got: " + rows);
            assertEquals(
                ConstructionOrderStatus.IN_PROGRESS, older.status(),
                "a Terran building that just appeared has just STARTED");
            assertSame(
                scv, older.builder(),
                "the live SCV stays attached");
            assertTrue(
                ConstructionRequests.constructions.contains(newer),
                "the losing sibling stays tracked");
        });
    }
}
