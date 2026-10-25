package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.focus.MiddleFocusChoke;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.combat.retreating.RetreatFloor;
import atlantis.combat.retreating.RetreatLadder;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Retreat floor + ladder (owner orders 2026-10-25 / 2026-10-26): a
 * losing fight stops on the choke line it was holding, never runs to
 * the main. Two rungs, picked from the ground the army last fought on
 * ({@link RetreatLadder}): beyond the middle choke it goes back to the
 * middle choke line, losing even there drops it to the natural (main)
 * choke line, and that is the floor - the Nexus itself is never a
 * retreat destination. Both lines are the RADIUS + 6 standoff the
 * formation rule answers ({@link ChokeStandoff}), so a retreat ends on
 * ground the army would hold anyway.
 *
 * <p>Two guards keep the floor from drafting orders of its own: an army
 * still inside the natural choke keeps today's rally (base defense is
 * its own rule - a breach forces FIGHT), and a rung standing AHEAD of
 * the army is never marched onto - it holds where it is instead of
 * advancing while it flees. A breach never reaches this path at all -
 * {@code ClusterTactic} forces FIGHT while raiders are in the base.</p>
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

    /** The middle-choke standing line: RADIUS + 6 off its center, our side. */
    private HasPosition floorLine() {
        return middle.center().translateTilesTowards(
            ChokeStandoff.standoffTiles(middle), nexus.position());
    }

    /** The natural-choke standing line - the last rung, never the main. */
    private HasPosition naturalLine() {
        return mainChoke.center().translateTilesTowards(
            ChokeStandoff.standoffTiles(mainChoke), nexus.position());
    }

    /** What the fight funnel records (ArmyContact.noteFight) on every engaged frame. */
    private void recordFightAt(HasPosition ground) {
        RetreatLadder.noteFightGround(ground, Alpha.get().size());
    }

    @Test
    public void deepRallyStopsAtTheMiddleLine() {
        RetreatLadder.reset();
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
                    // The army fought its way BEYOND the middle choke, then
                    // lost: that ground is what the ladder remembers.
                    recordFightAt(Alpha.get().center());

                    // What a losing fight would rally to today: the main-choke standoff.
                    HasPosition deep = naturalLine();
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
        RetreatLadder.reset();
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
    public void insideTheNaturalChokeKeepsItsOwnRally() {
        RetreatLadder.reset();
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
                    recordFightAt(Alpha.get().center());
                    HasPosition deep = naturalLine();
                    HasPosition kept = RetreatFloor.clamp(deep);

                    assertNotNull(kept, "the clamp answers a point");
                    assertTrue(kept.distTo(deep) < 0.1,
                        "an army inside its own choke line keeps today's rally: " + kept);
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void armyBetweenTheLinesFallsBackToTheNaturalChoke() {
        RetreatLadder.reset();
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 60 + i, 60);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    recordFightAt(Alpha.get().center());

                    // Today's rally for a routed unit: all the way to the Nexus.
                    HasPosition clamped = RetreatFloor.clamp(nexus.position());

                    assertNotNull(clamped, "the retreat names somewhere to stand");
                    assertTrue(
                        Math.abs(clamped.distTo(mainChoke.center())
                            - ChokeStandoff.standoffTiles(mainChoke)) < 1,
                        "the retreat stops on the natural-choke line (RADIUS + 6): "
                            + clamped.distTo(mainChoke.center()) + " tiles off its center");
                    assertTrue(clamped.distTo(nexus) > 10,
                        "never the main itself: " + clamped.distTo(nexus) + " tiles off the Nexus");
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void losingOnTheMiddleLineDropsToTheNaturalChoke() {
        RetreatLadder.reset();
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 60 + i, 60);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    // "Even there" = the fight happened ON the middle line.
                    recordFightAt(floorLine());

                    HasPosition clamped = RetreatFloor.clamp(nexus.position());

                    assertNotNull(clamped, "the retreat names somewhere to stand");
                    assertTrue(
                        Math.abs(clamped.distTo(mainChoke.center())
                            - ChokeStandoff.standoffTiles(mainChoke)) < 1,
                        "losing on the middle line drops to the natural choke: "
                            + clamped.distTo(mainChoke.center()) + " tiles off its center");
                    assertTrue(clamped.distTo(floorLine()) > 10,
                        "and never back down to the middle line: " + clamped);
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void aRungAheadOfTheArmyHoldsInsteadOfMarching() {
        RetreatLadder.reset();
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 60 + i, 60);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    // Fought beyond the middle, then pushed back past it: the
                    // rung (the middle line) now sits AHEAD of the army.
                    recordFightAt(APosition.create(140, 140));

                    HasPosition clamped = RetreatFloor.clamp(nexus.position());

                    assertNotNull(clamped, "the retreat names somewhere to stand");
                    assertTrue(clamped.distTo(Alpha.get().center()) < 1,
                        "a rung ahead is held, not marched onto: " + clamped
                            + " vs cluster center " + Alpha.get().center());
                    assertTrue(clamped.distTo(nexus) > 20,
                        "and it stays off the main: " + clamped.distTo(nexus));
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void aPokeSmallerThanASquadNeverSetsTheRung() {
        RetreatLadder.reset();
        aGame.when(AGame::isUms).thenReturn(false);
        nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] army = new FakeUnit[6];
        for (int i = 0; i < 6; i++) army[i] = fake(AUnitType.Protoss_Dragoon, 60 + i, 60);
        stubChokes();

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<MiddleFocusChoke> middleFocus = Mockito.mockStatic(MiddleFocusChoke.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            middleFocus.when(MiddleFocusChoke::get).thenReturn(middle);

            world(5, fakeOurs(concat(nexus, army)), fakeEnemies(), () -> {
                if (A.now() != 2) return;
                joinAlpha(army);
                try {
                    HasPosition far = APosition.create(140, 140);

                    RetreatLadder.noteFightGround(far, RetreatLadder.MIN_FIGHT_UNITS - 1);
                    assertEquals(
                        RetreatLadder.NO_FIGHT_RECORDED, RetreatLadder.lastFightDepth(), 0.01,
                        "a lone poke never sets the ladder");

                    RetreatLadder.noteFightGround(far, RetreatLadder.MIN_FIGHT_UNITS);
                    assertEquals(
                        far.distTo(nexus.position()), RetreatLadder.lastFightDepth(), 0.01,
                        "a squad fight records its depth from the main");
                }
                finally {
                    leaveAlpha(army);
                }
            });
        }
    }

    @Test
    public void retreatNeverGoesBehindTheMiddleLine() {
        RetreatLadder.reset();
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
                    recordFightAt(Alpha.get().center());

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
