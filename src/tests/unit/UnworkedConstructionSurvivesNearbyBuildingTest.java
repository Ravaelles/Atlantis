package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner 2026-10-15 (NEXT #57): a freshly accepted construction whose
 * builder never issued must not be finished off by a nearby standing
 * building of the same type. The live loop accepted the same Gateway
 * tile 50+ times with totalOfType pinned at 1 and no cancel line: every
 * accept was silently finished-removed because an earlier Gateway stood
 * within 2.5 tiles, so no builder ever walked long enough to warp.
 *
 * <p>The proximity finish exists for worked constructions (NEXT #53: a
 * builder that built for 526 frames while the status never advanced).
 * An unworked one is still owed - finishing it drops demand the queue
 * believes is covered. Attempted (or started) work keeps the old
 * behavior; only never-attempted, builder-assigned, NOT_STARTED waits.</p>
 */
@Tag("production")
public class UnworkedConstructionSurvivesNearbyBuildingTest extends AbstractTestWithWorld {

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

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void unworkedAdoptedConstructionIsNotFinishedByNeighbor() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            fake(AUnitType.Protoss_Gateway, 32, 30),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.clear();

            Construction fresh = new Construction(AUnitType.Protoss_Gateway);
            fresh.setPositionToBuild(APosition.create(30, 30));
            fresh.setStatus(ConstructionOrderStatus.NOT_STARTED);
            fresh.setBuilder(builder);
            ConstructionRequests.addConstruction(fresh);

            new ConstructionStatusChanger().forceHandle();

            assertNotEquals(
                ConstructionOrderStatus.FINISHED, fresh.status(),
                "no attempt was ever issued for this site - a Gateway standing "
                    + "2 tiles away must not finish work that never started");
            assertTrue(
                ConstructionRequests.constructions.contains(fresh),
                "the owed construction stays listed until its builder works it");
        });
    }

    @Test
    public void attemptedConstructionStillFinishesByNeighbor() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            fake(AUnitType.Protoss_Gateway, 32, 30),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.clear();

            Construction worked = new Construction(AUnitType.Protoss_Gateway);
            worked.setPositionToBuild(APosition.create(30, 30));
            worked.setStatus(ConstructionOrderStatus.NOT_STARTED);
            worked.setBuilder(builder);
            worked.setLastBuildAttemptNow();
            ConstructionRequests.addConstruction(worked);

            new ConstructionStatusChanger().forceHandle();

            assertEquals(
                ConstructionOrderStatus.FINISHED, worked.status(),
                "attempted work with a standing building nearby keeps the old "
                    + "proximity finish (NEXT #53)");
        });
    }
}
