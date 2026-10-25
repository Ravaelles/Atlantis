package tests.unit;

import atlantis.combat.cluster.ClusterAnchor;
import atlantis.combat.focus.MiddleFocusChoke;
import atlantis.combat.focus.ProtossDefendFocusPoint;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import atlantis.battle.advance.focus.AFocusPoint;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

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

    @Test
    public void bigArmyStagesAtMiddleNotMain() {
        enemyRaceInWorld = Race.Protoss;

        atlantis.map.choke.AChoke mainChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));
        atlantis.map.choke.AChoke middleChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(middleChoke.center()).thenReturn(APosition.create(100, 100));

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[]{
            fake(AUnitType.Protoss_Zealot, 22, 21),
            fake(AUnitType.Protoss_Zealot, 23, 21),
            fake(AUnitType.Protoss_Zealot, 22, 22),
            fake(AUnitType.Protoss_Zealot, 23, 22),
            fake(AUnitType.Protoss_Zealot, 24, 21),
            fake(AUnitType.Protoss_Zealot, 24, 22)};
        FakeUnit[] ours = new FakeUnit[]{nexus, army[0], army[1], army[2], army[3], army[4], army[5]};

        try (org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                 org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             org.mockito.MockedStatic<MiddleFocusChoke> middle =
                 org.mockito.Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            middle.when(MiddleFocusChoke::get).thenReturn(middleChoke);

            world(1, fakeOurs(ours), new FakeUnit[]{}, () -> {
                HasPosition anchor = ClusterAnchor.defendAnchor();
                assertNotNull(anchor, "defend has an answer with a 6+ army");
                assertTrue(
                    anchor instanceof AFocusPoint
                        && ((AFocusPoint) anchor).name().contains("MiddleFocusChoke"),
                    "a 6+ Protoss army stages at the middle, never the main: " + anchor);

                assertTrue(
                    anchor.distTo(middleChoke.center()) < anchor.distTo(mainChoke.center()),
                    "closer to the middle than to the main choke");
            });
        }
    }

    @Test
    public void bigArmyWithoutMiddleFallsToNaturalNotMain() {
        enemyRaceInWorld = Race.Protoss;

        atlantis.map.choke.AChoke mainChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));
        FakeChoke naturalChoke = new FakeChoke(APosition.create(40, 40), 3);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 40, 44);
        FakeUnit[] ours = new FakeUnit[]{
            nexus, natural,
            fake(AUnitType.Protoss_Zealot, 22, 21),
            fake(AUnitType.Protoss_Zealot, 23, 21),
            fake(AUnitType.Protoss_Zealot, 22, 22),
            fake(AUnitType.Protoss_Zealot, 23, 22),
            fake(AUnitType.Protoss_Zealot, 24, 21),
            fake(AUnitType.Protoss_Zealot, 24, 22)};

        try (org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                 org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             org.mockito.MockedStatic<atlantis.map.base.Bases> bases =
                 org.mockito.Mockito.mockStatic(atlantis.map.base.Bases.class);
             org.mockito.MockedStatic<MiddleFocusChoke> middle =
                 org.mockito.Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            chokes.when(atlantis.map.choke.Chokes::natural).thenReturn(naturalChoke);
            bases.when(atlantis.map.base.Bases::natural).thenReturn(natural);
            middle.when(MiddleFocusChoke::get).thenReturn(null);

            world(1, fakeOurs(ours), new FakeUnit[]{}, () -> {
                HasPosition anchor = ClusterAnchor.defendAnchor();
                assertNotNull(anchor, "defend has an answer with a 6+ army and no middle");
                assertTrue(
                    anchor instanceof AFocusPoint
                        && ((AFocusPoint) anchor).name().contains("NaturalChoke"),
                    "no middle: a 6+ Protoss army falls to the natural, never the main: " + anchor);
            });
        }
    }

    @Test
    public void smallArmyWithoutMiddleStillHoldsMainStandoff() {
        enemyRaceInWorld = Race.Protoss;

        atlantis.map.choke.AChoke mainChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ours = new FakeUnit[]{
            nexus,
            fake(AUnitType.Protoss_Zealot, 22, 21),
            fake(AUnitType.Protoss_Zealot, 23, 21),
            fake(AUnitType.Protoss_Zealot, 22, 22),
            fake(AUnitType.Protoss_Zealot, 23, 22),
            fake(AUnitType.Protoss_Zealot, 24, 21)};

        try (org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                 org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             org.mockito.MockedStatic<MiddleFocusChoke> middle =
                 org.mockito.Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            middle.when(MiddleFocusChoke::get).thenReturn(null);

            world(1, fakeOurs(ours), new FakeUnit[]{}, () -> {
                HasPosition anchor = ClusterAnchor.defendAnchor();
                assertNotNull(anchor, "defend has an answer with a 5 army");
                assertTrue(
                    anchor instanceof AFocusPoint
                        && ((AFocusPoint) anchor).name().contains("MainChoke"),
                    "below 6 units the old main-choke standoff owns the line: " + anchor);
            });
        }
    }

    @Test
    public void elevenDragoonsBanMainEvenSparta() {
        enemyRaceInWorld = Race.Protoss;

        atlantis.map.choke.AChoke mainChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));
        atlantis.map.choke.AChoke middleChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(middleChoke.center()).thenReturn(APosition.create(100, 100));

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] dragoons = new FakeUnit[11];
        for (int i = 0; i < 11; i++) {
            dragoons[i] = fake(AUnitType.Protoss_Dragoon, 22 + (i % 4), 21 + (i / 4));
        }
        FakeUnit[] ours = new FakeUnit[12];
        ours[0] = nexus;
        System.arraycopy(dragoons, 0, ours, 1, 11);

        try (org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                 org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             org.mockito.MockedStatic<MiddleFocusChoke> middle =
                 org.mockito.Mockito.mockStatic(MiddleFocusChoke.class);
             org.mockito.MockedStatic<atlantis.battle.cluster.Missions> missions =
                 org.mockito.Mockito.mockStatic(atlantis.battle.cluster.Missions.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            middle.when(MiddleFocusChoke::get).thenReturn(middleChoke);
            missions.when(atlantis.battle.cluster.Missions::isGlobalMissionSparta).thenReturn(true);

            world(1, fakeOurs(ours), new FakeUnit[]{}, () -> {
                HasPosition anchor = ClusterAnchor.defendAnchor();
                assertNotNull(anchor, "defend has an answer with 11 dragoons");
                assertTrue(
                    anchor instanceof AFocusPoint
                        && ((AFocusPoint) anchor).name().contains("MiddleFocusChoke"),
                    "11 dragoons ban the main choke absolutely, even a wall-in plan: " + anchor);
            });
        }
    }

    @Test
    public void elevenDragoonsVsZergLeaveMain() {
        enemyRaceInWorld = Race.Zerg;

        atlantis.map.choke.AChoke mainChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));
        atlantis.map.choke.AChoke middleChoke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(middleChoke.center()).thenReturn(APosition.create(100, 100));

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ours = new FakeUnit[12];
        ours[0] = nexus;
        for (int i = 0; i < 11; i++) {
            ours[i + 1] = fake(AUnitType.Protoss_Dragoon, 22 + (i % 4), 21 + (i / 4));
        }

        try (org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                 org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             org.mockito.MockedStatic<MiddleFocusChoke> middle =
                 org.mockito.Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            middle.when(MiddleFocusChoke::get).thenReturn(middleChoke);

            world(1, fakeOurs(ours), new FakeUnit[]{}, () -> {
                HasPosition anchor = ClusterAnchor.defendAnchor();
                assertNotNull(anchor, "defend has an answer with 11 dragoons vs Zerg");
                assertTrue(
                    anchor instanceof AFocusPoint
                        && ((AFocusPoint) anchor).name().contains("MiddleFocusChoke"),
                    "11 dragoons stage at the middle vs Zerg too, never home: " + anchor);
            });
        }
    }

    @Test
    public void tenDragoonsVsZergStillHugMain() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ours = new FakeUnit[11];
        ours[0] = nexus;
        for (int i = 0; i < 10; i++) {
            ours[i + 1] = fake(AUnitType.Protoss_Dragoon, 22 + (i % 4), 21 + (i / 4));
        }

        world(1, fakeOurs(ours), new FakeUnit[]{}, () -> {
            HasPosition anchor = ClusterAnchor.defendAnchor();

            assertTrue(
                anchor.distTo(nexus) < 1,
                "at exactly 10 dragoons the ban stays off, vs Zerg the line hugs the main: "
                    + anchor.distTo(nexus));
        });
    }
}
