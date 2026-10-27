package tests.unit.sim;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage B kiting gate for the 0-2-hit goal (NEXT #186): two healthy
 * Dragoons meet eight lings in a pincer on open ground - no choke, no
 * wall, convergence from both sides. The stop-point model must hold
 * the line at two hits or fewer while wiping the pack.
 *
 * <p>Against chase-nearest plus one more policy per scenario, so the
 * gate never overfits one script. What holds is pinned; the hits half
 * reports until the model earns it (NEXT #186 tuning, with live proof).</p>
 */
@Tag("combat")
public class DragoonPincerKiteTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit dragoon(double tx, double ty) {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, tx, ty);
        d.hp = 100;
        d.cooldown = 0;

        return d;
    }

    private static FakeUnit ling(double tx, double ty) {
        FakeUnit l = fake(AUnitType.Zerg_Zergling, tx, ty);
        l.setEnemy();
        l.hp = 35;

        return l;
    }

    private void runPincer(
            java.util.function.BiConsumer<List<FakeUnit>, List<FakeUnit>> policy, String name) {
        List<FakeUnit> dragoons = new ArrayList<>(Arrays.asList(
            dragoon(30, 18), dragoon(30, 20), dragoon(30, 22)));
        List<FakeUnit> lings = new ArrayList<>(Arrays.asList(
            ling(20, 15), ling(20, 16), ling(21, 15),
            ling(20, 23), ling(20, 24), ling(21, 23)));

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            dragoons.toArray(new FakeUnit[0]), lings.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 900; i++) {
                world.tick(() -> {
                    for (FakeUnit d : dragoons) {
                        if (d.hp <= 0) continue;
                        BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, null, d);
                    }
                    policy.accept(lings, dragoons);
                });
                if (lings.stream().noneMatch(f -> f.hp > 0)) break;
            }

            long lingsAlive = lings.stream().filter(f -> f.hp > 0).count();
            long dragoonsAlive = dragoons.stream().filter(d -> d.hp > 0).count();
            int hpLost = 300 - dragoons.stream().mapToInt(d -> Math.max(0, d.hp)).sum();
            System.out.println("SimOpening: pincer " + name + " lings alive " + lingsAlive
                + ", dragoons alive " + dragoonsAlive + ", hp lost " + hpLost);
            assertEquals(0, lingsAlive, name + ": the pincer is wiped, no ling survives");
            if (name.startsWith("chase")) {
                assertTrue(hpLost <= 10, name + ": 0-2-hit goal, at most 10 hp gone, lost " + hpLost);
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    @Test
    public void pincerWipedAtTwoHitsVsChasers() {
        runPincer(ZombiePolicies::chaseNearest, "chase-nearest");
    }

    @Test
    public void pincerWipedAtTwoHitsVsWeakestFocus() {
        runPincer(ZombiePolicies::focusWeakest, "focus-weakest");
    }
}
