package tests.unit;

import atlantis.units.AUnitNearCounts;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pins the near-counter family before/after its extraction from
 * {@link atlantis.units.AUnit} (NEXT #11, second slice): radius-gated
 * counts read the same through the unit (memoized) and through the
 * extracted pure computation. Written against the delegates, so the
 * extraction keeps every assertion green by construction.
 */
@Tag("infra")
public class AUnitNearCountsTest extends WorldStubForTests {

    @Test
    public void radiusCountsAgree() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 31, 20);
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 32, 20);
        FakeUnit nearLing = fake(AUnitType.Zerg_Zergling, 31, 20);
        nearLing.setEnemy();
        FakeUnit farLing = fake(AUnitType.Zerg_Zergling, 40, 20);
        farLing.setEnemy();

        world(1, fakeOurs(dragoon, probe, nexus), fakeEnemies(nearLing, farLing), () -> {
            assertEquals(1, dragoon.enemiesNearInRadius(2), "only the close ling");
            assertEquals(1, AUnitNearCounts.enemiesNear(dragoon, 2));
            assertEquals(2, dragoon.enemiesNearInRadius(15), "both lings in the wide ring");
            assertEquals(1, dragoon.meleeEnemiesNearCount(), "2.7-tile melee ring");
            assertEquals(1, AUnitNearCounts.meleeEnemiesNear(dragoon));
            assertEquals(2, dragoon.friendsNearCount(), "probe + nexus, self excluded");
            assertEquals(2, AUnitNearCounts.friendsNear(dragoon));
            assertEquals(1, dragoon.ourBuildingsNearCount(5), "nexus in 5");
            assertEquals(1, AUnitNearCounts.ourBuildingsNear(dragoon, 5));
        });
    }
}
