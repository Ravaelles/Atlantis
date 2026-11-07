package tests.unit;

import atlantis.combat.focus.MiddleFocusChoke;
import atlantis.combat.focus.ProtossDefendFocusPoint;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Absolute main-choke ban by base count (owner order 2026-11-07): past
 * two bases the line never stages home, so a powered slot of excuses
 * (sparta, standoff, hold) must not park the army at the main choke.
 * Companion to the dragoon-count ban: same rule, different tripwire.
 *
 * <p>Pinned at the predicate and at the focus answer. The focus tests
 * mock the chokes (the stub world has no map behind it) and count
 * real fake bases.</p>
 */
@Tag("combat")
public class MainChokeBanTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ProtossDefendFocusPoint.clearForTests();
    }

    @Test
    public void threeBasesBanMainChoke() {
        FakeUnit a = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit c = fake(AUnitType.Protoss_Nexus, 60, 20);
        world(3, fakeOurs(a, b, c), fakeEnemies(), () -> {
            assertTrue(ProtossDefendFocusPoint.mainChokeBannedByBases(),
                "three bases: the main choke is banned, the army lives forward");
            assertTrue(ProtossDefendFocusPoint.mainChokeBanned(),
                "the combined ban answers the tripwire");
        });
    }

    @Test
    public void twoBasesDoNotBanMainChoke() {
        FakeUnit a = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(3, fakeOurs(a, b, probe), fakeEnemies(), () -> {
            assertFalse(ProtossDefendFocusPoint.mainChokeBannedByBases(),
                "two bases: the ban stays off, small armies still hold home");
        });
    }

    @Test
    public void focusLeavesMainChokeWithThreeBases() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));

        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit n2 = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit n3 = fake(AUnitType.Protoss_Nexus, 60, 40);
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 21, 21);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 22, 22);

        try (MockedStatic<atlantis.map.choke.Chokes> chokes =
                 Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             MockedStatic<MiddleFocusChoke> middle =
                 Mockito.mockStatic(MiddleFocusChoke.class);
             MockedStatic<atlantis.combat.cluster.ClusterRally> rally =
                 Mockito.mockStatic(atlantis.combat.cluster.ClusterRally.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            middle.when(MiddleFocusChoke::get).thenReturn(null);
            rally.when(atlantis.combat.cluster.ClusterRally::chokeStandoffPoint)
                .thenReturn(APosition.create(55, 20));

            world(3, fakeOurs(n1, n2, n3, z1, z2), fakeEnemies(), () -> {
                atlantis.battle.advance.focus.AFocusPoint focus =
                    ProtossDefendFocusPoint.focusPoint();

                assertTrue(focus == null || !focus.name().contains("MainChoke"),
                    "banned: the focus never stages the main choke, even unpowered-middle: "
                        + (focus == null ? "null" : focus.name()));
            });
        }
    }

    @Test
    public void focusHoldsMainChokeWithTwoBases() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));

        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit n2 = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 21, 21);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 22, 22);

        try (MockedStatic<atlantis.map.choke.Chokes> chokes =
                 Mockito.mockStatic(atlantis.map.choke.Chokes.class);
             MockedStatic<MiddleFocusChoke> middle =
                 Mockito.mockStatic(MiddleFocusChoke.class);
             MockedStatic<atlantis.combat.cluster.ClusterRally> rally =
                 Mockito.mockStatic(atlantis.combat.cluster.ClusterRally.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(mainChoke);
            middle.when(MiddleFocusChoke::get).thenReturn(null);
            rally.when(atlantis.combat.cluster.ClusterRally::chokeStandoffPoint)
                .thenReturn(APosition.create(55, 20));

            world(3, fakeOurs(n1, n2, z1, z2), fakeEnemies(), () -> {
                atlantis.battle.advance.focus.AFocusPoint focus =
                    ProtossDefendFocusPoint.focusPoint();

                assertNotNull(focus, "unbanned: the focus still answers");
                assertTrue(focus.name().contains("MainChoke"),
                    "two bases, small army: the main standoff stands: " + focus.name());
            });
        }
    }

    @Test
    public void noContactHoldSkippedUnderBan() {
        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit n2 = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit n3 = fake(AUnitType.Protoss_Nexus, 60, 40);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);

        world(3, fakeOurs(n1, n2, n3, probe), fakeEnemies(), () -> {
            APosition anchor = n1.position();

            assertNull(atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor),
                "banned: no halfway-main-choke hold - the line forms up elsewhere");
        });
    }

    @Test
    public void noContactHoldStandsWithoutBan() {
        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);

        world(3, fakeOurs(n1, probe), fakeEnemies(), () -> {
            APosition anchor = n1.position();

            assertNotNull(atlantis.combat.cluster.ClusterRally.noContactHoldPoint(anchor),
                "unbanned: the halfway hold still owns the blind wait");
        });
    }

    @Test
    public void baitSkippedAtBannedMainChoke() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.isMainChoke()).thenReturn(true);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));

        // Bait first (lowest id among combat units), the line far from the
        // choke mouth, one enemy contesting it.
        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit n2 = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit n3 = fake(AUnitType.Protoss_Nexus, 60, 40);
        FakeUnit bait = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 12, 12);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 14, 14);
        FakeUnit z3 = fake(AUnitType.Protoss_Zealot, 16, 16);
        FakeUnit z4 = fake(AUnitType.Protoss_Zealot, 18, 18);
        FakeUnit z5 = fake(AUnitType.Protoss_Zealot, 22, 22);
        FakeUnit z6 = fake(AUnitType.Protoss_Zealot, 24, 24);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 60, 20);
        foe.setEnemy();

        try (MockedStatic<atlantis.combat.strategy.BattleSetup> setup =
                 Mockito.mockStatic(atlantis.combat.strategy.BattleSetup.class,
                     Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))) {
            setup.when(() -> atlantis.combat.strategy.BattleSetup.chokeNear(Mockito.any(AUnit.class)))
                .thenReturn(mainChoke);

            world(3, fakeOurs(n1, n2, n3, bait, z1, z2, z3, z4, z5, z6),
                fakeEnemies(foe), () -> {
                    assertFalse(atlantis.combat.strategy.SquadScout.applies(bait, null),
                        "banned: no bait games at the main choke");
                });
        }
    }

    @Test
    public void baitStandsAtUnbannedMainChoke() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.isMainChoke()).thenReturn(true);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));

        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit bait = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 12, 12);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 14, 14);
        FakeUnit z3 = fake(AUnitType.Protoss_Zealot, 16, 16);
        FakeUnit z4 = fake(AUnitType.Protoss_Zealot, 18, 18);
        FakeUnit z5 = fake(AUnitType.Protoss_Zealot, 22, 22);
        FakeUnit z6 = fake(AUnitType.Protoss_Zealot, 24, 24);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 60, 20);
        foe.setEnemy();

        try (MockedStatic<atlantis.combat.strategy.BattleSetup> setup =
                 Mockito.mockStatic(atlantis.combat.strategy.BattleSetup.class,
                     Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))) {
            setup.when(() -> atlantis.combat.strategy.BattleSetup.chokeNear(Mockito.any(AUnit.class)))
                .thenReturn(mainChoke);

            world(3, fakeOurs(n1, bait, z1, z2, z3, z4, z5, z6),
                fakeEnemies(foe), () -> {
                    assertTrue(atlantis.combat.strategy.SquadScout.applies(bait, null),
                        "unbanned: the bait still pulls at the held choke");
                });
        }
    }

    @Test
    public void standoffSkippedAtBannedMainChoke() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.isMainChoke()).thenReturn(true);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));

        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit n2 = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit n3 = fake(AUnitType.Protoss_Nexus, 60, 40);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 55, 20);

        try (MockedStatic<atlantis.combat.strategy.BattleSetup> setup =
                 Mockito.mockStatic(atlantis.combat.strategy.BattleSetup.class,
                     Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))) {
            setup.when(() -> atlantis.combat.strategy.BattleSetup.chokeNear(Mockito.any(AUnit.class)))
                .thenReturn(mainChoke);

            world(3, fakeOurs(n1, n2, n3, dragoon), fakeEnemies(), () -> {
                assertNull(atlantis.combat.strategy.ChokeStandoff.holdPointFor(dragoon),
                    "banned: no standoff on the main choke");
            });
        }
    }

    @Test
    public void standoffHoldsAtUnbannedMainChoke() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.isMainChoke()).thenReturn(true);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));

        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 55, 20);

        try (MockedStatic<atlantis.combat.strategy.BattleSetup> setup =
                 Mockito.mockStatic(atlantis.combat.strategy.BattleSetup.class,
                     Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))) {
            setup.when(() -> atlantis.combat.strategy.BattleSetup.chokeNear(Mockito.any(AUnit.class)))
                .thenReturn(mainChoke);

            world(3, fakeOurs(n1, dragoon), fakeEnemies(), () -> {
                assertNotNull(atlantis.combat.strategy.ChokeStandoff.holdPointFor(dragoon),
                    "unbanned: the standoff still owns the choke");
            });
        }
    }

    @Test
    public void marchDressSkippedAtBannedMainChoke() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.isMainChoke()).thenReturn(true);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));
        Mockito.when(mainChoke.position()).thenReturn(APosition.create(60, 20));

        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit n2 = fake(AUnitType.Protoss_Nexus, 40, 20);
        FakeUnit n3 = fake(AUnitType.Protoss_Nexus, 60, 40);
        FakeUnit vanguard = fake(AUnitType.Protoss_Zealot, 25, 20);
        FakeUnit m1 = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit m2 = fake(AUnitType.Protoss_Zealot, 35, 20);
        FakeUnit asker = fake(AUnitType.Protoss_Zealot, 55, 20);

        try (MockedStatic<atlantis.map.path.PathToEnemyBase> path =
                 Mockito.mockStatic(atlantis.map.path.PathToEnemyBase.class);
             MockedStatic<atlantis.map.choke.Chokes> chokes =
                 Mockito.mockStatic(atlantis.map.choke.Chokes.class)) {
            path.when(atlantis.map.path.PathToEnemyBase::chokesLeadingToEnemyBase)
                .thenReturn(new java.util.ArrayList<>());
            chokes.when(() -> atlantis.map.choke.Chokes.nearestChoke(
                    Mockito.any(HasPosition.class)))
                .thenReturn(mainChoke);

            world(3, fakeOurs(n1, n2, n3, vanguard, m1, m2, asker), fakeEnemies(), () -> {
                assertNull(
                    atlantis.combat.formation.MarchCohesion.holdPointFor(
                        asker, APosition.create(60, 20)),
                    "banned: no dress-hold at the main choke fallback");
            });
        }
    }

    @Test
    public void marchDressHoldsAtUnbannedMainChoke() {
        AChoke mainChoke = Mockito.mock(AChoke.class);
        Mockito.when(mainChoke.isMainChoke()).thenReturn(true);
        Mockito.when(mainChoke.center()).thenReturn(APosition.create(60, 20));
        Mockito.when(mainChoke.position()).thenReturn(APosition.create(60, 20));

        FakeUnit n1 = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit vanguard = fake(AUnitType.Protoss_Zealot, 25, 20);
        FakeUnit m1 = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit m2 = fake(AUnitType.Protoss_Zealot, 35, 20);
        FakeUnit asker = fake(AUnitType.Protoss_Zealot, 55, 20);

        try (MockedStatic<atlantis.map.path.PathToEnemyBase> path =
                 Mockito.mockStatic(atlantis.map.path.PathToEnemyBase.class);
             MockedStatic<atlantis.map.choke.Chokes> chokes =
                 Mockito.mockStatic(atlantis.map.choke.Chokes.class)) {
            path.when(atlantis.map.path.PathToEnemyBase::chokesLeadingToEnemyBase)
                .thenReturn(new java.util.ArrayList<>());
            chokes.when(() -> atlantis.map.choke.Chokes.nearestChoke(
                    Mockito.any(HasPosition.class)))
                .thenReturn(mainChoke);

            world(3, fakeOurs(n1, vanguard, m1, m2, asker), fakeEnemies(), () -> {
                assertNotNull(
                    atlantis.combat.formation.MarchCohesion.holdPointFor(
                        asker, APosition.create(60, 20)),
                    "unbanned: the dress still holds the front while the rear catches up");
            });
        }
    }
}
