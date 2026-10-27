package tests.unit.sim;

import atlantis.map.position.APosition;
import tests.fakes.FakeUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit movement for StarEngine: accelerate toward the ordered velocity,
 * brake without orders, capped turning, obstacle slide-or-stop.
 *
 * <p>Extracted verbatim from {@code SimWorld} (SRP: the world loop owned
 * movement physics next to combat, selection mocks and read-outs). The
 * economy driver reuses this mover for builders and miners, so one
 * movement model serves both halves of the sim - FAST mode resolves
 * walks analytically instead of calling here (see {@code SimConfig}).
 */
public final class SimMovement {

    /**
     * Approach rate toward desired velocity per tick (0..1).
     */
    private static final double ACCEL = 0.35;
    /**
     * Kept velocity fraction per tick with no order.
     */
    private static final double BRAKE = 0.55;
    /**
     * Max heading change per tick, degrees.
     */
    private static final double MAX_TURN_DEG = 75.0;

    private final SimArena arena;

    private final Map<Integer, double[]> velocity = new HashMap<>();
    private final Map<Integer, double[]> precise = new HashMap<>();
    private final Map<Integer, Integer> stuckFrames = new HashMap<>();

    public SimMovement(SimArena arena) {
        this.arena = arena;
    }

    /**
     * Starts tracking a unit: still, at its published position.
     */
    public void register(FakeUnit unit) {
        velocity.put(unit.id(), new double[]{0.0, 0.0});
        precise.put(unit.id(),
            new double[]{(double) unit.position.x(), (double) unit.position.y()});
        stuckFrames.put(unit.id(), 0);
    }

    /**
     * Advances every living unit one tick toward its ordered velocity.
     */
    public void stepAll(List<FakeUnit> units) {
        for (FakeUnit unit : units) {
            if (unit == null || unit.hp <= 0) continue;
            ensureRegistered(unit);
            double[] vel = velocity.get(unit.id());
            double[] desired = desiredVelocity(unit);
            if (desired == null) {
                vel[0] *= BRAKE;
                vel[1] *= BRAKE;
                if (Math.hypot(vel[0], vel[1]) < 0.05) {
                    vel[0] = 0;
                    vel[1] = 0;
                }
            }
            else {
                double[] turned = limitTurn(vel, desired);
                vel[0] += (turned[0] - vel[0]) * ACCEL;
                vel[1] += (turned[1] - vel[1]) * ACCEL;
            }
            step(unit, vel);
        }
    }

    /**
     * Teleports a unit (scenario use only): the sim owns positions, so a
     * bare write to {@code unit.position} is overwritten by the next step.
     * Updates both the published position and the precise accumulator,
     * and stops residual motion.
     */
    public void teleport(FakeUnit unit, APosition dest) {
        unit.position = dest;
        precise.put(unit.id(), new double[]{(double) dest.x(), (double) dest.y()});
        velocity.put(unit.id(), new double[]{0.0, 0.0});
    }

    /**
     * Precise (sub-pixel) position: the published FakeUnit position snaps
     * to whole pixels, so pixel reads freeze frames before the model
     * actually stops. Braking pins read this.
     */
    public double[] precisePosition(FakeUnit unit) {
        double[] pos = precise.get(unit.id());

        return new double[]{pos[0], pos[1]};
    }

    public int stuckFrames(FakeUnit unit) {
        return stuckFrames.getOrDefault(unit.id(), 0);
    }

    public int maxStuckFrames(FakeUnit[] units) {
        int max = 0;
        for (FakeUnit unit : units) max = Math.max(max, stuckFrames(unit));

        return max;
    }

    // =========================================================

    private void ensureRegistered(FakeUnit unit) {
        if (!precise.containsKey(unit.id())) register(unit);
    }

    /**
     * Ordered velocity in px/frame, or null when the unit should brake.
     */
    private double[] desiredVelocity(FakeUnit unit) {
        String order = unit.lastCommand;
        if ("Move".equals(order) && unit.targetPosition != null) {
            return toward(unit, unit.targetPosition.x(), unit.targetPosition.y(), topSpeed(unit));
        }
        if ("AttackUnit".equals(order) && unit.target != null && unit.target.isAlive()) {
            if (unit.distTo(unit.target) <= SimWorld.reachTiles(unit, unit.target)) return null;

            return toward(unit, unit.target.position.x(), unit.target.position.y(), topSpeed(unit));
        }
        if ("AttackUnit".equals(order) && unit.targetPosition != null) {
            return toward(unit, unit.targetPosition.x(), unit.targetPosition.y(), topSpeed(unit));
        }
        return null;
    }

    private double[] toward(FakeUnit unit, int tx, int ty, double speed) {
        double dx = tx - unit.position.x();
        double dy = ty - unit.position.y();
        double dist = Math.hypot(dx, dy);
        if (dist < 1.0) return new double[]{0.0, 0.0};

        return new double[]{dx / dist * speed, dy / dist * speed};
    }

    private double topSpeed(FakeUnit unit) {
        try {
            return unit.maxSpeed();
        } catch (Exception e) {
            return 4.0;
        }
    }

    private double[] limitTurn(double[] vel, double[] desired) {
        double speed = Math.hypot(vel[0], vel[1]);
        if (speed < 0.05) return desired;

        double cur = Math.toDegrees(Math.atan2(vel[1], vel[0]));
        double want = Math.toDegrees(Math.atan2(desired[1], desired[0]));
        double diff = want - cur;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        double capped = Math.max(-MAX_TURN_DEG, Math.min(MAX_TURN_DEG, diff));
        double angle = Math.toRadians(cur + capped);
        double mag = Math.hypot(desired[0], desired[1]);

        return new double[]{Math.cos(angle) * mag, Math.sin(angle) * mag};
    }

    private void step(FakeUnit unit, double[] vel) {
        // Precise position accumulates fractional velocity (integer steps
        // alone stall a slide: round(-0.41) is 0 forever). Only the
        // published FakeUnit position snaps to whole pixels.
        double[] pos = precise.get(unit.id());
        double nx = arena.clampXd(pos[0] + vel[0]);
        double ny = arena.clampYd(pos[1] + vel[1]);
        if (arena.isWalkablePx((int) Math.round(nx), (int) Math.round(ny))) {
            pos[0] = nx;
            pos[1] = ny;
            publish(unit, pos);
            stuckFrames.put(unit.id(), 0);

            return;
        }
        double sx = arena.clampXd(pos[0] + vel[0]);
        if (arena.isWalkablePx((int) Math.round(sx), (int) Math.round(pos[1]))) {
            pos[0] = sx;
            publish(unit, pos);
            stuckFrames.put(unit.id(), 0);

            return;
        }
        double sy = arena.clampYd(pos[1] + vel[1]);
        if (arena.isWalkablePx((int) Math.round(pos[0]), (int) Math.round(sy))) {
            pos[1] = sy;
            publish(unit, pos);
            stuckFrames.put(unit.id(), 0);

            return;
        }
        stuckFrames.put(unit.id(), stuckFrames.get(unit.id()) + 1);
    }

    private void publish(FakeUnit unit, double[] pos) {
        unit.position = unit.position.translateByPixels(
            (int) Math.round(pos[0]) - unit.position.x(),
            (int) Math.round(pos[1]) - unit.position.y());
    }
}
