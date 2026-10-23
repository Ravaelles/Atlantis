package tests.unit.sim;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterTactic;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Stardust rule, live-fire proof in the sim (owner 2026-10-26, NEXT #188):
 * a cluster facing an armed enemy force sitting between it and home
 * fights through instead of running into it.
 *
 * <p>Same force both arms (3 wounded zealots vs 12 hydras - melee cannot
 * kite); only the geometry differs: treatment blocks the homeward
 * segment, control sits lateral to it. Treatment must put attack orders
 * on the blockers; control must walk home without engaging them.
 * (Dragoons were tried first: the JFAP sim rates their kiting above
 * 3v8, so no losing geometry was constructible with them. The override
 * branch itself - eval &lt; 1 yet FIGHT - is pinned at chain level in
 * RetreatSplitChainTest with a measured eval of 0.146; in this sim the
 * treatment arm reads eval &gt;= 1, so this test proves the end-to-end
 * behavior, not the branch.)</p>
 */
@Tag("combat")
public class BlockingForceSimTest extends tests.unit.AbstractTestWithUnits {

    private static final int TILE = 32;

    private static final class Outcome {
        int attacksOnFoes;
        double endX = 55;
    }

    @Test
    public void blockingForceIsFoughtThroughNotFledInto() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 30);

        // Blockers sit on the homeward segment (t ~ 0.5, corridor 0).
        Outcome blocked = run(nexus,
            new double[][]{{49, 30}, {51, 30}, {47, 30}, {49, 32}, {51, 32}, {47, 32}, {49, 28}, {51, 28}, {47, 28}, {45, 30}, {53, 30}, {49, 34}});
        // Same force, same distance, lateral to the segment (t = 0).
        Outcome lateral = run(nexus,
            new double[][]{{55, 22}, {57, 22}, {53, 22}, {55, 24}, {57, 24}, {53, 24}, {55, 20}, {57, 20}, {53, 20}, {59, 22}, {51, 22}, {55, 26}});

        assertTrue(blocked.attacksOnFoes >= 3,
            "blocking army is engaged, not fled into: " + blocked.attacksOnFoes + " attack orders");
        assertTrue(lateral.attacksOnFoes == 0,
            "lateral army is walked away from, not engaged: " + lateral.attacksOnFoes + " attack orders");
        assertTrue(lateral.endX <= 45,
            "unblocked cluster retreats home (x falls toward the main at 10): end x=" + lateral.endX);
    }

    private Outcome run(FakeUnit nexus, double[][] foeSpots) {
        FakeUnit[] zealots = new FakeUnit[]{
            fake(AUnitType.Protoss_Zealot, 55, 30).setHp(25),
            fake(AUnitType.Protoss_Zealot, 57, 30).setHp(25),
            fake(AUnitType.Protoss_Zealot, 53, 30).setHp(25),
        };
        FakeUnit[] foes = new FakeUnit[foeSpots.length]; // NOSONAR
        for (int i = 0; i < foeSpots.length; i++) {
            foes[i] = fake(AUnitType.Zerg_Hydralisk, foeSpots[i][0], foeSpots[i][1]);
        }
        Set<Integer> foeIds = new HashSet<>();
        for (FakeUnit f : foes) foeIds.add(f.id());

        Outcome out = new Outcome();
        SimArena arena = new SimArena(100 * TILE, 60 * TILE);
        SimWorld world = new SimWorld(fakeOurs(nexus, zealots[0], zealots[1], zealots[2]), fakeEnemies(foes), arena);
        try {
            BattleCommander.clearForTests();
            ClusterTactic.reset();
            Alpha.gather();

            for (int i = 0; i < 500; i++) {
                final FightOrFlight.Tactic tactic = ClusterTactic.decide();
                world.tick(() -> {
                    for (FakeUnit zealot : zealots) {
                        if (zealot.hp <= 0) continue;
                        BattleCommander.act(zealot, tactic, nexus.position(), zealots[0]);
                    }
                });
                double cx = 0;
                int alive = 0;
                for (FakeUnit zealot : zealots) {
                    if (zealot.hp <= 0) continue;
                    cx += zealot.position.x();
                    alive++;
                }
                if (alive > 0) out.endX = (cx / alive) / TILE;
            }

            for (FakeUnit zealot : zealots) {
                for (FakeOrderSink.Record r : ((FakeOrderSink) zealot.orderSink()).orders()) {
                    if (!r.operation.equals("attackUnit")) continue;
                    try {
                        if (foeIds.contains(Integer.parseInt(r.detail.trim()))) out.attacksOnFoes++;
                    }
                    catch (NumberFormatException ignored) {
                    }
                }
            }

            return out;
        }
        finally {
            for (FakeUnit zealot : zealots) Alpha.get().removeUnit(zealot);
            Alpha.get().removeUnit(nexus);
            world.close();
            BattleCommander.clearForTests();
            ClusterTactic.reset();
        }
    }
}
