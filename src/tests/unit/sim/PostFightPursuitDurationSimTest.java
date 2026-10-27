package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Map-duration proxy for Drag_v_LingsUpg2 (owner: the map must finish
 * fast). The faithful shape: survivors are SEEN first, then flee out
 * of sight (fogged memory, not never-seen) - sight comes back when the
 * line closes in, and the fight resumes. A post-fight standstill shows
 * up directly as clear time.
 */
@Tag("combat")
public class PostFightPursuitDurationSimTest extends AbstractTestWithUnits {

    @Test
    public void twoWavesClearFast() {
        aGame.when(AGame::isUms).thenReturn(true);

        List<FakeUnit> ours = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            FakeUnit d = fake(AUnitType.Protoss_Dragoon, 10 + i, 20);
            d.cooldown = 0;
            ours.add(d);
        }
        List<FakeUnit> foes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, 15 + i, 20);
            ling.setEnemy();
            foes.add(ling);
        }

        SimArena arena = new SimArena(70 * 32, 40 * 32);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);
        int clearTick = -1;
        int fledAt = -1;
        int engagedAt = -1;
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 1500; i++) {
                final List<FakeUnit> foesNow = foes;
                final List<FakeUnit> oursNow = ours;
                final boolean fled = fledAt >= 0;
                final boolean engaged = engagedAt >= 0;
                world.tick(() -> {
                    for (FakeUnit d : oursNow) {
                        if (d.hp > 0) BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, null, d);
                    }
                    if (!fled) ZombiePolicies.chaseNearest(foesNow, oursNow);
                    else if (!engaged) ZombiePolicies.holdStill(foesNow, oursNow);
                    else ZombiePolicies.chaseNearest(foesNow, oursNow);
                    reacquire(oursNow, foesNow);
                });
                if (fledAt < 0 && liveCount(foes) <= 3) {
                    fledAt = i;
                    for (FakeUnit f : foes) {
                        if (f.hp <= 0) continue;
                        world.teleport(f, APosition.create(38, 20));
                        f.isVisibleUnitOnMap = false;
                    }
                }
                if (engagedAt < 0 && fledAt >= 0 && anyVisible(foes)) engagedAt = i;
                if (liveCount(foes) == 0) {
                    clearTick = i;
                    break;
                }
                if (i % 200 == 0) {
                    StringBuilder sb = new StringBuilder("PURSUIT t=" + i);
                    for (FakeUnit d : ours) sb.append(" d").append(d.id()).append("@(")
                        .append(d.x() / 32).append(",").append(d.y() / 32).append(")hp").append(d.hp);
                    System.out.println(sb);
                }
            }
            System.out.println("PURSUIT-DURATION fledAt=" + fledAt
                + " engagedAt=" + engagedAt + " clearTick=" + clearTick);
            assertTrue(fledAt >= 0, "setup: the first wave must fall");
            assertTrue(engagedAt >= 0 && engagedAt < 360,
                "the line must re-acquire the fled survivors instead of standing"
                    + " out the stale window: engagedAt=" + engagedAt);
            assertTrue(clearTick >= 0 && clearTick < 520,
                "both waves must fall fast (measured 336, pre-fix never clears"
                    + " in 1500): clearTick=" + clearTick);
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    /** Sight comes back when the line closes in - the live re-acquisition. */
    private static void reacquire(List<FakeUnit> ours, List<FakeUnit> foes) {
        for (FakeUnit ling : foes) {
            if (ling.hp <= 0 || ling.isVisibleUnitOnMap) continue;
            for (FakeUnit d : ours) {
                if (d.hp <= 0) continue;
                try {
                    if (d.distTo(ling) <= 10) ling.isVisibleUnitOnMap = true;
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static boolean anyVisible(List<FakeUnit> foes) {
        for (FakeUnit f : foes) {
            if (f.hp > 0 && f.isVisibleUnitOnMap) return true;
        }
        return false;
    }

    private static int liveCount(List<FakeUnit> foes) {
        int n = 0;
        for (FakeUnit f : foes) if (f.hp > 0) n++;

        return n;
    }
}
