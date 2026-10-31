package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * What fog-of-war buys StarEngine: a hidden-unit scenario that needs no
 * live game (the #230 follow-up the spec asks for - SimFog proves useful
 * by enabling exactly this).
 *
 * <p>A burrowed Lurker sits off a Dragoon's shoulder. The dragoon follows
 * the only honest rule under fog: it engages the nearest VISIBLE enemy
 * and holds otherwise. Far away the lurker is fog memory, not a target -
 * no phantom retaliation at something the army cannot see. Teleported
 * into sight range, the next 15-frame evaluation picks it up and the
 * dragoon kills it.</p>
 *
 * <p>One-sided on purpose (SimFog's documented scope): only OUR
 * perception is modeled, so the lurker is passive. Live JBWAPI concerns
 * (detectors, cloak rules) stay live-twinned, never guessed here.</p>
 */
@Tag("combat")
@Tag("full-tier")
public class LurkerAmbushSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit dragoon(double tx, double ty) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, tx, ty);
        dragoon.hp = 180;

        return dragoon;
    }

    private static FakeUnit lurker(double tx, double ty) {
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, tx, ty);
        lurker.setEnemy();
        lurker.setBurrowed(true);
        lurker.hp = 60;

        return lurker;
    }

    /**
     * Fog-gated targeting: nearest visible live enemy in reach gets the
     * attack, otherwise the unit holds. Visibility reads SimFog, never
     * raw distance - raw distance is what the sim knows, fog is what the
     * army knows.
     */
    private static void fogAware(SimWorld world, FakeUnit dragoon, FakeUnit lurker) {
        if (lurker.hp > 0 && world.fog().isVisible(lurker.id())
            && dragoon.distTo(lurker) <= SimWorld.reachTiles(dragoon, lurker)) {
            dragoon.attackUnit(lurker);

            return;
        }

        dragoon.lastCommand = "Hold";
    }

    private static void tickTo(SimWorld world, FakeUnit dragoon, FakeUnit lurker, int target) {
        while (world.frame() < target) {
            world.tick(() -> fogAware(world, dragoon, lurker));
        }
    }

    @Test
    public void unseenLurkerDrawsNoFire() {
        FakeUnit dragoon = dragoon(10, 10);
        FakeUnit lurker = lurker(20, 10);
        SimWorld world = new SimWorld(
            fakeOurs(dragoon), fakeEnemies(lurker), new SimArena(40 * TILE, 40 * TILE));
        try {
            tickTo(world, dragoon, lurker, 60);

            assertEquals(60, lurker.hp, "10 tiles out is fog memory, not a target");
            assertEquals("Hold", dragoon.lastCommand, "nothing visible means hold");
        }
        finally {
            world.close();
        }
    }

    @Test
    public void seenLurkerDies() {
        FakeUnit dragoon = dragoon(10, 10);
        FakeUnit lurker = lurker(20, 10);
        SimWorld world = new SimWorld(
            fakeOurs(dragoon), fakeEnemies(lurker), new SimArena(40 * TILE, 40 * TILE));
        try {
            tickTo(world, dragoon, lurker, 60);
            assertEquals(60, lurker.hp, "setup: still unseen, still whole");

            // 3 tiles: inside dragoon reach and inside sight.
            world.teleport(lurker, APosition.create(13, 10));

            int deadline = world.frame() + 400;
            while (lurker.hp > 0 && world.frame() < deadline) {
                world.tick(() -> fogAware(world, dragoon, lurker));
            }

            assertTrue(lurker.hp <= 0, "once seen, the dragoon kills it");
        }
        finally {
            world.close();
        }
    }

    @Test
    public void sightingWaitsForTheEvaluation() {
        FakeUnit dragoon = dragoon(10, 10);
        FakeUnit lurker = lurker(20, 10);
        SimWorld world = new SimWorld(
            fakeOurs(dragoon), fakeEnemies(lurker), new SimArena(40 * TILE, 40 * TILE));
        try {
            tickTo(world, dragoon, lurker, 16);
            world.teleport(lurker, APosition.create(13, 10));

            // Next evaluation is frame 30: at 29 the lurker is in range
            // but still fog - the cadence gates sighting, not distance.
            tickTo(world, dragoon, lurker, 29);
            assertEquals(60, lurker.hp, "in range but unevaluated: still fog");
            assertEquals("Hold", dragoon.lastCommand, "in range but unevaluated: still hold");

            tickTo(world, dragoon, lurker, 30);
            assertTrue(world.fog().isVisible(lurker.id()),
                "the frame-30 evaluation sees it");
            assertEquals("Hold", dragoon.lastCommand,
                "decisions ran before the update, so engagement lands next frame");

            tickTo(world, dragoon, lurker, 31);
            assertEquals("AttackUnit", dragoon.lastCommand,
                "the frame-31 decisions engage the now-visible lurker");
        }
        finally {
            world.close();
        }
    }
}
