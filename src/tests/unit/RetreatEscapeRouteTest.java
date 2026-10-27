package tests.unit;

import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.retreating.FleeReason;
import atlantis.combat.retreating.RetreatEscapeRoute;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The retreat owns an escape route (owner 2026-10-26, tested loss vs
 * Protoss): fleeing to the base straight through a dozen zealots donates
 * the army on its own march. When the road home runs through the enemy
 * the rally moves to the farthest choke whose own road is clear; a choke
 * inside the enemy, or one reached only through it, is never an answer.
 */
@Tag("combat")
public class RetreatEscapeRouteTest extends WorldStubForTests {

    @Test
    public void aClearRoadHomeKeepsHome() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 90, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 10, 100);
        foe.setEnemy();
        APosition home = APosition.create(23, 20);

        assertFalse(
            RetreatEscapeRoute.pathCrossesThreat(
                dragoon, home, Collections.singletonList((AUnit) foe),
                RetreatEscapeRoute.BLOCKED_CORRIDOR_TILES),
            "a foe far off the road is not a blockade: " + foe);
    }

    @Test
    public void aFoeOnTheRoadIsABlockade() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 90, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 56, 25);
        foe.setEnemy();
        APosition home = APosition.create(23, 20);

        assertTrue(
            RetreatEscapeRoute.pathCrossesThreat(
                dragoon, home, Collections.singletonList((AUnit) foe),
                RetreatEscapeRoute.BLOCKED_CORRIDOR_TILES),
            "a foe sitting on the segment blocks the march home");
    }

    @Test
    public void aBlockedRoadPicksTheFarClearChoke() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 90, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 56, 25);
        foe.setEnemy();
        APosition home = APosition.create(23, 20);
        AChoke near = new FakeChoke(APosition.create(52, 30), 8);
        AChoke far = new FakeChoke(APosition.create(110, 100), 8);
        List<AUnit> foes = Collections.singletonList((AUnit) foe);

        HasPosition escape = RetreatEscapeRoute.bestEscape(
            dragoon, home, Arrays.asList(near, far), foes, null);

        assertNotNull(escape, "a clear choke exists, the retreat names it");
        assertTrue(escape.distTo(far.center()) < 0.5,
            "the near choke sits inside the enemy, the far one owns a clear road: " + escape);
    }

    @Test
    public void everyRoadCrossingAnswersNull() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 90, 30);
        FakeUnit glued = fake(AUnitType.Protoss_Zealot, 91, 30);
        glued.setEnemy();
        APosition home = APosition.create(23, 20);
        AChoke far = new FakeChoke(APosition.create(110, 100), 8);
        List<AUnit> foes = Collections.singletonList((AUnit) glued);

        HasPosition escape = RetreatEscapeRoute.bestEscape(
            dragoon, home, Collections.singletonList(far), foes, null);

        assertNull(escape,
            "the enemy chews the unit itself: every road starts through it, step locally instead");
    }

    @Test
    public void aBlockedRoadReroutesTheFleeVerdict() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 90, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 56, 25);
        foe.setEnemy();

        AChoke mainChoke = new FakeChoke(APosition.create(60, 20), 10);
        AChoke near = new FakeChoke(APosition.create(52, 30), 8);
        AChoke far = new FakeChoke(APosition.create(110, 100), 8);

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            chokes.when(Chokes::chokes).thenReturn(Arrays.asList(near, far));

            world(5, fakeOurs(nexus, dragoon), fakeEnemies(foe), () -> {
                if (atlantis.game.A.now() != 2) return;

                RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                    dragoon, FightOrFlight.Tactic.FLEE, null, null);

                assertEquals(FleeReason.ESCAPE_RALLY, verdict.reason,
                    "the road home runs through the zealot: the verdict names the reroute");
                assertNotNull(verdict.dest, "a reroute still marches somewhere");
                assertTrue(verdict.dest.distTo(far.center()) <= 5,
                    "the march owns the far clear choke, not the base: " + verdict.dest);
            });
        }
    }

    @Test
    public void aClearRoadKeepsTheHomeVerdict() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 90, 30);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 10, 100);
        foe.setEnemy();

        AChoke mainChoke = new FakeChoke(APosition.create(60, 20), 10);
        AChoke far = new FakeChoke(APosition.create(110, 100), 8);

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            chokes.when(Chokes::chokes).thenReturn(Collections.singletonList(far));

            world(5, fakeOurs(nexus, dragoon), fakeEnemies(foe), () -> {
                if (atlantis.game.A.now() != 2) return;

                RetreatFromLostFight.Verdict verdict = RetreatFromLostFight.decide(
                    dragoon, FightOrFlight.Tactic.FLEE, null, null);

                assertEquals(FleeReason.RALLY, verdict.reason,
                    "nothing on the road home: the retreat keeps today's rally");
                assertNotNull(verdict.dest, "a clear road still marches");
            });
        }
    }
}
