package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Fog-of-war memory in StarEngine (owner spec 2026-10-28): visibility is
 * re-evaluated every 15 frames, and a known enemy unit is visible iff any
 * of our units is within 8 tiles of it - otherwise it stays in fog with
 * its last-seen position. No cones, no occlusion, no detector types.
 */
@Tag("combat")
@Tag("full-tier")
public class SimFogTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit eye(double tx, double ty) {
        return fake(AUnitType.Protoss_Probe, tx, ty);
    }

    private static FakeUnit lurker(double tx, double ty) {
        FakeUnit lurker = fake(AUnitType.Zerg_Lurker, tx, ty);
        lurker.setEnemy();

        return lurker;
    }

    private static void tickTo(SimWorld world, int target) {
        while (world.frame() < target) {
            world.tick(() -> {
            });
        }
    }

    @Test
    public void visibilityReevaluatesEvery15Frames() {
        FakeUnit eye = eye(10, 10);
        FakeUnit lurker = lurker(13, 14);
        SimWorld world = new SimWorld(
            fakeOurs(eye), fakeEnemies(lurker), new SimArena(40 * TILE, 40 * TILE));
        try {
            tickTo(world, 14);
            assertFalse(world.fog().isVisible(lurker.id()),
                "no evaluation before frame 15 - in range but unseen");

            tickTo(world, 15);
            assertTrue(world.fog().isVisible(lurker.id()),
                "first evaluation sees the lurker 5 tiles out");
        }
        finally {
            world.close();
        }
    }

    @Test
    public void sightBoundaryIsEightTiles() {
        FakeUnit eye = eye(10, 10);
        FakeUnit lurker = lurker(15, 16);
        SimWorld world = new SimWorld(
            fakeOurs(eye), fakeEnemies(lurker), new SimArena(40 * TILE, 40 * TILE));
        try {
            // sqrt(25 + 36) = ~7.81 tiles: visible.
            tickTo(world, 15);
            assertTrue(world.fog().isVisible(lurker.id()), "7.81 tiles is inside sight");

            // sqrt(36 + 36) = ~8.49 tiles: unseen.
            world.teleport(lurker, APosition.create(16, 16));
            tickTo(world, 30);
            assertFalse(world.fog().isVisible(lurker.id()), "8.49 tiles is fog");

            // Exactly 8.0 tiles: still visible (within means <=).
            world.teleport(lurker, APosition.create(18, 10));
            tickTo(world, 45);
            assertTrue(world.fog().isVisible(lurker.id()), "exactly 8.0 tiles still counts as seen");
        }
        finally {
            world.close();
        }
    }

    @Test
    public void fogMemoryKeepsLastSeen() {
        FakeUnit eye = eye(10, 10);
        FakeUnit lurker = lurker(15, 16);
        SimWorld world = new SimWorld(
            fakeOurs(eye), fakeEnemies(lurker), new SimArena(40 * TILE, 40 * TILE));
        try {
            tickTo(world, 15);
            assertTrue(world.fog().isVisible(lurker.id()));

            world.teleport(lurker, APosition.create(30, 30));
            tickTo(world, 30);

            assertFalse(world.fog().isVisible(lurker.id()), "far away means unseen");

            SimFog.Contact contact = world.fog().contact(lurker.id());
            assertEquals(15, contact.lastSeenFrame, "last contact was the frame-15 evaluation");
            assertEquals(15, contact.lastSeenTx, "last-seen tile is where it was seen");
            assertEquals(16, contact.lastSeenTy, "last-seen tile is where it was seen");
        }
        finally {
            world.close();
        }
    }
}
