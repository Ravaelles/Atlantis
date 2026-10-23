package tests.unit;

import atlantis.battle.cluster.ClusterAnchor;
import atlantis.battle.cluster.ProtossDefendFocusPoint;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Where the Protoss army stands on defend depends on the enemy (owner
 * 2026-10-26, from a tested loss vs Protoss): vs Zerg the line hugs the
 * main for worker support, vs Protoss it forms up 8 tiles from the main
 * choke so a lost fight has room to retreat instead of wiping against
 * our own buildings.
 */
@Tag("combat")
public class ProtossDefendFocusTest extends WorldStubForTests {

    @BeforeEach
    public void clearFocus() {
        ProtossDefendFocusPoint.clearForTests();
    }

    @AfterEach
    public void cleanFocus() {
        ProtossDefendFocusPoint.clearForTests();
    }

    @Test
    public void protossEnemyStandsOffTheChoke() {
        enemyRaceInWorld = Race.Protoss;

        atlantis.map.choke.AChoke choke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(choke.center()).thenReturn(APosition.create(60, 20));

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);
        try (org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                 org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(choke);

            world(1, fakeOurs(nexus, zealot), new FakeUnit[]{}, () -> {
                HasPosition anchor = ClusterAnchor.defendAnchor();
                assertNotNull(anchor, "defend has an answer vs Protoss");

                double distToChoke = anchor.distTo(choke.center());
                assertTrue(
                    distToChoke >= 6 && distToChoke <= 10,
                    "8 tiles off the choke, not on it and not at the base: " + distToChoke);

                assertTrue(
                    anchor.distTo(nexus) > 5,
                    "off the buildings: " + anchor.distTo(nexus));
            });
        }
    }

    @Test
    public void zergEnemyHugsTheMain() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);

        world(1, fakeOurs(nexus, zealot), new FakeUnit[]{}, () -> {
            HasPosition anchor = ClusterAnchor.defendAnchor();

            assertTrue(
                anchor.distTo(nexus) < 1,
                "vs Zerg the line stays on the main for worker support: " + anchor.distTo(nexus));
        });
    }

    @Test
    public void noChokeFallsBackToMain() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 22, 21);

        world(1, fakeOurs(nexus, zealot), new FakeUnit[]{}, () -> {
            HasPosition anchor = ClusterAnchor.defendAnchor();

            assertTrue(
                anchor.distTo(nexus) < 1,
                "no choke, no standoff - the base, as before: " + anchor.distTo(nexus));
        });
    }
}
