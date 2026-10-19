package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.units.workers.defence.run.WorkerDefenceRun;
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
 * Owner decision (review R6): a builder is never drafted into defence -
 * it has a building to raise, and drafting it is the same hijack class
 * as scouting or repairing with it. An assigned-but-waiting Protoss
 * probe counts: builder is an assignment, not a moment.
 */
public class WorkerDefenceSparesBuildersTest extends AbstractTestWithWorld {

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
    public void hermetic() {
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void assignedBuilderIsNotEligible() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 24),
            fake(AUnitType.Protoss_Probe, 22, 24)
        ), fakeEnemies(), () -> {
            FakeUnit victim = fake(AUnitType.Protoss_Probe, 23, 24);
            FakeUnit builder = fake(AUnitType.Protoss_Probe, 21, 24);
            FakeUnit free = fake(AUnitType.Protoss_Probe, 22, 24);

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(builder);
            construction.setPositionToBuild(atlantis.map.position.APosition.create(95, 72));
            ConstructionRequests.addConstruction(construction);

            assertFalse(
                WorkerDefenceRun.isEligibleHelper(builder, victim),
                "linked waiter is not drafted, even while it constructs nothing");
            assertTrue(
                WorkerDefenceRun.isEligibleHelper(free, victim),
                "a truly free worker still answers");
        });
    }
}
