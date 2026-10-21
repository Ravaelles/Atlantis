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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        world(120, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            int now = atlantis.game.A.now();
            // Frame 1: visible contact - the fight snapshots the point.
            // Later frames: the enemy is gone (teleported out of sight),
            // the squad remembers where the fight was.
            if (now == 1) {
                for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                    BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, a);
                }
                assertNotNull(
                    atlantis.battle.FightTarget.lastContact(a),
                    "visible fight snapshots the contact point");
                zealot.position = APosition.createFromPixels(28 * 32, 45 * 32);
                // The line restages away from the contact (cleared orders,
                // like T-ghost clears the stale fight): the head start must
                // hold it, the creep must walk it back - drifting on stale
                // fight orders would prove nothing.
                b.position = APosition.createFromPixels(28 * 32, 35 * 32);
                b.lastCommand = "Stop";
                b.targetPosition = null;
                c.position = APosition.createFromPixels(29 * 32, 35 * 32);
                c.lastCommand = "Stop";
                c.targetPosition = null;
                return;
            }
            for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, nexus);
            }
            if (now == 2) {
                APosition ghost = atlantis.battle.FightTarget.lastContact(a);
                assertNotNull(ghost, "the squad remembers the fight");
                assertNotNull(a.targetPosition, "scout steps out immediately");
                assertTrue(
                    a.targetPosition.distTo(ghost) < a.position().distTo(ghost),
                    "scout closes toward the remembered contact: " + a.targetPosition);
                assertTrue(b.commandHistory().toString().contains("ClusterReengage"),
                    "line holds its fight orders while the scout goes: " + b.commandHistory());
                assertFalse(b.commandHistory().toString().contains("ClusterGhost"),
                    "line waits out the head start, not creeping yet: " + b.commandHistory());
                return;
            }
            if (now != 120) return;

            APosition ghost = atlantis.battle.FightTarget.lastContact(a);
            assertNotNull(ghost, "the squad remembers the fight");
            for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                assertTrue(
                    u.position().distTo(ghost) < 3.0,
                    "converged on the remembered contact instead of standing or bunching to the base: "
                        + u.position() + " vs " + ghost);
            }
            assertTrue(a.position().distTo(ghost) <= b.position().distTo(ghost),
                "scout stays ahead of the line: " + a.position() + " vs " + b.position());
            assertTrue(b.commandHistory().toString().contains("ClusterGhost"),
                "the line crept after its head start: " + b.commandHistory());
            assertFalse(b.commandHistory().toString().contains("ClusterMove"),
                "no bunching back to the base while the ghost is warm: " + b.commandHistory());
        });
    }

    @Test
    public void exactlyOneScoutWalksAheadWhileTheLineCreeps() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 29);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 24);

        world(31, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                    BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, a);
                }
                zealot.position = APosition.createFromPixels(28 * 32, 45 * 32);
                b.position = APosition.createFromPixels(28 * 32, 35 * 32);
                b.lastCommand = "Stop";
                b.targetPosition = null;
                c.position = APosition.createFromPixels(29 * 32, 35 * 32);
                c.lastCommand = "Stop";
                c.targetPosition = null;
                return;
            }

            assertTrue(GhostAdvance.isVanguard(a), "lowest id scouts");
            assertFalse(GhostAdvance.isVanguard(b), "one scout, not two");
            assertFalse(GhostAdvance.isVanguard(c), "one scout, not three");

            for (FakeUnit u : new FakeUnit[] { a, b, c }) {
                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, nexus);
            }
            if (now == 2) {
                double scoutStep = a.position().distTo(a.targetPosition);
                assertTrue(Math.abs(scoutStep - GhostAdvance.SCOUT_STEP_TILES) < 0.6,
                    "scout walks ahead ~3 tiles: " + scoutStep);
                assertFalse(b.commandHistory().toString().contains("ClusterGhost"),
                    "line holds the head start first: " + b.commandHistory());
                return;
            }
            if (now != 31) return;

            double lineStepB = b.position().distTo(b.targetPosition);
            double lineStepC = c.position().distTo(c.targetPosition);
            assertTrue(Math.abs(lineStepB - GhostAdvance.LINE_STEP_TILES) < 0.6,
                "line creeps ~1.5 tiles once the scout is out: " + lineStepB);
            assertTrue(Math.abs(lineStepC - GhostAdvance.LINE_STEP_TILES) < 0.6,
                "line creeps ~1.5 tiles once the scout is out: " + lineStepC);
            assertTrue(a.position().distTo(atlantis.battle.FightTarget.lastContact(a))
                    <= b.position().distTo(atlantis.battle.FightTarget.lastContact(a)),
                "scout stays ahead of the line");
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
    public void umsFarGhostStillMarches() {
        // NEXT #177: with no visible enemy in reach, a ghost beyond the
        // 12-tile contact envelope is still the only address the army
        // has - UMS closes the distance instead of standing. Melee
        // keeps the give-up (cross-map marches there are reinforcement
        // calls, not scouting).
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 70, 29);
        zealot.setEnemy();
        zealot.isVisibleUnitOnMap = false;

        // Past the 15 s UMS opening stand-ground (pinned separately):
        // the march owns every frame after it.
        world(400, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 365) return;

            assertNotNull(GhostAdvance.ghostPointFor(dragoon),
                "the fogged zealot is remembered even though nobody sees it");
            assertNotNull(GhostAdvance.advancePointFor(dragoon, FightOrFlight.Tactic.FIGHT),
                "UMS marches to the far ghost instead of standing");
            assertTrue(GhostAdvance.move(dragoon, FightOrFlight.Tactic.FIGHT),
                "the vanguard owns the march, the formation does not hold it");
            assertNotNull(dragoon.targetPosition, "a step was ordered");
        });
    }

    @Test
    public void meleeFarGhostStillGivesUp() {
        // The other half of the UMS rule above: melee keeps the
        // 12-tile give-up, a far ghost belongs to reinforcements.
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 70, 29);
        zealot.setEnemy();
        zealot.isVisibleUnitOnMap = false;

        world(3, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertNotNull(GhostAdvance.ghostPointFor(dragoon),
                "the ghost is found - only the LOST gate may refuse it");
            assertNull(GhostAdvance.advancePointFor(dragoon, FightOrFlight.Tactic.FIGHT),
                "melee gives up far ghosts as before");
        });
    }

    @Test
    public void umsExploresUnseenMapWhenNobodyRemembersAnything() {
        // NEXT #177, last resort: no contact, no fogged enemies, but the
        // map is not fully explored - march at the nearest unexplored
        // region instead of standing. Melee keeps the old null.
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);

        atlantis.map.region.ARegion region = org.mockito.Mockito.mock(atlantis.map.region.ARegion.class);
        org.mockito.Mockito.when(region.center()).thenReturn(APosition.create(50, 30));
        try (org.mockito.MockedStatic<atlantis.map.region.Regions> regions =
                org.mockito.Mockito.mockStatic(atlantis.map.region.Regions.class)) {
            regions.when(() -> atlantis.map.region.Regions.getNearestUnexploredRegion(
                org.mockito.ArgumentMatchers.any())).thenReturn(region);

            world(3, fakeOurs(dragoon), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertEquals(APosition.create(50, 30), GhostAdvance.ghostPointFor(dragoon),
                    "unseen map means explore, not stand");
            });
        }
    }

    @Test
    public void meleeDoesNotChaseUnexploredMap() {
        // The UMS-only gate, pinned: melee keeps formation when blind.
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);

        atlantis.map.region.ARegion region = org.mockito.Mockito.mock(atlantis.map.region.ARegion.class);
        org.mockito.Mockito.when(region.center()).thenReturn(APosition.create(50, 30));
        try (org.mockito.MockedStatic<atlantis.map.region.Regions> regions =
                org.mockito.Mockito.mockStatic(atlantis.map.region.Regions.class)) {
            regions.when(() -> atlantis.map.region.Regions.getNearestUnexploredRegion(
                org.mockito.ArgumentMatchers.any())).thenReturn(region);

            world(3, fakeOurs(dragoon), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertNull(GhostAdvance.ghostPointFor(dragoon),
                    "melee holds formation instead of exploring");
            });
        }
    }

    @Test
    public void foggedAddressBeatsUnexploredMap() {
        // Priority order: contact, then fogged memory, then exploration.
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 29, 29);
        zealot.setEnemy();
        zealot.isVisibleUnitOnMap = false;

        atlantis.map.region.ARegion region = org.mockito.Mockito.mock(atlantis.map.region.ARegion.class);
        org.mockito.Mockito.when(region.center()).thenReturn(APosition.create(50, 30));
        try (org.mockito.MockedStatic<atlantis.map.region.Regions> regions =
                org.mockito.Mockito.mockStatic(atlantis.map.region.Regions.class)) {
            regions.when(() -> atlantis.map.region.Regions.getNearestUnexploredRegion(
                org.mockito.ArgumentMatchers.any())).thenReturn(region);

            world(3, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertEquals(zealot.position(), GhostAdvance.ghostPointFor(dragoon),
                    "a remembered enemy beats exploring");
            });
        }
    }

    @Test
    public void freshPersonalContactBeatsFoggedMemory() {
        // Priority order, fresh side: this unit's own contact from last
        // frame beats the fogged address of the same enemy elsewhere.
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 29, 29);

        world(3, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                return;
            }
            if (now != 2) return;

            APosition contact = atlantis.battle.FightTarget.lastContact(dragoon);
            assertNotNull(contact, "contact snapshot exists");
            zealot.isVisibleUnitOnMap = false;
            zealot.position = APosition.createFromPixels(70 * 32, 29 * 32);

            assertEquals(contact, GhostAdvance.ghostPointFor(dragoon),
                "fresh personal contact wins over fogged memory: " + contact);
        });
    }

    @Test
    public void stalePersonalContactFallsThroughToFogged() {
        // NEXT #177, staleness: a contact older than STALE_CONTACT_FRAMES
        // is history - the creep falls through to fogged memory instead of
        // ping-ponging one stale tile all game. Same setup as above, only
        // the clock moves past the brake.
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 29, 29);

        world(300, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                return;
            }
            if (now == 2) {
                zealot.isVisibleUnitOnMap = false;
                zealot.position = APosition.createFromPixels(70 * 32, 29 * 32);
                return;
            }
            if (now != 300) return;

            APosition stale = atlantis.battle.FightTarget.lastContact(dragoon);
            assertNotNull(stale, "the snapshot itself never expires, only its use");
            assertEquals(zealot.position(), GhostAdvance.ghostPointFor(dragoon),
                "stale contact drops, fogged memory owns the march: " + stale);
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
