package tests.unit.sim;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.BaseSelect;
import org.mockito.Mockito;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.unit.helpers.ClearAllCaches;

import java.util.*;

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
    private final SimMovement movement;

    private final Map<Integer, Integer> attackStartedAt = new HashMap<>();

    private int frame = 0;
    private int shotsFired = 0;
    private int ourShotsFired = 0;
    private int enemyShotsFired = 0;

    public SimWorld(FakeUnit[] ours, FakeUnit[] enemies, SimArena arena) {
        this.ours = ours;
        this.enemies = enemies;
        this.movement = new SimMovement(arena);
        for (FakeUnit unit : all()) movement.register(unit);
        // Static TTL caches assume monotonically increasing frames for the
        // whole JVM run. Every scenario restarts the frame counter, so a
        // previous run's cached-until entries would read as valid in the
        // new run (measured: the whole second run went blind - AliveEnemies
        // served the dead first run, zero enemies seen). Drop query caches
        // per scenario, not per tick: frames are monotonic inside one run.
        ClearAllCaches.clearQueries();
        installSelection();
    }

    private static List<AUnit> living(FakeUnit[] units) {
        List<AUnit> alive = new ArrayList<>();
        if (units == null) return alive;

        for (FakeUnit unit : units) {
            if (unit != null && unit.hp > 0) alive.add(unit);
        }
        return alive;
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

    /**
     * BaseSelect answers with the living units of this scenario, as the game would.
     */
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

    /** Advances one frame: decisions first, then movement, then combat. */

    public void close() {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
    }

    /**
     * Precise (sub-pixel) position for tests: the published FakeUnit
     * position snaps to whole pixels, so pixel reads freeze frames
     * before the model actually stops. Braking pins read this.
     */
    double[] precisePosition(FakeUnit unit) {
        return movement.precisePosition(unit);
    }

    public void tick(Runnable decisions) {
        frame++;
        A.setNow(frame, frame / 30);
        decisions.run();
        movement.stepAll(all());
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
        movement.teleport(unit, dest);
    }

    // =========================================================
    // Combat: attack cycles with real cooldown/range/damage, windup as
    // the documented model seam, shields-first damage, death at 0 hp.
    // =========================================================

    private List<FakeUnit> all() {
        List<FakeUnit> all = new ArrayList<>();
        if (ours != null) Collections.addAll(all, ours);
        if (enemies != null) Collections.addAll(all, enemies);

        return all;
    }

    private void combatAll() {
        for (FakeUnit unit : all()) {
            if (!isAlive(unit)) continue;
            if (!"AttackUnit".equals(unit.lastCommand)) {
                attackStartedAt.remove(unit.id());
                continue;
            }
            FakeUnit target = unit.target;
            if (!isAlive(target)) {
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
            int windup = WINDUP.getOrDefault(unit.type().fullName(), 6);
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
        return movement.stuckFrames(unit);
    }

    public int maxStuckFrames(FakeUnit[] units) {
        return movement.maxStuckFrames(units);
    }
}
