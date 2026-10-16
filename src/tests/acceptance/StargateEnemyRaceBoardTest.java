package tests.acceptance;

import atlantis.production.GoalBoardAdvisor;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Second Decision-absorption slice (NEXT #93 track): the board snapshot
 * names the enemy race, so matchup branches stop asking {@code Enemy}
 * mid-chain. First consumer: the Stargate Terran veto (never Stargate vs
 * Terran). The Protoss/Zerg branches keep their Decision shape until
 * their own slices land.
 */
public class StargateEnemyRaceBoardTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        fired = false;
    }

    @Test
    public void snapshotNamesTerranEnemy() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 11)
        ), fakeEnemies(fake(AUnitType.Terran_Marine, 60, 20)), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            enemyRaceInWorld = Race.Terran;

            assertEquals(
                "Terran", GoalBoardAdvisor.snapshot().enemyRace,
                "a Terran enemy must read as Terran on the board");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void snapshotNamesZergEnemy() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 11)
        ), fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            enemyRaceInWorld = Race.Zerg;

            assertEquals(
                "Zerg", GoalBoardAdvisor.snapshot().enemyRace,
                "a Zerg enemy must read as Zerg on the board");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
