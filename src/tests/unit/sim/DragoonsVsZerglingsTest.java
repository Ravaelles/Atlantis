package tests.unit.sim;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeOrderSink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Dragoons vs Zerglings on the fast behavior sim (NEXT #161): the whole
 * point of StarEngine. Controlled counts, controlled map, real engine
 * data (top speed / cooldown / range / damage), scripted enemies, REAL
 * decision code on our side ({@code BattleCommander.act}).
 *
 * <p>T1: a surrounded wounded dragoon flees instead of holding and
 * dying. T2: wounded dragoons withdraw as a group and survive. T3: the
 * sim mover itself, through an obstacle course (scripted orders, no
 * decisions - this one tests movement, not micro).
 */
@Tag("combat")
public class DragoonsVsZerglingsTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    // =========================================================
    // T0: the sim runs on real engine data. Probe-verified
    // (Dragoon 5.0/30/128px, Zergling 5.49/8/15px, Zealot 4.0/22/15px):
    // if the binding ever reports otherwise, these fail loudly instead
    // of silently simulating a different game.
    // =========================================================

    @Test
    public void engineDataPins() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 12, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 14, 10);

        assertEquals(5.0, dragoon.maxSpeed(), 0.001, "dragoon top speed, px/frame");
        assertEquals(30, dragoon.cooldownAbsolute(), "dragoon cooldown, frames");
        assertEquals(5.49, ling.maxSpeed(), 0.001, "ling top speed, px/frame");
        assertEquals(8, ling.cooldownAbsolute(), "ling cooldown, frames");
        assertEquals(4.0, zealot.maxSpeed(), 0.001, "zealot top speed, px/frame");
        assertEquals(22, zealot.cooldownAbsolute(), "zealot cooldown, frames");
    }

    // =========================================================
    // T1: surrounded wounded dragoon flees and lives.
    // =========================================================

    @Test
    public void surroundedWoundedDragoonFleesInsteadOfHolding() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.hp = 30;
        dragoon.shields = 0;
        dragoon.cooldown = 15;

        List<FakeUnit> lings = new ArrayList<>();
        double[][] ring = { { 28, 20 }, { 32, 20 }, { 30, 18 }, { 30, 22 } };
        for (double[] spot : ring) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { dragoon }, lings.toArray(new FakeUnit[0]), arena);
        try {
            double startX = dragoon.position.x();
            double startY = dragoon.position.y();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 250; i++) {
                world.tick(() -> {
                    BattleCommander.act(
                        dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    scriptLings(lings, dragoon);
                });
                if (dragoon.hp <= 0) break;
            }

            assertTrue(dragoon.hp > 0,
                "wounded surrounded dragoon must withdraw, not die in the pocket");
            double fledPx = Math.hypot(
                dragoon.position.x() - startX, dragoon.position.y() - startY);
            assertTrue(fledPx > 2 * TILE,
                "dragoon must gain ground from the pocket, fled only " + fledPx + "px");
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "kite must issue move steps, not hold-and-shoot: " + ordersOf(dragoon));
            assertTrue(world.shotsFired() > 0,
                "a kite that never shoots is just running - trade while yielding");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    // =========================================================
    // T2: wounded dragoons withdraw as a group and survive.
    // =========================================================

    @Test
    public void woundedDragoonsWithdrawAndSurvive() {
        FakeUnit[] dragoons = new FakeUnit[] {
            woundedDragoon(30, 20, 40),
            woundedDragoon(29, 21, 50),
            woundedDragoon(31, 19, 60),
        };
        List<FakeUnit> lings = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, 36, 17 + i);
            ling.setEnemy();
            lings.add(ling);
        }

        double startCentroidX = centroidX(dragoons);
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(dragoons, lings.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 300; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptLings(lings, dragoons);
                });
            }

            for (FakeUnit dragoon : dragoons) {
                assertTrue(dragoon.hp > 0, "no dragoon left behind to die");
            }
            if (world.aliveEnemies() == 0) {
                assertTrue(world.shotsFired() > 0,
                    "threat gone because it died - the fight must have happened");
            }
            else {
                assertTrue(world.shotsFired() > 0,
                    "withdrawing without shooting is just running - trade while yielding");
                assertTrue(centroidX(dragoons) < startCentroidX - TILE,
                    "with live lings pressing, the group must yield ground west");
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    // =========================================================
    // T3: obstacle course for the mover (scripted, no decisions).
    // =========================================================

    @Test
    public void moverReachesThroughGapWithoutPermanentWedge() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 10, 15);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 10, 13);
        FakeUnit[] dragoons = new FakeUnit[] { a, b };

        // Vertical wall with a single gap: x in [18,20], gap y in [12,14].
        SimArena arena = new SimArena(40 * TILE, 30 * TILE)
            .block(18 * TILE, 0, 2 * TILE, 12 * TILE)
            .block(18 * TILE, 14 * TILE, 2 * TILE, 16 * TILE);
        SimWorld world = new SimWorld(dragoons, new FakeUnit[0], arena);
        try {
            for (int i = 0; i < 400; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        dragoon.lastCommand = "Move";
                        dragoon.target = null;
                        dragoon.targetPosition = atlantis.map.position.APosition
                            .createFromPixels(30 * TILE, 13 * TILE);
                    }
                });
                if (arrived(a, 30 * TILE, 13 * TILE)
                    && arrived(b, 30 * TILE, 13 * TILE)) break;
            }

            assertTrue(arrived(a, 30 * TILE, 13 * TILE), "dragoon A reaches through the gap");
            assertTrue(arrived(b, 30 * TILE, 13 * TILE), "dragoon B reaches through the gap");
            assertTrue(world.maxStuckFrames(dragoons) < 120,
                "sliding along the wall is fine, a permanent wedge is not");
        }
        finally {
            world.close();
        }
    }

    // =========================================================
    // T4a-b + T5a-b: formation coordination (NEXT #162). Wounded
    // first rank withdraws, the line helps: make-way opens the
    // corridor, cover takes the pursuer instead of the nearest.
    // =========================================================

    @Test
    public void makeWayStepClearsBlockedCorridor() {
        FakeUnit escaper = woundedDragoon(34, 20, 30);
        FakeUnit pursuer = fake(AUnitType.Zerg_Zergling, 36, 20);
        pursuer.setEnemy();
        FakeUnit helper = fake(AUnitType.Protoss_Dragoon, 32.7, 20);
        FakeUnit third = fake(AUnitType.Protoss_Dragoon, 20, 26);
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { escaper, helper, third },
            new FakeUnit[] { pursuer }, arena);
        try {
            atlantis.battle.BattleCommander.clearForTests();

            atlantis.map.position.APosition step =
                atlantis.battle.formation.CoverWithdraw.makeWayStep(helper);
            assertTrue(step != null,
                "helper stands on the escape ray: it must step aside");
            assertTrue(Math.abs(step.y() - helper.position.y()) > 1,
                "the step is lateral (off the ray), not along it");

            assertTrue(atlantis.battle.formation.CoverWithdraw.makeWayStep(third) == null,
                "far from the corridor: no step, fight on unchanged");
        }
        finally {
            world.close();
            atlantis.battle.BattleCommander.clearForTests();
        }
    }

    @Test
    public void makeWayStepStaysOutOfDuels() {
        FakeUnit escaper = woundedDragoon(34, 20, 30);
        FakeUnit helper = fake(AUnitType.Protoss_Dragoon, 32.7, 20);
        FakeUnit pursuer = fake(AUnitType.Zerg_Zergling, 36, 20);
        pursuer.setEnemy();
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { escaper, helper },
            new FakeUnit[] { pursuer }, arena);
        try {
            atlantis.battle.BattleCommander.clearForTests();
            assertTrue(atlantis.battle.formation.CoverWithdraw.makeWayStep(helper) == null,
                "two units is a duel: frozen per-unit behavior, no sharing");
        }
        finally {
            world.close();
            atlantis.battle.BattleCommander.clearForTests();
        }
    }

    @Test
    public void woundedPassesThroughItsOwnLine() {
        FakeUnit wounded = woundedDragoon(34, 20, 30);
        FakeUnit holderA = fake(AUnitType.Protoss_Dragoon, 33, 20);
        FakeUnit holderB = fake(AUnitType.Protoss_Dragoon, 33, 21);
        FakeUnit[] ours = new FakeUnit[] { wounded, holderA, holderB };
        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][] { { 36, 19 }, { 36, 21 }, { 37, 20 },
                { 37, 19 }, { 35, 22 }, { 35, 18 } }) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(ours, lings.toArray(new FakeUnit[0]), arena);
        final boolean[] sawMakeWay = new boolean[] { false };
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 300; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : ours) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptLings(lings, wounded, holderA, holderB);
                    if (atlantis.battle.formation.CoverWithdraw.makeWayStep(holderA) != null
                        || atlantis.battle.formation.CoverWithdraw.makeWayStep(holderB) != null) {
                        sawMakeWay[0] = true;
                    }
                });
                if (wounded.hp <= 0) break;
            }

            assertTrue(wounded.hp > 0, "the withdrawing dragoon must live");
            assertTrue(wounded.position.x() < 32 * TILE,
                "it passes west through its own line, not wedged on it");
            assertTrue(sawMakeWay[0],
                "the corridor trigger fired live: a holder stood on the escape ray");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    @Test
    public void coverTakesPursuerInsteadOfNearest() {
        FakeUnit wounded = woundedDragoon(32, 20, 30);
        FakeUnit healthy = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit third = fake(AUnitType.Protoss_Dragoon, 20, 26);
        FakeUnit pursuer = fake(AUnitType.Zerg_Zergling, 33, 20);
        pursuer.setEnemy();
        FakeUnit distractor = fake(AUnitType.Zerg_Zergling, 27.5, 20);
        distractor.setEnemy();
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { wounded, healthy, third },
            new FakeUnit[] { pursuer, distractor }, arena);
        try {
            BattleCommander.clearForTests();

            AUnit picked = atlantis.battle.formation.CoverWithdraw.coverTarget(healthy);
            assertTrue(picked == pursuer,
                "cover names the pursuer chewing the wounded, not the nearer ling");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
        }
    }

    @Test
    public void healthyCoversPursuerInLiveFight() {
        FakeUnit wounded = woundedDragoon(32, 20, 30);
        FakeUnit healthy = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit third = fake(AUnitType.Protoss_Dragoon, 20, 26);
        FakeUnit[] ours = new FakeUnit[] { wounded, healthy, third };
        FakeUnit pursuer = fake(AUnitType.Zerg_Zergling, 33, 20);
        pursuer.setEnemy();
        FakeUnit distractor = fake(AUnitType.Zerg_Zergling, 27.5, 20);
        distractor.setEnemy();
        FakeUnit[] foes = new FakeUnit[] { pursuer, distractor };
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(ours, foes, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 250; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : ours) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptLings(java.util.Arrays.asList(pursuer), wounded);
                    scriptLings(java.util.Arrays.asList(distractor), healthy);
                });
                if (wounded.hp <= 0) break;
            }

            assertTrue(wounded.hp > 0, "covered withdrawal keeps the wounded alive");
            assertTrue(hasOrder(ordersOf(healthy), healthy.id(), "attackUnit"),
                "healthy issues attack orders, not idle watching");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    // =========================================================
    // Helpers.
    // =========================================================

    private static FakeUnit woundedDragoon(double tx, double ty, int hp) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, tx, ty);
        dragoon.hp = hp;
        dragoon.shields = 0;
        dragoon.cooldown = 10;
        return dragoon;
    }

    /** Scripted pursuers: nearest live dragoon, move into range, then trade. */
    private static void scriptLings(List<FakeUnit> lings, FakeUnit... dragoons) {
        for (FakeUnit ling : lings) {
            if (ling.hp <= 0) continue;
            FakeUnit nearest = null;
            double best = Double.MAX_VALUE;
            for (FakeUnit dragoon : dragoons) {
                if (dragoon.hp <= 0) continue;
                double dist = ling.distTo(dragoon);
                if (dist < best) {
                    best = dist;
                    nearest = dragoon;
                }
            }
            if (nearest == null) {
                ling.lastCommand = "Stop";
                continue;
            }
            if (best <= ling.weaponRangeAgainst(nearest)) {
                ling.lastCommand = "AttackUnit";
                ling.target = nearest;
                ling.targetPosition = nearest.position;
            }
            else {
                ling.lastCommand = "Move";
                ling.target = nearest;
                ling.targetPosition = nearest.position;
            }
        }
    }

    private static double centroidX(FakeUnit[] dragoons) {
        double sum = 0;
        for (FakeUnit dragoon : dragoons) sum += dragoon.position.x();
        return sum / dragoons.length;
    }

    private static boolean arrived(FakeUnit unit, int x, int y) {
        return Math.hypot(unit.position.x() - x, unit.position.y() - y) < 1.5 * TILE;
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    private static boolean hasOrder(List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }
}
