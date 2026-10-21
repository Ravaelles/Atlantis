package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.strategy.GhostAdvance;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Ghost advance (owner: dragoons stood in formation against remembered-but-
 * invisible enemies - one stepped 2-3 tiles with ClusterHunt, then the
 * formation fallback bunched it back on the leader, forever).
 *
 * <p>While the evaluator says FIGHT and a remembered contact sits ahead,
 * the line creeps after it in small steps instead of standing, and exactly
 * one unit walks ahead as the scout. FLEE, the UMS opening stand-ground
 * and held chokes still own their units - this never overrules them.</p>
 */
@Tag("combat")
public class GhostAdvanceTest extends WorldStubForTests {

    @AfterEach
    public void clearUmsFlag() {
        aGame.when(AGame::isUms).thenReturn(false);
    }

    @Test
    public void lineCreepsTowardRememberedContactInsteadOfStanding() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 29);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 24);

        world(3, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            // Frame 1: visible contact - the fight snapshots the point.
            // Later frames: the enemy is gone (teleported out of sight),
            // the squad remembers where the fight was.
            if (atlantis.game.A.now() == 1) {
                for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                    BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, a);
                }
                assertNotNull(
                    atlantis.battle.FightTarget.lastContact(a),
                    "visible fight snapshots the contact point");
                zealot.position = APosition.createFromPixels(28 * 32, 45 * 32);
                return;
            }
            if (atlantis.game.A.now() != 2) return;

            APosition ghost = atlantis.battle.FightTarget.lastContact(a);
            assertNotNull(ghost, "the squad remembers the fight");

            for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, a);
            }
            for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                assertNotNull(u.targetPosition, "creeps instead of standing: " + u.lastCommand);
                assertTrue(
                    u.targetPosition.distTo(ghost) < u.position().distTo(ghost),
                    "closing toward the remembered contact, not bunching home: " + u.targetPosition);
            }
        });
    }

    @Test
    public void exactlyOneScoutWalksAheadWhileTheLineCreeps() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 29);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 24);

        world(3, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() == 1) {
                for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                    BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, a);
                }
                zealot.position = APosition.createFromPixels(28 * 32, 45 * 32);
                return;
            }
            if (atlantis.game.A.now() != 2) return;

            assertTrue(GhostAdvance.isVanguard(a), "lowest id scouts");
            assertFalse(GhostAdvance.isVanguard(b), "one scout, not two");
            assertFalse(GhostAdvance.isVanguard(c), "one scout, not three");

            for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, a);
            }
            double scoutStep = a.position().distTo(a.targetPosition);
            double lineStepB = b.position().distTo(b.targetPosition);
            double lineStepC = c.position().distTo(c.targetPosition);
            assertTrue(Math.abs(scoutStep - GhostAdvance.SCOUT_STEP_TILES) < 0.6,
                "scout walks ahead ~3 tiles: " + scoutStep);
            assertTrue(Math.abs(lineStepB - GhostAdvance.LINE_STEP_TILES) < 0.6,
                "line creeps ~1.5 tiles: " + lineStepB);
            assertTrue(Math.abs(lineStepC - GhostAdvance.LINE_STEP_TILES) < 0.6,
                "line creeps ~1.5 tiles: " + lineStepC);
        });
    }

    @Test
    public void fleeingUnitsNeverChaseGhosts() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 24);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() == 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                zealot.position = APosition.createFromPixels(28 * 32, 45 * 32);
                return;
            }
            if (atlantis.game.A.now() != 2) return;

            assertNotNull(atlantis.battle.FightTarget.lastContact(dragoon), "ghost exists");
            assertNull(GhostAdvance.advancePointFor(dragoon, FightOrFlight.Tactic.FLEE),
                "FLEE owns retreating units, never the creep");

            APosition ghost = atlantis.battle.FightTarget.lastContact(dragoon);
            double before = dragoon.position().distTo(ghost);
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);
            if (dragoon.targetPosition != null) {
                assertTrue(dragoon.targetPosition.distTo(ghost) >= before,
                    "retreat opens the ghost distance instead of closing it: "
                        + dragoon.targetPosition);
            }
        });
    }

    @Test
    public void umsOpeningKeepsItsStandGround() {
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 24);

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() == 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                zealot.position = APosition.createFromPixels(28 * 32, 45 * 32);
                return;
            }
            if (atlantis.game.A.now() != 2) return;

            assertNotNull(atlantis.battle.FightTarget.lastContact(dragoon), "ghost exists");
            assertNull(GhostAdvance.advancePointFor(dragoon, FightOrFlight.Tactic.FIGHT),
                "first 15 s on UMS: stand, do not scout out of the line");
        });
    }

    @Test
    public void heldChokeStillBeatsTheGhost() {
        // Same contested-choke shape as ChokeStandoffTest (dragoon 11 tiles
        // south of choke (28,18), zealot inside the 12-tile contest ring):
        // the hold owns the unit even with a fresh contact memory.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 24);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() == 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                zealot.position = APosition.createFromPixels(34 * 32, 14 * 32);
                return;
            }
            if (atlantis.game.A.now() != 2) return;

            assertNotNull(atlantis.battle.FightTarget.lastContact(dragoon), "ghost exists");
            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);

            APosition choke = dragoon.nearestChoke().center();
            APosition dest = dragoon.targetPosition;
            assertNotNull(dest, "holds somewhere instead of idling");
            assertTrue(dest.distTo(choke) >= 7 && dest.distTo(choke) <= 12,
                "standoff ring wins over the ghost creep: " + dest + " vs " + choke);
        });
    }
}
