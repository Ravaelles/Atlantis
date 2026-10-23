package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.battle.BattleCommander;
import atlantis.battle.FightTarget;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner rules from the Steamhammer loss (two Dragoons died shooting
 * nothing, glued on a Gateway; user: never flee into a building, keep
 * 1.5 tiles, and disengage faster with 3+ melee in 2.5):
 * (a) kite steps keep building clearance end to end, never glue;
 * (b) a swamped Dragoon (3+ melee in 2.5) takes the full gap, not
 * the capped 2.0 step.
 */
@Tag("combat")
public class DragoonSwampTest extends WorldStubForTests {

    private static FakeUnit dragoon(double tx, double ty) {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, tx, ty);
        d.hp = 120;
        d.shields = 80;
        d.cooldown = 0;

        return d;
    }

    private static FakeUnit ling(double tx, double ty) {
        FakeUnit l = fake(AUnitType.Zerg_Zergling, tx, ty);
        l.setEnemy();
        l.hp = 35;

        return l;
    }

    private static List<double[]> moveDests(FakeUnit u) {
        List<double[]> out = new ArrayList<>();
        Pattern p = Pattern.compile("\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
        for (FakeOrderSink.Record r : ((FakeOrderSink) u.orderSink()).orders()) {
            if (!r.operation.equals("move")) continue;
            Matcher m = p.matcher(r.detail);
            if (m.find()) out.add(new double[]{Integer.parseInt(m.group(1)) / 32.0, Integer.parseInt(m.group(2)) / 32.0});
        }

        return out;
    }

    // =========================================================
    // Seam pins: step length numbers without running frames.
    // =========================================================

    @Test
    public void swampedDragoonTakesTheFullGap() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = dragoon(30, 20);
        d.hp = 40;
        d.shields = 0;
        FakeUnit t = ling(31, 20);
        FakeUnit a = ling(29, 21);
        FakeUnit b = ling(31, 21);
        FakeUnit c = ling(30, 19);

        world(3, fakeOurs(nexus, d), fakeEnemies(t, a, b, c), () -> {
            if (atlantis.game.A.now() != 1) return;

            double len = FightTarget.kiteStepLength(d, t);
            assertTrue(len > 2.5,
                "wounded + 3+ melee in 2.5: break contact with the full gap, not the 2.0 cap: " + len);
        });
    }

    @Test
    public void lonePresserKeepsTheMeasuredCap() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = dragoon(30, 20);
        FakeUnit t = ling(31, 20);

        world(3, fakeOurs(nexus, d), fakeEnemies(t), () -> {
            if (atlantis.game.A.now() != 1) return;

            double len = FightTarget.kiteStepLength(d, t);
            assertTrue(len <= 2.01,
                "open ground keeps the #152-measured 2.0 cap: " + len);
        });
    }

    @Test
    public void swampedRuleIsDragoonOnly() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit z = fake(AUnitType.Protoss_Zealot, 30, 20);
        z.hp = 100;
        z.cooldown = 0;
        FakeUnit t = ling(31, 20);
        FakeUnit a = ling(29, 21);
        FakeUnit b = ling(31, 21);

        world(3, fakeOurs(nexus, z), fakeEnemies(t, a, b), () -> {
            if (atlantis.game.A.now() != 1) return;

            double len = FightTarget.kiteStepLength(z, t);
            assertTrue(len <= 2.01,
                "zealots withdraw on their own rule - the long step is dragoon-only: " + len);
        });
    }

    // =========================================================
    // Sim: forced kite steps with a Gateway in the escape path -
    // clearance on every step, still displaces (no glue).
    // =========================================================

    @Test
    public void kitingAroundGatewayKeepsClearanceAndDisplaces() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 40, 20);
        gateway.hp = 500;
        FakeUnit d = dragoon(36, 20);
        // Spent weapon: the dragoon must kite, not trade. Lings west,
        // Gateway east - the escape runs at the wall.
        d.cooldown = 25;
        List<FakeUnit> foes = new ArrayList<>();
        foes.add(ling(33, 20));
        foes.add(ling(33, 21));

        SimArena arena = new SimArena(70 * 32, 40 * 32);
        SimWorld world = new SimWorld(
            new FakeUnit[] { nexus, gateway, d }, foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            double sx = d.x() / 32.0, sy = d.y() / 32.0;
            double maxDisp = 0;
            for (int i = 0; i < 150; i++) {
                // Anchor null: no formation-hold approach game, the contact
                // veto owns the unit and the kite footwork must show.
                world.tick(() -> {
                    d.cooldown = Math.max(d.cooldown, 20);
                    if (d.hp > 0) BattleCommander.act(d, FightOrFlight.Tactic.FIGHT, null, d);
                    for (FakeUnit f : foes) {
                        if (f.hp <= 0) continue;
                        FakeUnit n = d.hp > 0 ? d : null;
                        f.lastCommand = "AttackUnit";
                        f.target = n;
                        if (n != null) f.targetPosition = n.position;
                    }
                });
                if (d.hp > 0 && d.position() != null) {
                    double dd = Math.hypot(d.x() / 32.0 - sx, d.y() / 32.0 - sy);
                    if (dd > maxDisp) maxDisp = dd;
                }
                if (d.hp <= 0) break;
            }

            String log = atlantis.util.log.EventLog.toJsonLines();
            int stepped = 0, nowhere = 0, idx = 0;
            while ((idx = log.indexOf("\"stepped-aside\"", idx)) >= 0) { stepped++; idx++; }
            idx = 0;
            while ((idx = log.indexOf("nowhere-to-step", idx)) >= 0) { nowhere++; idx++; }
            System.err.println("DIAG kite steppedAside=" + stepped + " nowhere=" + nowhere);
            assertTrue(maxDisp > 2.0,
                "the dragoon displaces instead of gluing on the wall: " + maxDisp);
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }
}
