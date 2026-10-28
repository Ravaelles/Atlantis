package tests.unit;

import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Hidden-unit crisis response (owner: lurkers and Dark Templar must trigger
 * detection - fast Robotics Facility into Observatory and Observer, or
 * Forge plus cannons when there is no time). PurpleWave answers the same
 * shape (observer pump 1-vs-3 on lurker-likelihood plus cannons at
 * openings, behind the Robo chain); ours queues both halves with top
 * priority on first sight. Unpinned until now - the owner asked whether
 * it works at all.
 */
@Tag("production")
public class HiddenUnitResponseTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @AfterEach
    public void cleanQueueAndFlag() {
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = false;
    }

    private static boolean queueHas(AUnitType type) {
        return Queue.get().allOrders().list().stream()
            .anyMatch(order -> type.equals(order.unitType()));
    }

    @Test
    public void lurkerSightRequestsDetectionChain() {
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 60, 60);
        lurker.setEnemy();

        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 10)
        ), fakeEnemies(lurker), () -> {
            if (atlantis.game.A.now() != 10) return;

            Queue.get().allOrders().list().clear();
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(lurker);

            assertTrue(EnemyFlags.HAS_HIDDEN_COMBAT_UNIT,
                "a seen lurker latches the hidden-unit flag");

            assertTrue(queueHas(AUnitType.Protoss_Robotics_Facility),
                "fast Robo first: " + Queue.get().allOrders().list());
            assertTrue(queueHas(AUnitType.Protoss_Observatory),
                "Observatory behind it: " + Queue.get().allOrders().list());
            assertTrue(queueHas(AUnitType.Protoss_Observer),
                "and the Observer itself: " + Queue.get().allOrders().list());
        });
    }

    @Test
    public void lurkerSightWithoutForgeRequestsForgeAndCannons() {
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, 60, 60);
        lurker.setEnemy();

        world(30, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 11, 10)
        ), fakeEnemies(lurker), () -> {
            if (atlantis.game.A.now() != 10) return;

            Queue.get().allOrders().list().clear();
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(lurker);

            assertTrue(queueHas(AUnitType.Protoss_Forge),
                "no time for tech without a Forge - it goes first: "
                    + Queue.get().allOrders().list());
            assertTrue(queueHas(AUnitType.Protoss_Photon_Cannon),
                "cannons hold while the Robo chain spins up: "
                    + Queue.get().allOrders().list());
        });
    }
}
