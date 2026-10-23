package tests.unit.sim;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.BaseSelect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.Mockito;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.unit.helpers.ClearAllCaches;

/**
 * A fast, fully controlled fight stepper for behavior scenarios (NEXT #161).
 *
 * <p>One tick = decisions (the test's callback, usually the REAL decision
 * code like {@code BattleCommander.act}) + movement with acceleration,
 * braking and capped turning + attack cycles with real engine data +
 * damage + death. Hundreds of ticks run in milliseconds, with exact
 * control over counts, positions and obstacles - the thing OpenBW UMS
 * cannot give today and Wine only gives on owner call.
 *
 * <p>What is real here: top speed, cooldown, range and damage come from
 * the engine/binding through the units themselves
 * ({@code maxSpeed()}, {@code cooldownAbsolute()}, {@code damageAgainst},
 * guarded {@code UnitStats} table). What is a model, documented as such:
 * attack windup frames per type (the binding exposes no animation split;
 * values below), acceleration/braking/turn rates, shields-first damage.
 * Assertions pin behavior and coarse outcomes (fled? withdrew? alive?),
 * never exact HP as engine truth.
 *
 * <p>Does NOT touch the shared harness: {@code FakeOnFrameEnd} keeps its
 * mover for decision tests, this mover lives here for behavior scenarios.
 */
public class SimWorld {

    /** Approach rate toward desired velocity per tick (0..1). */
    private static final double ACCEL = 0.35;
    /** Kept velocity fraction per tick with no order. */
    private static final double BRAKE = 0.55;
    /** Max heading change per tick, degrees. */
    private static final double MAX_TURN_DEG = 75.0;

    /**
     * Attack windup: frames from attack start to the damage frame, per
     * unit type. MODEL PARAMS, not engine data - the binding exposes
     * cooldown/range/damage but no animation split (PurpleWave-derived
     * stops; damage lands just past the stop frame). Pinned by the
     * scenario tests only as "these are the values the sim used".
     */
    private static final Map<String, Integer> WINDUP = new HashMap<>();

    static {
        WINDUP.put("Protoss_Dragoon", 8);
        WINDUP.put("Protoss_Zealot", 8);
        WINDUP.put("Zerg_Zergling", 5);
    }

    private final FakeUnit[] ours;
    private final FakeUnit[] enemies;
    private final SimArena arena;

    private final Map<Integer, double[]> velocity = new HashMap<>();
    private final Map<Integer, double[]> precise = new HashMap<>();
    private final Map<Integer, Integer> stuckFrames = new HashMap<>();
    private final Map<Integer, Integer> attackStartedAt = new HashMap<>();

    private int frame = 0;
    private int shotsFired = 0;
    private int ourShotsFired = 0;
    private int enemyShotsFired = 0;

    public SimWorld(FakeUnit[] ours, FakeUnit[] enemies, SimArena arena) {
        this.ours = ours;
        this.enemies = enemies;
        this.arena = arena;
        for (FakeUnit unit : all()) {
            velocity.put(unit.id(), new double[] { 0.0, 0.0 });
            precise.put(unit.id(),
                new double[] { (double) unit.position.x(), (double) unit.position.y() });
            stuckFrames.put(unit.id(), 0);
        }
        // Static TTL caches assume monotonically increasing frames for the
        // whole JVM run. Every scenario restarts the frame counter, so a
        // previous run's cached-until entries would read as valid in the
        // new run (measured: the whole second run went blind - AliveEnemies
        // served the dead first run, zero enemies seen). Drop query caches
        // per scenario, not per tick: frames are monotonic inside one run.
        ClearAllCaches.clearQueries();
        installSelection();
    }

    /** BaseSelect answers with the living units of this scenario, as the game would. */
    private void installSelection() {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);
        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList)
            .thenAnswer(invocation -> living(ours));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::enemyUnits)
            .thenAnswer(invocation -> living(enemies));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::neutralUnits)
            .thenAnswer(invocation -> new ArrayList<AUnit>());
        AbstractTestWithWorld.baseSelect.when(BaseSelect::allUnits).thenAnswer(invocation -> {
            List<AUnit> alive = new ArrayList<>(living(ours));
            alive.addAll(living(enemies));

            return alive;
        });
    }

    private static List<AUnit> living(FakeUnit[] units) {
        List<AUnit> alive = new ArrayList<>();
        if (units == null) return alive;

        for (FakeUnit unit : units) {
            if (unit != null && unit.hp > 0) alive.add(unit);
        }
        return alive;
    }

    public void close() {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
    }

    /** Advances one frame: decisions first, then movement, then combat. */
    /**
     * Precise (sub-pixel) position for tests: the published FakeUnit
     * position snaps to whole pixels, so pixel reads freeze frames
     * before the model actually stops. Braking pins read this.
     */
    double[] precisePosition(FakeUnit unit) {
        double[] pos = precise.get(unit.id());
        return new double[] { pos[0], pos[1] };
    }

    public void tick(Runnable decisions) {
        frame++;
        A.setNow(frame, frame / 30);
        decisions.run();
        moveAll();
        combatAll();
        for (FakeUnit unit : all()) {
            if (unit.cooldown > 0) unit.cooldown--;
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
        precise.put(unit.id(), new double[] { (double) dest.x(), (double) dest.y() });
        velocity.put(unit.id(), new double[] { 0.0, 0.0 });
    }

    private List<FakeUnit> all() {
        List<FakeUnit> all = new ArrayList<>();
        if (ours != null) for (FakeUnit unit : ours) all.add(unit);
        if (enemies != null) for (FakeUnit unit : enemies) all.add(unit);

        return all;
    }

    // =========================================================
    // Movement: accelerate toward the ordered velocity, brake without
    // orders, capped turning, obstacle slide-or-stop.
    // =========================================================

    private void moveAll() {
        for (FakeUnit unit : all()) {
            if (!isAlive(unit)) continue;
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

    /** Ordered velocity in px/frame, or null when the unit should brake. */
    private double[] desiredVelocity(FakeUnit unit) {
        String order = unit.lastCommand;
        if ("Move".equals(order) && unit.targetPosition != null) {
            return toward(unit, unit.targetPosition.x(), unit.targetPosition.y(), topSpeed(unit));
        }
        if ("AttackUnit".equals(order) && unit.target != null && unit.target.isAlive()) {
            if (unit.distTo(unit.target) <= reachTiles(unit, unit.target)) return null;

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
        if (dist < 1.0) return new double[] { 0.0, 0.0 };

        return new double[] { dx / dist * speed, dy / dist * speed };
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

        return new double[] { Math.cos(angle) * mag, Math.sin(angle) * mag };
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

    // =========================================================
    // Combat: attack cycles with real cooldown/range/damage, windup as
    // the documented model seam, shields-first damage, death at 0 hp.
    // =========================================================

    private void combatAll() {
        for (FakeUnit unit : all()) {
            if (!isAlive(unit)) continue;
            if (!"AttackUnit".equals(unit.lastCommand)) {
                attackStartedAt.remove(unit.id());
                continue;
            }
            FakeUnit target = unit.target;
            if (target == null || !isAlive(target)) {
                attackStartedAt.remove(unit.id());
                continue;
            }
            // Tiles everywhere, like all game code: distTo returns tiles
            // and weaponRangeAgainst answers tiles. The target's body
            // radius is added (edge-to-edge, not center-to-center):
            // melee ranges truncate to int tiles (ling Claws 15px -> 0),
            // so without the radius term melee could never connect at
            // all (measured: 0 ling hits in every run before this).
            if (unit.distTo(target) > reachTiles(unit, target)) {
                continue;
            }
            if (unit.cooldown > 0) continue;
            if (!attackStartedAt.containsKey(unit.id())) {
                attackStartedAt.put(unit.id(), frame);
                unit.attackStartedAt = frame;
                continue;
            }
            int windup = WINDUP.getOrDefault(unit.type().name(), 6);
            if (frame - attackStartedAt.get(unit.id()) >= windup) {
                fire(unit, target);
                attackStartedAt.remove(unit.id());
            }
        }
    }

    private void fire(FakeUnit attacker, FakeUnit target) {
        int damage = attacker.damageAgainst(target);
        int rest = damage;
        if (target.shields > 0) {
            int absorbed = Math.min(target.shields, rest);
            target.shields -= absorbed;
            rest -= absorbed;
        }
        target.hp -= rest;
        attacker.cooldown = attacker.cooldownAbsolute();
        shotsFired++;
        if (attacker.isOur()) ourShotsFired++;
        else enemyShotsFired++;
    }

    /**
     * Engagement reach in tiles: weapon range plus the target's body
     * radius (edge-to-edge). Dimensions come from the engine binding,
     * so no hand table rots here. Single source for the sim and the
     * scripted opponents in scenario tests.
     */
    public static double reachTiles(FakeUnit attacker, FakeUnit target) {
        double weapon = attacker.weaponRangeAgainst(target);
        double radius = 0;
        try {
            int l = target.type().dimensionLeftPixels();
            int r = target.type().dimensionRightPixels();
            int u = target.type().dimensionUpPixels();
            int d = target.type().dimensionDownPixels();
            radius = Math.max(Math.max(l, r), Math.max(u, d)) / 32.0;
        } catch (Exception ignored) {
        }
        return Math.max(0, weapon) + radius;
    }

    private boolean isAlive(FakeUnit unit) {
        return unit != null && unit.hp > 0;
    }

    // =========================================================
    // Assertions read-outs.
    // =========================================================

    public int frame() {
        return frame;
    }

    public int shotsFired() {
        return shotsFired;
    }

    public int aliveOurs() {
        return living(ours).size();
    }

    public int aliveEnemies() {
        return living(enemies).size();
    }

    public int stuckFrames(FakeUnit unit) {
        return stuckFrames.getOrDefault(unit.id(), 0);
    }

    public int maxStuckFrames(FakeUnit[] units) {
        int max = 0;
        for (FakeUnit unit : units) max = Math.max(max, stuckFrames(unit));

        return max;
    }
}
