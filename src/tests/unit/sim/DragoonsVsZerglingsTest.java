package tests.unit.sim;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.unit.AbstractTestWithUnits;
import tests.fakes.FakeUnit;
import tests.fakes.FakeOrderSink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

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
        // A lone wounded dragoon cannot outrun lings (5.49 > 5.0 px/f) and
        // cannot out-DPS them before they chew through 50 hp, so survival
        // is NOT asserted here - T2 covers surviving as a group. What this
        // test pins is the decision: move orders away from the threat and
        // real displacement while still trading shots, never hold-and-die.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.hp = 50;
        dragoon.shields = 0;
        dragoon.cooldown = 15;

        List<FakeUnit> lings = new ArrayList<>();
        double[][] ring = { { 28, 20 }, { 32, 20 } };
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
        // Band matters: all three under the 80hp pinch line (wounded),
        // healthy enough that 4 lings cannot eat them before focus fire
        // (about a ling per 2 volleys) clears the pressers.
        FakeUnit[] dragoons = new FakeUnit[] {
            woundedDragoon(30, 20, 70),
            woundedDragoon(29, 21, 75),
            woundedDragoon(31, 19, 75),
        };
        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][] { { 36, 19 }, { 36, 20 }, { 36, 21 }, { 37, 20 } }) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }

        double startCentroidX = centroidX(dragoons);
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(dragoons, lings.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            final boolean[] withdrew = new boolean[] { false };
            for (int i = 0; i < 300; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptLings(lings, dragoons);
                });
                if (centroidX(dragoons) < startCentroidX - TILE) withdrew[0] = true;
            }

            for (FakeUnit dragoon : dragoons) {
                assertTrue(dragoon.hp > 0, "no dragoon left behind to die");
            }
            assertTrue(world.shotsFired() > 0,
                "kite or wipe, the guns must speak - silent guns prove nothing");
            assertTrue(withdrew[0],
                "sustained melee pressure must move the group west at some point");
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
    // T2b: the wipe branch, determined (healthy trio vs few lings).
    // T2 above stays branch-agnostic on purpose: with live lings it
    // must withdraw, with dead ones it must have shot. Here there is
    // nowhere to withdraw to - the trio stands and trades.
    // =========================================================

    @Test
    public void healthyTrioWipesPressingLings() {
        FakeUnit[] dragoons = new FakeUnit[] {
            woundedDragoon(28, 20, 100),
            woundedDragoon(29, 21, 100),
            woundedDragoon(29, 19, 100),
        };
        for (FakeUnit dragoon : dragoons) dragoon.cooldown = 0;
        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][] { { 34, 20 }, { 34, 21 } }) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }
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

            assertTrue(world.aliveEnemies() == 0,
                "two lings pressing a healthy trio die - the wipe branch");
            for (FakeUnit dragoon : dragoons) {
                assertTrue(dragoon.hp > 0, "wipe costs no dragoon here");
            }
            assertTrue(world.shotsFired() > 0, "a wipe without shots is a contradiction");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    // =========================================================
    // T-spread: pinch A/B with standoff (NEXT #159 without a map).
    // Same scenario twice, one knob different: pinch yielding on
    // (default DragoonPinchMeleeCount) vs off (count 99, never
    // triggered). Deterministic sim, so the comparison is exact -
    // stronger than Wine N=5+5 with randomized confounders.
    // =========================================================

    @Test
    public void pinchWithStandoffDecidesOnShield() {
        int[] withoutPinch = runSpreadDuel(true);
        int[] withPinch = runSpreadDuel(false);

        // Pinch was actually engageable in both arms (pocket pressure
        // observed on live wounded dragoons) - without this the identity
        // below would be vacuous.
        assertTrue(withoutPinch[4] >= 2,
            "standoff must pocket at some point, else pinch can never fire");
        assertTrue(withPinch[4] >= 2,
            "standoff must pocket at some point, else pinch can never fire");
        // Verdict (sim, deterministic): identical outcomes either way -
        // same survivors, same hp, same kills. Combined with Wine N=5+5
        // (OFF slightly better on contact): no evidence pinch ever helps.
        // If this fails after a behavior change, re-evaluate NEXT #159.
        assertEquals(withoutPinch[0], withPinch[0], "survivors");
        assertEquals(withoutPinch[1], withPinch[1], "dragoon hp left");
        assertEquals(withoutPinch[2], withPinch[2], "lings killed");
    }

    /**
     * @return {dragoons alive, dragoon hp, lings alive, shots fired, max pocket pressure}.
     */
    private static int[] runSpreadDuel(boolean disablePinch) {
        FakeUnit[] dragoons = new FakeUnit[] {
            woundedDragoon(12, 20, 75),
            woundedDragoon(11, 21, 75),
            woundedDragoon(13, 19, 75),
        };
        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][] { { 18, 20 }, { 18, 19 }, { 18, 21 },
                { 19, 20 }, { 19, 21 } }) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }
        SimArena arena = new SimArena(40 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(dragoons, lings.toArray(new FakeUnit[0]), arena);
        atlantis.battle.kite.CombatTuning previous = atlantis.battle.kite.CombatTuning.current();
        if (disablePinch) {
            atlantis.battle.kite.CombatTuning.install(
                atlantis.battle.kite.CombatTuning.defaults().withDragoonPinchMeleeCount(99));
        }
        final int[] maxPinchPressure = new int[1];
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            for (int i = 0; i < 400; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptLings(lings, dragoons);
                });
                for (FakeUnit dragoon : dragoons) {
                    if (dragoon.hp <= 0 || dragoon.hp + dragoon.shields >= 80) continue;
                    int pressing = 0;
                    for (FakeUnit ling : lings) {
                        if (ling.hp > 0 && dragoon.distTo(ling) <= 2.0) pressing++;
                    }
                    if (pressing > maxPinchPressure[0]) maxPinchPressure[0] = pressing;
                }
            }
            int alive = 0;
            int hp = 0;
            for (FakeUnit dragoon : dragoons) {
                if (dragoon.hp > 0) {
                    alive++;
                    hp += (int) dragoon.hp;
                }
            }
            int lingsAlive = world.aliveEnemies();
            return new int[] { alive, hp, lingsAlive, world.shotsFired(), maxPinchPressure[0] };
        }
        finally {
            atlantis.battle.kite.CombatTuning.install(previous);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }


    // =========================================================
    // T-contact: the Wine shape in the sim - pocket from the start.
    // Standoff (above) never pockets the dragoons, so pinch never
    // fires there; here lings start at 2-3 tiles and the pocket is
    // immediate. Compares WITH vs WITHOUT on identical ground.
    // =========================================================

    @Test
    public void pinchWithContactDecidesOnShield() {
        int[] withoutPinch = runContactDuel(true);
        int[] withPinch = runContactDuel(false);

        // Instant contact must pocket: if maxPinchPressure stayed < 2
        // the scenario failed to create the pocket, not the rule.
        assertTrue(withoutPinch[4] >= 2 && withPinch[4] >= 2,
            "contact must pocket wounded dragoons, else pinch can never fire");
        assertTrue(withoutPinch[3] > 0 && withPinch[3] > 0,
            "both arms must actually trade shots");
        // Verdict (sim, deterministic): same survivors, same hp, same
        // kills either way - even point-blank, yielding changes nothing
        // vs standing to trade. Same conclusion as the standoff arms.
        assertEquals(withoutPinch[0], withPinch[0], "survivors");
        assertEquals(withoutPinch[1], withPinch[1], "dragoon hp left");
        assertEquals(withoutPinch[2], withPinch[2], "lings killed");
    }

    private static int[] runContactDuel(boolean disablePinch) {
        FakeUnit[] dragoons = new FakeUnit[] {
            woundedDragoon(12, 20, 75),
            woundedDragoon(11, 21, 75),
            woundedDragoon(13, 19, 75),
        };
        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][] { { 14, 20 }, { 14, 19 }, { 14, 21 },
                { 15, 20 }, { 15, 19 }, { 15, 21 } }) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }
        SimArena arena = new SimArena(40 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(dragoons, lings.toArray(new FakeUnit[0]), arena);
        atlantis.battle.kite.CombatTuning previous = atlantis.battle.kite.CombatTuning.current();
        if (disablePinch) {
            atlantis.battle.kite.CombatTuning.install(
                atlantis.battle.kite.CombatTuning.defaults().withDragoonPinchMeleeCount(99));
        }
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            final int[] maxPinchPressure = new int[1];
            for (int i = 0; i < 350; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptLings(lings, dragoons);
                });
                for (FakeUnit dragoon : dragoons) {
                    if (dragoon.hp <= 0 || dragoon.hp + dragoon.shields >= 80) continue;
                    int pressing = 0;
                    for (FakeUnit ling : lings) {
                        if (ling.hp > 0 && dragoon.distTo(ling) <= 2.0) pressing++;
                    }
                    if (pressing > maxPinchPressure[0]) maxPinchPressure[0] = pressing;
                }
            }
            int alive = 0;
            int hp = 0;
            for (FakeUnit dragoon : dragoons) {
                if (dragoon.hp > 0) {
                    alive++;
                    hp += (int) dragoon.hp;
                }
            }
            return new int[] { alive, hp, world.aliveEnemies(), world.shotsFired(), maxPinchPressure[0] };
        }
        finally {
            atlantis.battle.kite.CombatTuning.install(previous);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    // =========================================================
    // T-gate: the pinch knob is actually wired to the decision.
    // Quarry sits at 5 tiles (margin holds there either way); two
    // flankers make it a pocket. WITH pinch (count 2) the rule
    // yields; WITHOUT (count 99) it holds - proving the knob under
    // test is the one the A/B arms differ in.
    // =========================================================

    @Test
    public void pinchGateTogglesYieldVsHold() {
        FakeUnit dragoon = woundedDragoon(30, 20, 70);
        dragoon.cooldown = 20;
        FakeUnit quarry = fake(AUnitType.Zerg_Zergling, 35, 20);
        quarry.setEnemy();
        FakeUnit f1 = fake(AUnitType.Zerg_Zergling, 28.5, 20);
        f1.setEnemy();
        FakeUnit f2 = fake(AUnitType.Zerg_Zergling, 28.5, 21);
        f2.setEnemy();
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { dragoon }, new FakeUnit[] { quarry, f1, f2 }, arena);
        atlantis.battle.kite.CombatTuning previous = atlantis.battle.kite.CombatTuning.current();
        try {
            BattleCommander.clearForTests();
            atlantis.battle.kite.CombatTuning.install(
                atlantis.battle.kite.CombatTuning.defaults());
            assertTrue(atlantis.battle.kite.Kite.shouldKite(dragoon, quarry),
                "pinch on: hurt + 2 melee inside 2 tiles yields");
            atlantis.battle.kite.CombatTuning.install(
                atlantis.battle.kite.CombatTuning.defaults().withDragoonPinchMeleeCount(99));
            assertFalse(atlantis.battle.kite.Kite.shouldKite(dragoon, quarry),
                "pinch off: 5 tiles out the margin holds, no stand (only 2 melee)");
        }
        finally {
            atlantis.battle.kite.CombatTuning.install(previous);
            world.close();
            BattleCommander.clearForTests();
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

            atlantis.battle.formation.CoverWithdraw.Step step =
                atlantis.battle.formation.CoverWithdraw.makeWayStep(helper);
            assertTrue(step != null,
                "helper stands on the escape ray: it must step aside");
            assertTrue(step.escaper == escaper,
                "the step names whose ray it clears");
            assertTrue(Math.abs(step.dest.y() - helper.position.y()) > 1,
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
        // Escaper hurt but not doomed; two quarry lings keep the pressure
        // on IT (not on the holders) so the corridor stays the story.
        FakeUnit wounded = woundedDragoon(34, 20, 50);
        FakeUnit holderA = fake(AUnitType.Protoss_Dragoon, 33, 20);
        FakeUnit holderB = fake(AUnitType.Protoss_Dragoon, 33, 21);
        FakeUnit[] ours = new FakeUnit[] { wounded, holderA, holderB };
        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][] { { 36, 20 }, { 37, 21 } }) {
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
                    scriptLings(lings, wounded);
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
            assertTrue(atlantis.util.log.EventLog.toJsonLines().contains("\"MAKE_WAY\""),
                "the live step-aside rows itself for the game proof");
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

            atlantis.battle.formation.CoverWithdraw.Cover cover =
                atlantis.battle.formation.CoverWithdraw.coverTarget(healthy);
            assertTrue(cover != null && cover.pursuer == pursuer,
                "cover names the pursuer chewing the wounded, not the nearer ling");
            assertTrue(cover.escaper == wounded,
                "cover names on whose behalf it fires");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
        }
    }

    @Test
    public void healthyCoversPursuerInLiveFight() {
        // Cover usually finishes a pursuer the fight already wounded:
        // one dragoon shot kills it, so the healthy saves the wounded
        // in time. The distractor stands untouched at 2.5 tiles (nearer
        // than the pursuer) to prove selection, not proximity, decides.
        FakeUnit wounded = woundedDragoon(32, 20, 30);
        FakeUnit healthy = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit third = fake(AUnitType.Protoss_Dragoon, 20, 26);
        FakeUnit[] ours = new FakeUnit[] { wounded, healthy, third };
        FakeUnit pursuer = fake(AUnitType.Zerg_Zergling, 33, 20);
        pursuer.hp = 10;
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
                    distractor.lastCommand = "Stop";
                });
                if (wounded.hp <= 0) break;
            }

            assertTrue(wounded.hp > 0, "covered withdrawal keeps the wounded alive");
            assertTrue(hasOrder(ordersOf(healthy), healthy.id(), "attackUnit"),
                "healthy issues attack orders, not idle watching");
            assertTrue(atlantis.util.log.EventLog.toJsonLines().contains("\"COVER\""),
                "the live cover decision rows itself for the game proof");
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

    // =========================================================
    // T-hunt: lost contact hunts the last known point (1Drag_v_1Zeal:
    // brief contact at 0:12, then 9 min idle 60 tiles apart - neither
    // side re-acquires). Gated to baseless maps + beyond the 9-tile
    // squad envelope, so focus refusals inside it are never overruled
    // and based maps keep their formation behavior (negative control).
    // No UMS check on purpose: the gate is contact memory (lastTarget
    // exists only after a real fight) plus baselessness.
    // =========================================================

    @Test
    public void lostContactHuntsLastKnownPosition() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(new FakeUnit[] { dragoon }, new FakeUnit[] { ling }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            // Phase 1: visible fight - the commander snapshots the contact
            // point while it is still visible (no manual memory setup: this
            // is the whole point, the snapshot must form on its own).
            for (int i = 0; i < 5; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            assertTrue(ling.hp > 0, "setup: the quarry must survive phase 1");

            // Phase 2: separated after contact (the live 1Drag shape) and
            // contact lost beyond the squad envelope.
            world.teleport(dragoon, APosition.create(15, 20));
            world.teleport(ling, APosition.create(55, 20));
            dragoon.lastCommand = "Stop";
            dragoon.target = null;
            dragoon.targetPosition = null;
            ordersOf(dragoon).clear();
            assertTrue(dragoon.distTo(ling) > 9, "setup: contact truly lost");
            for (int i = 0; i < 200; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            assertTrue(dragoon.distTo(APosition.create(32, 20)) < 1.5 * TILE,
                "dragoon must march the last engaged point, not sit: " + dragoon.position);
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "move"), "hunt issues moves");
        }
        finally {
            world.close();
        }
    }

    @Test
    public void noHuntOnBasedMaps() {
        // Negative control: same lost contact, but a base exists FAR from
        // the fight (outside the 8-tile breach radius - this is about the
        // baseless gate, not base defense). The formation fallback holds,
        // no cross-map hunting in macro games.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 5, 5);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(new FakeUnit[] { nexus, dragoon }, new FakeUnit[] { ling }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 5; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            world.teleport(dragoon, APosition.create(15, 20));
            world.teleport(ling, APosition.create(55, 20));
            dragoon.lastCommand = "Stop";
            dragoon.target = null;
            dragoon.targetPosition = null;
            ordersOf(dragoon).clear();
            double anchorX = dragoon.position.x();
            double anchorY = dragoon.position.y();
            for (int i = 0; i < 200; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            assertTrue(
                Math.hypot(dragoon.position.x() - anchorX, dragoon.position.y() - anchorY) < TILE,
                "based maps keep the formation fallback, no cross-map hunting");
        }
        finally {
            world.close();
        }
    }

    // =========================================================
    // T-duel: 1v1 dragoon vs chasing zealot (NEXT #169: Wine takes 10
    // hits in a WIN, target under 2). The zealot chases like the map AI:
    // attack-move at the dragoon, no perfect prediction. The dragoon
    // must win while the DuelKite standoff keeps the hits down.
    // =========================================================

    @Test
    public void duelVsChasingZealot() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 6, 6);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 13, 6);
        zealot.setEnemy();

        SimArena arena = new SimArena(20 * TILE, 12 * TILE);
        SimWorld world = new SimWorld(new FakeUnit[] { dragoon }, new FakeUnit[] { zealot }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 600; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptChaser(zealot, dragoon);
                });
                if (zealot.hp <= 0) break;
            }
            assertTrue(zealot.hp <= 0, "the dragoon wins the duel");
            int taken = (180 - (int) dragoon.hp) + (80 - dragoon.shields);
            assertTrue(taken <= 48, "standoff keeps the hits down, taken=" + taken
                + " (measured 40 on the DuelKite rule; the live Wine target is <2 hits, NEXT #169)");
        }
        finally {
            world.close();
        }
    }

    // =========================================================
    // T-run: a probe flees a chasing zealot through the running
    // managers (NEXT #172, second consumer). The probe outruns the
    // zealot (4.92 vs 4.0), so survival means the escape chain issued
    // somewhere away instead of freezing or circling back.
    // =========================================================

    @Test
    public void probeOutrunsChaserThroughRunningManagers() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 10, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 14, 20);
        zealot.setEnemy();
        double startDist = probe.distTo(zealot);

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(new FakeUnit[] { probe }, new FakeUnit[] { zealot }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 400; i++) {
                world.tick(() -> {
                    if (probe.hp > 0) {
                        probe.runningManager().runFrom(
                            zealot, 4, atlantis.units.actions.Actions.RUN_ENEMY, false);
                    }
                    scriptChaser(zealot, probe);
                });
                if (zealot.hp <= 0 || probe.hp <= 0) break;
            }
            assertTrue(probe.hp > 0, "the faster probe escapes the chase");
            assertTrue(probe.distTo(zealot) > startDist,
                "distance opened instead of circling back: " + probe.distTo(zealot) + " vs " + startDist);
        }
        finally {
            world.close();
        }
    }

    /** Map-AI-like chaser: face the quarry, move into range, trade. */
    private static void scriptChaser(FakeUnit foe, FakeUnit dragoon) {
        if (foe.hp <= 0 || dragoon.hp <= 0) {
            foe.lastCommand = "Stop";
            return;
        }
        foe.setAngle(Math.atan2(
            dragoon.position.y() - foe.position.y(), dragoon.position.x() - foe.position.x()));
        if (foe.distTo(dragoon) <= SimWorld.reachTiles(foe, dragoon)) {
            foe.lastCommand = "AttackUnit";
            foe.target = dragoon;
            foe.targetPosition = dragoon.position;
        }
        else {
            foe.lastCommand = "Move";
            foe.target = dragoon;
            foe.targetPosition = dragoon.position;
        }
    }

    // =========================================================
    // T-ghost: an engaged target that goes invisible and stays that way
    // stops pinning the frame (NEXT #163 follow-up, live 1Drag: 9 min of
    // shot-fired at an unhittable ghost, zero damage both sides). Grace
    // covers fog flicker; a stale ghost is dropped, and the hunt takes
    // over when the last contact is behind the squad envelope.
    // =========================================================

    @Test
    public void staleInvisibleTargetIsDroppedForHunt() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        ling.setEnemy();

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(new FakeUnit[] { dragoon }, new FakeUnit[] { ling }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            // Phase 1: visible fight, snapshot forms.
            for (int i = 0; i < 5; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            assertTrue(ling.hp > 0, "setup: quarry survives contact");

            // Phase 2: contact lost AND out of sight (fog, not death) - but
            // still in range, so only visibility decides.
            world.teleport(dragoon, APosition.create(52, 20));
            world.teleport(ling, APosition.create(55, 20));
            ling.isVisibleUnitOnMap = false;
            dragoon.lastCommand = "Stop";
            dragoon.targetPosition = null;
            ordersOf(dragoon).clear();
            for (int i = 0; i < 10; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            assertTrue(atlantis.battle.targeting.StickyTarget.currentTarget(dragoon) == ling,
                "fog flicker inside the grace window keeps the commitment");
            for (int i = 0; i < 60; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            assertTrue(atlantis.battle.targeting.StickyTarget.currentTarget(dragoon) == null,
                "a long-unseen target in range still drops (visibility, not distance)");
            // Phase 3: far away and long unseen - the ghost drops, the hunt takes over.
            world.teleport(dragoon, APosition.create(15, 20));
            dragoon.lastCommand = "Stop";
            dragoon.targetPosition = null;
            ordersOf(dragoon).clear();
            for (int i = 0; i < 120; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                });
            }
            assertTrue(atlantis.battle.targeting.StickyTarget.currentTarget(dragoon) == null,
                "a stale ghost stops pinning the frame");
            assertTrue(hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "dropped ghost hands over to the hunt: " + ordersOf(dragoon));
            assertTrue(dragoon.distTo(APosition.create(32, 20)) < 5 * TILE,
                "marching the last contact, not shooting a ghost: " + dragoon.position);
        }
        finally {
            world.close();
        }
    }

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
            // Same reach rule as the sim engine (SimWorld.reachTiles):
            // weapon range plus the target's body, so sub-tile melee
            // weapons can actually connect instead of chasing forever.
            if (best <= SimWorld.reachTiles(ling, nearest)) {
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

    // =========================================================
    // T-collapse: the bait pulled the lings into the funnel and the line
    // crushes 4v2 (NEXT #170). The line joins the bait at the funnel
    // mouth instead of standing the standoff, then wipes the pack.
    // =========================================================

    @Test
    public void collapseWipesBaitedPack() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] dragoons = new FakeUnit[] {
            fake(AUnitType.Protoss_Dragoon, 27, 29),
            fake(AUnitType.Protoss_Dragoon, 28, 29),
            fake(AUnitType.Protoss_Dragoon, 29, 29),
            fake(AUnitType.Protoss_Dragoon, 30, 29),
        };
        FakeUnit lingA = fake(AUnitType.Zerg_Zergling, 28, 10);
        FakeUnit lingB = fake(AUnitType.Zerg_Zergling, 29, 10);
        lingA.setEnemy();
        lingB.setEnemy();

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { nexus, dragoons[0], dragoons[1], dragoons[2], dragoons[3] },
            new FakeUnit[] { lingA, lingB }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 500; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        if (dragoon.hp > 0) {
                            BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                        }
                    }
                    scriptHoldMouth(lingA, dragoons);
                    scriptHoldMouth(lingB, dragoons);
                });
                if (lingA.hp <= 0 && lingB.hp <= 0) break;
            }
            assertTrue(lingA.hp <= 0 && lingB.hp <= 0, "the collapsed line wipes the baited pack");
            int alive = 0;
            for (FakeUnit dragoon : dragoons) if (dragoon.hp > 0) alive++;
            assertTrue(alive >= 3, "crushing collapse trades up, alive=" + alive);
        }
        finally {
            world.close();
        }
    }

    /** Baited-in defender: holds the mouth, trades only in reach (no chasing out). */
    private static void scriptHoldMouth(FakeUnit ling, FakeUnit[] dragoons) {
        if (ling.hp <= 0) {
            ling.lastCommand = "Stop";
            return;
        }
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
        if (nearest != null && best <= SimWorld.reachTiles(ling, nearest)) {
            ling.lastCommand = "AttackUnit";
            ling.target = nearest;
            ling.targetPosition = nearest.position;
        }
        else {
            ling.lastCommand = "Stop";
            ling.target = null;
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
