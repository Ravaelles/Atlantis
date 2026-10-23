package tests.unit;

import atlantis.battle.scout.ScoutUnitManagers;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.position.APosition;
import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner report 2026-10-23 (NEXT #187, critical): a scout that finds the
 * enemy main with no battle units yet leaves and wanders to far expos.
 * Required: circle the enemy main while no enemy combat unit is inside
 * (its attack range + 3 tiles); retreat toward home only when hurt.
 *
 * <p>The chaser is teleported every frame so the decision geometry stays
 * exact no matter how the stub world moves units: the test pins the
 * distance rule, not the engine.</p>
 */
@Tag("map")
public class ScoutCirclesEnemyMainTest extends WorldStubForTests {

    private static final double HOME_X = 7, HOME_Y = 44;
    private static final double FOE_X = 117, FOE_Y = 9;

    private static final Pattern TILE = Pattern.compile("\\[\\s*(\\d+(?:\\.\\d+)?)\\s*,\\s*(\\d+(?:\\.\\d+)?)\\s*\\]");

    @BeforeEach
    public void resetScoutState() throws Exception {
        resetSharedScoutStatic();
    }

    @AfterEach
    public void clearScoutState() throws Exception {
        resetSharedScoutStatic();
    }

    private void resetSharedScoutStatic() throws Exception {
        ScoutState.reset();
        ScoutState.scouts.clear();
        ScoutState.scoutsKilledCount = 0;
        Field last = EnemyMainBase.class.getDeclaredField("_lastNonNull");
        last.setAccessible(true);
        last.set(null, null);
        Field cache = EnemyMainBase.class.getDeclaredField("cache");
        cache.setAccessible(true);
        ((Cache<?>) cache.get(null)).clear();
    }

    private FakeUnit[] oursWithScoutAt(double x, double y) {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, HOME_X, HOME_Y);
        FakeUnit scout = fake(AUnitType.Protoss_Probe, x, y);
        return new FakeUnit[]{nexus, scout};
    }

    private void runScoutChain(int frames, FakeUnit[] ours, FakeUnit[] enemies, Runnable perFrame) {
        world(frames, ours, enemies, () -> {
            if (atlantis.game.A.now() == 1) {
                ScoutState.scoutUnitManager = new ScoutUnitManagers();
                ScoutState.scouts.add(ours[1]);
                ours[1].setScout(true);
                seedRoamRing();
            }
            if (perFrame != null) perFrame.run();
            new ScoutCommander().handle();
        });
    }

    /**
     * The stub world has no BWEM areas, so real regions expose no boundary
     * polygon and the roam manager could never engage here. Seed the ring
     * the live game builds from region boundaries: 8 points on a 6-tile
     * circle around the enemy main. This pins the circling behavior, not
     * the polygon init (live-game territory).
     */
    private void seedRoamRing() {
        if (!ScoutState.scoutingAroundBasePoints.empty()) return;
        atlantis.map.region.ARegion region = tests.fakes.FakeRegion.getByTxTy(117, 9);
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            atlantis.map.position.APosition p = atlantis.map.position.APosition.createFromPixels(
                (int) ((117 + 6 * Math.cos(a)) * 32), (int) ((9 + 6 * Math.sin(a)) * 32));
            ScoutState.scoutingAroundBasePoints.addPosition(
                atlantis.map.region.ARegionBoundary.create(region, p));
        }
    }

    private List<double[]> moveDests(FakeUnit scout) {
        List<double[]> out = new ArrayList<>();
        for (FakeOrderSink.Record r : ((FakeOrderSink) scout.orderSink()).orders()) {
            if (!r.operation.equals("move") && !r.operation.equals("patrol")) continue;
            Matcher m = TILE.matcher(r.detail);
            if (m.find()) out.add(new double[]{Double.parseDouble(m.group(1)) / 32, Double.parseDouble(m.group(2)) / 32});
        }
        return out;
    }

    private static double dist(double[] a, double bx, double by) {
        return Math.hypot(a[0] - bx, a[1] - by);
    }

    @Test
    public void noEnemiesMeansCirclingTheMainNeverWanderingOff() {
        FakeUnit[] ours = oursWithScoutAt(113, 12);
        FakeUnit foeNexus = fakeEnemies(fake(AUnitType.Protoss_Nexus, FOE_X, FOE_Y))[0];

        runScoutChain(150, ours, new FakeUnit[]{foeNexus}, null);

        FakeUnit scout = ours[1];
        List<double[]> dests = moveDests(scout);
        assertTrue(!dests.isEmpty(), "the chain issues scout moves at all: "
            + ((FakeOrderSink) scout.orderSink()).orders());
        for (double[] d : dests) {
            assertTrue(dist(d, FOE_X, FOE_Y) <= 25,
                "every destination stays around the enemy main, never a far expo: "
                    + dests.stream().map(p -> "(" + (int) p[0] + "," + (int) p[1] + ")")
                        .reduce((a, b) -> a + " " + b).orElse("")
                    + " logs=" + scout.managerLogs());
        }
        String logs = scout.managerLogs().toString();
        assertTrue(logs.contains("RoamAroundEnemyBase") || logs.contains("MoveBetweenEnemyBaseAndEnemyNatural"),
            "a main-hugging manager claims the scout: " + logs);
        assertTrue(!logs.contains("ScoutPotentialEnemyBases") && !logs.contains("ScoutFreeBases")
            && !logs.contains("ScoutUnexploredBasesNearEnemy") && !logs.contains("ScoutEnemyBasesWithoutBase"),
            "no far-expo explorer claims a healthy scout at a found main: " + logs);
    }

    @Test
    public void meleeChaserAtFiveTilesDoesNotSeparate() {
        FakeUnit[] ours = oursWithScoutAt(113, 12);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, FOE_X, FOE_Y).setEnemy();
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 0, 0).setEnemy();

        runScoutChain(120, ours, new FakeUnit[]{foeNexus, zealot}, () ->
            zealot.position = ours[1].position().translateByPixels(5 * 32, 0));

        String logs = ours[1].managerLogs().toString();
        assertTrue(!logs.contains("ScoutSeparateFromCloseEnemies"),
            "zealot at 5 tiles (range + 3 = ~4) is not a chase yet: " + logs);
        List<double[]> dests = moveDests(ours[1]);
        assertTrue(!dests.isEmpty(), "scout keeps working: " + logs);
        String destStr = dests.stream().map(p -> "(" + (int) p[0] + "," + (int) p[1] + ")")
            .reduce((a, b) -> a + " " + b).orElse("");
        for (double[] d : dests) {
            assertTrue(dist(d, FOE_X, FOE_Y) <= 25,
                "no fleeing the map while the chaser is outside range + 3: " + destStr
                    + " logs=" + ours[1].managerLogs());
        }
    }

    @Test
    public void adjacentMeleeSeparatesButDoesNotFleeHome() {
        FakeUnit[] ours = oursWithScoutAt(113, 12);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, FOE_X, FOE_Y).setEnemy();
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 0, 0).setEnemy();

        runScoutChain(120, ours, new FakeUnit[]{foeNexus, zealot}, () ->
            zealot.position = ours[1].position().translateByPixels(2 * 32, 0));

        String logs = ours[1].managerLogs().toString();
        assertTrue(logs.contains("ScoutSeparateFromCloseEnemies") || logs.contains("DoAvoidEnemies"),
            "adjacent chaser triggers separation micro: " + logs);
        for (double[] d : moveDests(ours[1])) {
            assertTrue(dist(d, HOME_X, HOME_Y) >= 12,
                "healthy scout separates locally, does not run home: " + d[0] + "," + d[1]);
        }
    }

    @Test
    public void hurtScoutRetreatsHome() {
        FakeUnit[] ours = oursWithScoutAt(113, 12);
        ours[1].setHp(12);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, FOE_X, FOE_Y).setEnemy();
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 0, 0).setEnemy();

        // Chaser pinned NORTH: straight-south dodges stay at x=113, only a
        // homeward retreat bends west toward the main at (7,44).
        runScoutChain(150, ours, new FakeUnit[]{foeNexus, zealot}, () ->
            zealot.position = ours[1].position().translateByPixels(0, -5 * 32));

        double startHome = Math.hypot(113 - HOME_X, 12 - HOME_Y);
        boolean homeward = false;
        for (double[] d : moveDests(ours[1])) {
            if (d[0] <= 103 && Math.hypot(d[0] - HOME_X, d[1] - HOME_Y) <= startHome - 8) homeward = true;
        }
        assertTrue(homeward, "hurt chased scout retreats toward home, not merely south: scoutHp=" + ours[1].hp()
            + " pos=" + ours[1].position() + " logs=" + ours[1].managerLogs());
    }
}
