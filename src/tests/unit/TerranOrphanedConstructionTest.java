package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.terran.TerranNewBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
