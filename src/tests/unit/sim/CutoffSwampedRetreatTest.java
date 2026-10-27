package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sim escape-capability pin for the cutoff-surround shape (NEXT #190):
 * live GAME_9415845A Dragoon #193, cut off ~30 tiles from a FLEEing
 * cluster with 4 zealots on it, fought in place (Attack/ClusterKite
 * churn + stuck-no-step, 4.2 tiles over 400 frames) and died without
 * ever running - 10 of the 15 dragoon deaths in that game show the
 * same glued shape.
 *
 * <p>What this pins and what it does not: in sim the dragoon escapes
 * cleanly, so the maneuver physics work - a future run-escalation fix
 * has something working to delegate to, and any sim change that starts
 * gluing here fails loudly. It does NOT reproduce the live bug, and it
 * must not be "fixed" to glue: live #193 died glued where sim escapes,
 * that divergence is tracked in NEXT #190 and owned by Stage A
 * fidelity. The live-side gate is named by the KITE stuck-reason twin
 * (`stuck-no-step` validation-null vs `stuck-order-refused`), whose
 * rows decide the fix.</p>
 */
@Tag("combat")
public class CutoffSwampedRetreatTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void swampedCutoffDragoonBreaksContactInsteadOfDonating() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        dragoon.hp = 120;
        dragoon.shields = 0;
        dragoon.cooldown = 10;

        // Army far away: the test dragoon is detached (>12 tiles).
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 50, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 51, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 52, 20);

        FakeUnit z1 = fakeEnemy(AUnitType.Protoss_Zealot, 19, 20);
        FakeUnit z2 = fakeEnemy(AUnitType.Protoss_Zealot, 21, 20);
        FakeUnit z3 = fakeEnemy(AUnitType.Protoss_Zealot, 20, 19);
        FakeUnit z4 = fakeEnemy(AUnitType.Protoss_Zealot, 20, 21);
        FakeUnit[] zealots = new FakeUnit[]{z1, z2, z3, z4};

        int startX = dragoon.position.x();
        int startY = dragoon.position.y();
        int deathTick = -1;

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[]{dragoon, a, b, c}, zealots, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 400; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);
                    }
                    ZombiePolicies.chaseNearest(
                        Arrays.asList(zealots),
                        Collections.singletonList(dragoon));
                });
                if (dragoon.hp <= 0 && deathTick < 0) deathTick = i;
            }

            double displacement = Math.hypot(
                dragoon.position.x() - startX,
                dragoon.position.y() - startY) / TILE;
            assertTrue(displacement >= 8.0,
                "cutoff dragoon must displace instead of donating itself glued in place: "
                    + displacement + " tiles"
                    + (deathTick >= 0 ? ", died tick " + deathTick : ", survived"));
        } finally {
            Alpha.get().removeUnit(dragoon);
            Alpha.get().removeUnit(a);
            Alpha.get().removeUnit(b);
            Alpha.get().removeUnit(c);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }
}
