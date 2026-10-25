package tests.unit.sim;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.BattleCommander;
import atlantis.combat.FormationHold;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Holder unstick on the fast behavior sim (NEXT #182): a holder whose
 * move queue is wedged (orders ride, zero displacement) gets STOP once
 * and a fresh queue next frame; healthy holders marching at their slots
 * never trip it. The kite-loop unstick can never fire here - holders
 * take no kite steps - so without this watch a wedged holder stands
 * into chews until death (ZealDrag_v_Zeal game 144553: 300 frames,
 * zero displacement).
 *
 * <p>Wedge vehicle: the sim moves ordered units, so teleporting the
 * victim back to its pin after every tick simulates the stuck engine
 * queue exactly - move orders issued, position never changes.</p>
 */
@Tag("combat")
public class HolderUnstickSimTest extends AbstractTestWithUnits {
    private static final int TILE = 32;

    @Test
    public void wedgedHolderGetsStopWhileHealthyHoldersMarch() {
        FakeUnit victim = mk(20, 20);
        FakeUnit healthyA = mk(20, 22);
        FakeUnit healthyB = mk(20, 18);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 38, 20);
        foe.setEnemy();
        FakeUnit[] ours = new FakeUnit[] { victim, healthyA, healthyB };

        APosition victimPin = victim.position;
        APosition healthyPinA = healthyA.position;
        APosition healthyPinB = healthyB.position;

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(ours, new FakeUnit[] { foe }, arena);
        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 80; i++) {
                world.tick(() -> {
                    APosition anchor = APosition.create(60, 20);
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        FormationHold.holdIfCalm(u, foe, FightOrFlight.Tactic.FIGHT, victim, anchor);
                    }
                });
                // Wedged queue: the engine never applies the victim's orders.
                world.teleport(victim, victimPin);
            }

            assertTrue(stopsOf(victim) >= 1,
                "wedged holder gets STOP once the watch trips, stops were " + stopsOf(victim));
            assertEquals(0, stopsOf(healthyA),
                "a holder that displaces is marching, not wedged - never STOP it");
            assertEquals(0, stopsOf(healthyB),
                "a holder that displaces is marching, not wedged - never STOP it");
            assertTrue(healthyA.distTo(healthyPinA) > 1.0,
                "healthy holder A marched at its slot, displaced " + healthyA.distTo(healthyPinA));
            assertTrue(healthyB.distTo(healthyPinB) > 1.0,
                "healthy holder B marched at its slot, displaced " + healthyB.distTo(healthyPinB));
            assertTrue(EventLog.toJsonLines().contains("\"verdict\":\"UNSTICK\""),
                "the twin names the remedy, got: " + EventLog.toJsonLines());
        }
        finally {
            for (FakeUnit u : ours) Alpha.get().removeUnit(u);
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
        }
    }

    private static int stopsOf(FakeUnit unit) {
        int stops = 0;
        for (FakeOrderSink.Record r : ((FakeOrderSink) unit.orderSink()).orders()) {
            if (r.actorId == unit.id() && r.operation.equals("stop")) stops++;
        }
        return stops;
    }

    private static FakeUnit mk(double tx, double ty) {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, tx, ty);
        d.hp = 180;
        d.shields = 80;
        d.cooldown = 0;

        return d;
    }
}
