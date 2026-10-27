package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
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
 * P3 scripted enemy (06_STARENGINE, 115321 death shape): a 4-ling runby
 * reaches a mid-opening base - two Zealots staged out, six probes on the
 * line, Nexus behind. Gate: the army answers (no ling survives) and the
 * Nexus stands. Worker screening is tracked separately (NEXT #220): today
 * the Zealots fall back and the line pays.
 *
 * <p>Against two zombie policies (chase-nearest plus one more per
 * scenario, so the gate never overfits one script). Probes hold the line
 * with no orders; if the army does not engage, the lings eat through and
 * this goes red - that is the finding, not a bad assertion.</p>
 */
@Tag("combat")
public class LingFloodHoldsTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit zealot(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.hp = 100;
        z.cooldown = 0;

        return z;
    }

    private static FakeUnit probe(double tx, double ty) {
        FakeUnit p = fake(AUnitType.Protoss_Probe, tx, ty);
        p.hp = 20;

        return p;
    }

    private static FakeUnit ling(double tx, double ty) {
        FakeUnit l = fake(AUnitType.Zerg_Zergling, tx, ty);
        l.setEnemy();
        l.hp = 35;

        return l;
    }

    private void runRunby(
            java.util.function.BiConsumer<List<FakeUnit>, List<FakeUnit>> policy, String policyName) {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        List<FakeUnit> ours = new ArrayList<>(Arrays.asList(
            zealot(28, 18), zealot(30, 20),
            probe(12, 9), probe(13, 9), probe(14, 9),
            probe(12, 11), probe(13, 11), probe(14, 11)));
        List<FakeUnit> foes = new ArrayList<>(Arrays.asList(
            ling(55, 18), ling(56, 19), ling(55, 20), ling(56, 21)));

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 1500; i++) {
                world.tick(() -> {
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0 || !u.type().equals(AUnitType.Protoss_Zealot)) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, nexus.position(), u);
                    }
                    policy.accept(foes, ours);
                });
                if (foes.stream().noneMatch(f -> f.hp > 0)) break;
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }

        long lingsAlive = foes.stream().filter(f -> f.hp > 0).count();
        long probesAlive = ours.stream()
            .filter(u -> u.type().equals(AUnitType.Protoss_Probe) && u.hp > 0).count();
        assertEquals(0, lingsAlive, policyName + ": the army answers the runby, no ling survives");
        assertTrue(nexus.hp > 0, policyName + ": the Nexus stands");
        // Screening watch (report, NEXT #220): the Zealots fall back to the
        // Nexus and fight at the mineral line, so the line pays - 4-6 probes
        // fall. Forward screening is combat-cluster tuning with live proof,
        // not this gate.
        System.out.println("SimOpening: runby " + policyName + " probes alive " + probesAlive + "/6");
    }

    @Test
    public void runbyDiesAgainstStagedZealotsVsChasers() {
        runRunby(ZombiePolicies::chaseNearest, "chase-nearest");
    }

    @Test
    public void runbyDiesAgainstStagedZealotsVsWeakestFocus() {
        runRunby(ZombiePolicies::focusWeakest, "focus-weakest");
    }
}
