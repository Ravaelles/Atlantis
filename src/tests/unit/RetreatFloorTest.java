package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.focus.MiddleFocusChoke;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.combat.retreating.RetreatFloor;
import atlantis.combat.strategy.ChokeStandoff;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
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
 * Retreat floor (owner order 2026-10-25: "if we retreat, never further
 * than MiddleFocusChoke"): a losing fight stops at the middle-choke line
 * instead of running all the way to the main. The line is the same
 * RADIUS + 6 standoff the formation rule answers, so a retreat ends on
 * the ground the army would hold anyway.
 *
 * <p>The floor only stops a retreat that would CROSS it: an army already
 * inside (defending its own base) keeps today's rally, because yanking a
 * small line out through the base is the exact opposite of the order.
 * A breach never reaches this path at all - {@code ClusterTactic}
 * forces FIGHT while raiders are in the base.</p>
 */
@Tag("combat")
public class RetreatFloorTest extends WorldStubForTests {

    private AChoke mainChoke;
    private AChoke middle;
    private FakeUnit nexus;

    private void stubChokes() {
        mainChoke = new FakeChoke(APosition.create(60, 20), 10);
        middle = new FakeChoke(APosition.create(100, 100), 18);
    }

    private HasPosition floorLine() {
        return middle.center().translateTilesTowards(
            ChokeStandoff.standoffTiles(middle), nexus.position());
    }

    @Test
    public void deepRallyStopsAtTheMiddleLine() {
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 120 + i, 130);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    // What a losing fight would rally to today: the main-choke standoff.
                    HasPosition deep = mainChoke.center().translateTilesTowards(
                        ChokeStandoff.standoffTiles(mainChoke), nexus.position());
                    HasPosition clamped = RetreatFloor.clamp(deep);

                    assertNotNull(clamped, "the retreat names somewhere to stand");
                    assertTrue(
                        Math.abs(clamped.distTo(middle.center())
                            - ChokeStandoff.standoffTiles(middle)) < 1,
                        "the retreat stops on the middle-choke standing line (RADIUS + 6): "
                            + clamped.distTo(middle.center()) + " tiles off its center");
                    assertTrue(clamped.distTo(nexus) > deep.distTo(nexus),
                        "stopped short of the deep rally: " + clamped.distTo(nexus)
                            + " vs " + deep.distTo(nexus));
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void rallyAlreadyOutsideTheLineStands() {
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 120 + i, 130);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    HasPosition out = APosition.create(140, 140);
                    HasPosition kept = RetreatFloor.clamp(out);

                    assertNotNull(kept, "the clamp answers a point");
                    assertTrue(kept.distTo(out) < 0.1,
                        "a rally already in front of the line is never pulled back: " + kept);
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void lineInsideKeepsItsOwnRally() {
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 24 + i, 26);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    HasPosition deep = mainChoke.center().translateTilesTowards(
                        ChokeStandoff.standoffTiles(mainChoke), nexus.position());
                    HasPosition kept = RetreatFloor.clamp(deep);

                    assertNotNull(kept, "the clamp answers a point");
                    assertTrue(kept.distTo(deep) < 0.1,
                        "an army already inside its own base keeps today's rally: " + kept);
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void retreatNeverGoesBehindTheMiddleLine() {
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 120 + i, 130);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    boolean fled = RetreatFromLostFight.retreat(
                        army[0], FightOrFlight.Tactic.FLEE, null, army[0]);
                    assertTrue(fled, "a FLEE still issues a retreat");

                    HasPosition dest = army[0].targetPosition();
                    assertNotNull(dest, "the retreat names a destination");
                    HasPosition floor = floorLine();
                    assertTrue(dest.distTo(nexus) >= floor.distTo(nexus) - 0.5,
                        "the retreat stops at the middle line: dest " + dest
                            + " is behind floor " + floor);
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    // =========================================================

    private void joinAlpha(FakeUnit[] army) {
        for (FakeUnit unit : army) Alpha.get().addUnit(unit);
    }

    private void leaveAlpha(FakeUnit[] army) {
        for (FakeUnit unit : army) Alpha.get().removeUnit(unit);
    }

    private static FakeUnit[] concat(FakeUnit first, FakeUnit[] rest) {
        FakeUnit[] all = new FakeUnit[rest.length + 1];
        all[0] = first;
        System.arraycopy(rest, 0, all, 1, rest.length);

        return all;
    }
}
