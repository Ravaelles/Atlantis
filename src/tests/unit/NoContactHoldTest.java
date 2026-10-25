package tests.unit;

import atlantis.combat.ArmyContact;
import atlantis.combat.Regroup;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * No-contact hold (owner 2026-10-26, live game vs Protoss on 7th_rav):
 * with no enemy to engage and the line defending home, the march holds
 * halfway between the main base and the main choke instead of walking to
 * a far anchor - the far march uncovered the base while raiders ran by.
 * An attack anchor still marches out, and FLEE keeps its own rally.
 */
@Tag("combat")
public class NoContactHoldTest extends WorldStubForTests {

    @Test
    public void blindLineHoldsHalfwayBetweenMainAndChoke() {
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                ArmyContact.clearForTests();
                APosition anchor = APosition.create(26, 20);
                Regroup.march(a, FightOrFlight.Tactic.FIGHT, anchor, a, BattleStrategy.OPEN_FIELD);

                String journal = a.commandHistory().toString();
                assertTrue(journal.contains("ClusterStandoff"),
                    "a blind home line holds instead of marching out: " + journal);

                APosition expected = APosition.create(20, 20).translatePercentTowards(50, choke.center());
                APosition dest = a.targetPosition();
                assertNotNull(dest, "the hold names a destination");
                assertTrue(dest.distTo(expected) < 2,
                    "the hold stands halfway main->choke: dest " + dest + " vs " + expected);
            });
        }
    }

    @Test
    public void attackAnchorStillMarchesOut() {
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                ArmyContact.clearForTests();
                APosition farAnchor = APosition.create(90, 90);
                Regroup.march(a, FightOrFlight.Tactic.FIGHT, farAnchor, a, BattleStrategy.OPEN_FIELD);

                String journal = a.commandHistory().toString();
                assertTrue(!journal.contains("ClusterStandoff"),
                    "an attack anchor must march out, never hold home: " + journal);
            });
        }
    }

    @Test
    public void fleeKeepsItsOwnRally() {
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 22);

        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(5, fakeOurs(nexus, a), fakeEnemies(), () -> {
                if (A.now() != 2) return;

                ArmyContact.clearForTests();
                APosition anchor = APosition.create(26, 20);
                Regroup.march(a, FightOrFlight.Tactic.FLEE, anchor, a, BattleStrategy.OPEN_FIELD);

                String journal = a.commandHistory().toString();
                assertTrue(!journal.contains("ClusterStandoff"),
                    "FLEE owns the rally, the hold must not steal it: " + journal);
            });
        }
    }
}
