package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.strategy.BattleStrategies;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.combat.strategy.SquadScout;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Choke doctrine wiring (NEXT #170): with no contact, a BASED line near a
 * contested choke holds the standoff instead of bunching on the leader,
 * and one unit detaches as bait. Geometry is pinned so the contested
 * choke (28,18) is the unit's nearest: dragoons sit 11-13 tiles south of
 * it (inside DETECT_TILES 15 but outside the oursNear 12 ring, so the
 * line holds instead of crushing), the zealot sits 7 tiles north-east of
 * it (inside CONTEST_TILES 12) but 15+ tiles from the dragoons (outside
 * the 9-tile engage envelope and the 15-tile enemiesNear ring, so no
 * fight and no mid-fight bait veto).
 */
@Tag("combat")
public class ChokeStandoffTest extends WorldStubForTests {

    @Test
    public void lineHoldsStandoffInsteadOfBunching() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 14);
        zealot.setEnemy();

        world(5, fakeOurs(nexus, dragoon), fakeEnemies(zealot), () -> {
            for (int i = 0; i < 5; i++) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
            }
            APosition choke = dragoon.nearestChoke().center();
            APosition dest = dragoon.targetPosition;
            assertNotNull(dest, "holds somewhere instead of idling: " + dragoon.lastCommand);
            assertTrue(dest.distTo(choke) >= 7 && dest.distTo(choke) <= 12,
                "standoff ring around the choke, not the center itself: " + dest + " vs " + choke);
            assertTrue(dest.distTo(dragoon.position()) < choke.distTo(dragoon.position()),
                "on our side of the center, between us and it");
        });
    }

    @Test
    public void crushingLineEngagesInsteadOfHolding() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 27, 29);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 29, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 29, 21);
        zealot.setEnemy();

        world(5, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            // 3 vs 1 at the choke is crushing: no hold, no lone bait -
            // the decider sends the whole line to the funnel mouth.
            assertSame(BattleStrategies.pick(a, null), BattleStrategy.COLLAPSE, "crushing superiority collapses instead of holding");
            BattleCommander.act(a, FightOrFlight.Tactic.FIGHT, null, a);
            assertNotNull(a.targetPosition, "engages instead of idling");
            assertTrue(a.targetPosition.distTo(zealot.position()) < a.distTo(zealot),
                "closing toward the zealot (out of range, so a move, not a standoff hold): "
                    + a.targetPosition);
        });
    }

    @Test
    public void crushingLineCollapsesOntoTheBait() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 27, 29);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 28, 29);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 29, 29);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 14);
        zealot.setEnemy();

        world(5, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            // 3 vs 1 at the contested choke is crushing with contact:
            // the decider sends the line to the funnel mouth, not the standoff.
            assertSame(BattleStrategies.pick(a, null), BattleStrategy.COLLAPSE, "crushing + contested: collapse onto the bait");
            for (AUnit u : new AUnit[]{a, b, c}) {
                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, u);
            }
            APosition choke = a.nearestChoke().center();
            for (AUnit u : new AUnit[]{a, b, c}) {
                APosition dest = ((FakeUnit) u).targetPosition;
                assertNotNull(dest, "the whole line joins the bait, nobody holds");
                assertTrue(dest.distTo(choke) < 5,
                    "at the funnel mouth, not the 10-tile standoff: " + dest + " vs " + choke);
            }
        });
    }

    @Test
    public void baitDetachesWhileLineHolds() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 31);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 31);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 14);
        zealot.setEnemy();

        world(5, fakeOurs(nexus, a, b, c), fakeEnemies(zealot), () -> {
            AUnit bait = atlantis.combat.strategy.SquadScout
                .pickBait(atlantis.units.select.Select.ourCombatUnits());
            assertNotNull(bait, "someone volunteers");
            for (AUnit u : new AUnit[]{a, b, c}) {
                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, u);
            }
            APosition choke = a.nearestChoke().center();
            APosition baitDest = ((FakeUnit) bait).targetPosition;
            assertNotNull(baitDest, "bait walks into the funnel mouth");
            assertTrue(baitDest.distTo(choke) < 5,
                "bait at the mouth, not wandering: " + baitDest + " vs " + choke);
            for (AUnit u : new AUnit[]{a, b, c}) {
                if (u.id() == bait.id()) continue;
                APosition hold = ((FakeUnit) u).targetPosition;
                assertNotNull(hold, "holders take the standoff, not the bunch");
                assertTrue(hold.distTo(choke) >= 7, "holders keep their distance: " + hold);
            }
        });
    }

    @Test
    public void baitPullsLineIntoTheFunnelWhenEnemyCommits() {
        // The doctrine loop at tick level (NEXT #170): the bait walks out
        // while the line holds, then the enemy commits into the funnel and
        // the line joins the bait at the mouth instead of standing off.
        // No pure bait-hold steady state is asserted on purpose: with the
        // no-contact veto at 15 tiles, the contest ring at 12 and the
        // engage envelope at 9, contact always intervenes first - the bait
        // is inherently transient (walk out, contact, collapse). The
        // numbers stay non-crushing (3v2) so HOLD, not COLLAPSE, owns the
        // quiet phase; crushing lines collapse by design (pinned above).
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 28, 31);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 29, 31);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit z1 = fake(AUnitType.Protoss_Zealot, 34, 14);
        FakeUnit z2 = fake(AUnitType.Protoss_Zealot, 36, 15);
        final APosition[] aStart = new APosition[1];

        world(150, fakeOurs(nexus, a, b, c), fakeEnemies(z1, z2), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                aStart[0] = a.position();
            }
            if (now == 61) {
                // The enemy commits into the funnel: contact from here on.
                z1.position = APosition.createFromPixels(30 * 32, 16 * 32);
                z2.position = APosition.createFromPixels(31 * 32, 15 * 32);
            }
            for (AUnit u : new AUnit[]{a, b, c}) {
                BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, null, u);
            }
            if (now != 55 && now != 150) return;

            APosition choke = a.nearestChoke().center();
            if (now == 55) {
                assertEquals(SquadScout.pickBait(
                    Select.ourCombatUnits()).id(), a.id(), "lowest id scouts while the line holds");
                assertTrue(a.position().distTo(aStart[0]) >= 2.0,
                    "the scout walked out toward the funnel: " + a.position());
                for (FakeUnit u : new FakeUnit[]{b, c}) {
                    double hold = u.position().distTo(choke);
                    assertTrue(hold >= 6 && hold <= 13,
                        "holders keep the standoff while the bait walks out: " + u.position());
                }
                return;
            }
            for (FakeUnit u : new FakeUnit[]{a, b, c}) {
                assertTrue(u.position().distTo(choke) < 7,
                    "the line joined the bait at the funnel mouth after the commit: "
                        + u.position());
            }
        });
    }
}
