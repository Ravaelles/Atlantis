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
 * Post-fight freeze (owner report, Drag_v_LingsUpg2): after killing part
 * of a pack, the line stands on the kill site instead of marching at
 * the remembered enemy - the fresh ghost shadows older memory for the
 * 240f stale window while the reached-claim holds the unit with no
 * order. Repro: partial kill, survivors flee out of sight, the line
 * must displace toward them well before the stale window ends.
 */
@Tag("combat")
public class PostFightFreezeSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void lineMarchesAtRememberedEnemyAfterPartialKill() {
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit d1 = fake(AUnitType.Protoss_Dragoon, 10, 20);
        FakeUnit d2 = fake(AUnitType.Protoss_Dragoon, 11, 20);
        FakeUnit d3 = fake(AUnitType.Protoss_Dragoon, 12, 20);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(d1);
        ours.add(d2);
        ours.add(d3);
        for (FakeUnit d : ours) d.cooldown = 0;

        List<FakeUnit> foes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, 15 + i, 20);
            ling.setEnemy();
            foes.add(ling);
        }

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            // Phase 1: the visible fight, until half the pack is dead.
            int killFrame = -1;
            for (int i = 0; i < 600; i++) {
                final List<FakeUnit> foesNow = foes;
                final List<FakeUnit> oursNow = ours;
                world.tick(() -> {
                    for (FakeUnit d : oursNow) {
                        if (d.hp > 0) BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, null, d);
                    }
                    ZombiePolicies.chaseNearest(foesNow, oursNow);
                });
                if (liveCount(foes) <= 3) {
                    killFrame = i;
                    break;
                }
            }
            assertTrue(killFrame >= 0, "setup: the line must score a partial kill first");

            // Phase 2: survivors flee out of sight (fogged memory, not death).
            List<FakeUnit> survivors = liveFoes(foes);
            assertTrue(survivors.size() >= 2, "setup: survivors must remain, got " + survivors.size());
            for (int i = 0; i < survivors.size(); i++) {
                world.teleport(survivors.get(i), APosition.create(38 + i, 20));
                survivors.get(i).isVisibleUnitOnMap = false;
            }
            double[] kx = new double[] { d1.x(), d2.x(), d3.x() };
            double[] ky = new double[] { d1.y(), d2.y(), d3.y() };

            // Phase 3: the line must march at the remembered enemy -
            // standing the 240f stale window on the kill site is the bug.
            for (int i = 0; i < 200; i++) {
                final List<FakeUnit> foesNow = foes;
                final List<FakeUnit> oursNow = ours;
                world.tick(() -> {
                    for (FakeUnit d : oursNow) {
                        if (d.hp > 0) BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, null, d);
                    }
                    ZombiePolicies.holdStill(foesNow, oursNow);
                });
                if (i == 120) {
                    FakeUnit[] ds = new FakeUnit[] { d1, d2, d3 };
                    for (int u = 0; u < ds.length; u++) {
                        if (ds[u].hp <= 0) continue;
                        double moved = Math.hypot(ds[u].x() - kx[u], ds[u].y() - ky[u]) / TILE;
                        assertTrue(moved > 3,
                            "no 10s freeze: dragoon " + u + " displaced " + moved
                                + " tiles 120f after the partial kill (kill site hold)");
                    }
                }
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    private static int liveCount(List<FakeUnit> foes) {
        int n = 0;
        for (FakeUnit f : foes) if (f.hp > 0) n++;

        return n;
    }

    private static List<FakeUnit> liveFoes(List<FakeUnit> foes) {
        List<FakeUnit> live = new ArrayList<>();
        for (FakeUnit f : foes) if (f.hp > 0) live.add(f);

        return live;
    }
}
