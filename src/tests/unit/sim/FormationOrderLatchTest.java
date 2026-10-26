package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.FormationHold;
import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.formation.FormationOrderLatch;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #210: while defending, the same formation order goes out at most
 * once per 10 frames - re-issuing it every frame churns the line, so a
 * shared latch (not a per-unit cooldown) owns the cadence. First
 * deployment is never delayed, a changed destination always flows, and
 * leaving DEFEND forgets the latch. A denied re-issue holds position
 * instead of falling through to creep/gather - falling through would
 * scatter the line the latch is calming.
 */
@Tag("combat")
public class FormationOrderLatchTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @BeforeEach
    public void clearLatch() {
        FormationOrderLatch.clearForTests();
        atlantis.combat.cluster.ClusterMission.resetForTests();
    }

    @AfterEach
    public void cleanLatch() {
        FormationOrderLatch.clearForTests();
        atlantis.combat.cluster.ClusterMission.resetForTests();
    }

    @Test
    public void identicalReissueWaitsTenFrames() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 22, 20);
        // A lone zealot reads 790% strength here and flips the posture to
        // ATTACK (early-strength rule) - the latch under test only exists
        // on DEFEND, so the world fields a zealot mass that holds the line
        // home under both the old headcount and the new strength rule.
        FakeUnit[] foes = new FakeUnit[] {
            fake(AUnitType.Protoss_Zealot, 60, 20),
            fake(AUnitType.Protoss_Zealot, 61, 20),
            fake(AUnitType.Protoss_Zealot, 62, 20),
            fake(AUnitType.Protoss_Zealot, 60, 21),
            fake(AUnitType.Protoss_Zealot, 61, 21),
            fake(AUnitType.Protoss_Zealot, 62, 21),
        };

        world(30, fakeOurs(nexus, a, b, c), fakeEnemies(foes), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                for (FakeUnit u : new FakeUnit[] { a, b, c }) Alpha.get().addUnit(u);
            }
            if (now != 12) return;

            APosition slot = APosition.create(30, 20);
            assertTrue(FormationOrderLatch.shouldIssue(a, slot),
                "first deployment is never delayed");
            assertFalse(FormationOrderLatch.shouldIssue(a, slot),
                "the identical re-issue waits out the window");
            assertTrue(FormationOrderLatch.shouldIssue(a, APosition.create(31, 20)),
                "a changed destination is a new order, not a repeat");
        });
        for (FakeUnit u : new FakeUnit[] { a, b, c }) Alpha.get().removeUnit(u);
    }

    @Test
    public void windowReopensAfterTenFrames() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 22, 20);
        FakeUnit[] foes = new FakeUnit[] {
            fake(AUnitType.Protoss_Zealot, 60, 20),
            fake(AUnitType.Protoss_Zealot, 61, 20),
            fake(AUnitType.Protoss_Zealot, 62, 20),
            fake(AUnitType.Protoss_Zealot, 60, 21),
            fake(AUnitType.Protoss_Zealot, 61, 21),
            fake(AUnitType.Protoss_Zealot, 62, 21),
        };

        world(30, fakeOurs(nexus, a, b, c), fakeEnemies(foes), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                for (FakeUnit u : new FakeUnit[] { a, b, c }) Alpha.get().addUnit(u);
            }
            if (now == 2) {
                assertTrue(FormationOrderLatch.shouldIssue(a, APosition.create(30, 20)),
                    "first issuance goes");
            }
            if (now == 12) {
                assertTrue(FormationOrderLatch.shouldIssue(a, APosition.create(30, 20)),
                    "ten frames later the same order flows again");
            }
        });
        for (FakeUnit u : new FakeUnit[] { a, b, c }) Alpha.get().removeUnit(u);
    }

    @Test
    public void attackPostureNeverBlocks() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit[] army = new FakeUnit[9];
        for (int i = 0; i < 9; i++) {
            army[i] = fake(AUnitType.Protoss_Dragoon, 20 + i, 20);
            Alpha.get().addUnit(army[i]);
        }
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 60, 20);
        foe.setEnemy();

        FakeUnit[] ours = new FakeUnit[10];
        ours[0] = nexus;
        System.arraycopy(army, 0, ours, 1, 9);
        world(10, fakeOurs(ours), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 5) return;

            APosition slot = APosition.create(30, 20);
            assertTrue(FormationOrderLatch.shouldIssue(army[0], slot),
                "an attack posture marches unthrottled");
            assertTrue(FormationOrderLatch.shouldIssue(army[0], slot),
                "attack never blocks, even back-to-back");
        });
        for (FakeUnit u : army) Alpha.get().removeUnit(u);
    }

    @Test
    public void deniedHoldKeepsStandingInsteadOfFallingThrough() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 22, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 60, 20);
        foe.setEnemy();
        foe.hp = 100;
        FakeUnit[] pack = new FakeUnit[] {
            fake(AUnitType.Protoss_Zealot, 61, 20),
            fake(AUnitType.Protoss_Zealot, 62, 20),
            fake(AUnitType.Protoss_Zealot, 60, 21),
            fake(AUnitType.Protoss_Zealot, 61, 21),
            fake(AUnitType.Protoss_Zealot, 62, 21),
        };

        world(5, fakeOurs(nexus, a, b, c), fakeEnemies(foe, pack[0], pack[1], pack[2], pack[3], pack[4]), () -> {
            if (atlantis.game.A.now() != 2) return;
            for (FakeUnit u : new FakeUnit[] { a, b, c }) Alpha.get().addUnit(u);

            try {
                APosition anchor = APosition.create(30, 20);
                int before = moveOrders(a);
                assertTrue(FormationHold.holdIfCalm(a, foe, FightOrFlight.Tactic.FIGHT, a, anchor),
                    "first deployment goes out");
                int issued = moveOrders(a) - before;
                assertTrue(FormationHold.holdIfCalm(a, foe, FightOrFlight.Tactic.FIGHT, a, anchor),
                    "a denied re-issue still claims the unit (stand), "
                    + "it must not fall through to creep/gather");
                int denied = moveOrders(a) - before - issued;
                assertTrue(issued >= 1, "the first call issued the march");
                assertTrue(denied == 0,
                    "the denied call issues nothing - the line holds instead of churning");
            }
            finally {
                for (FakeUnit u : new FakeUnit[] { a, b, c }) Alpha.get().removeUnit(u);
            }
        });
    }

    private static int moveOrders(FakeUnit d) {
        int n = 0;
        for (tests.fakes.FakeOrderSink.Record r
                : ((tests.fakes.FakeOrderSink) d.orderSink()).orders()) {
            if (r.operation.equals("move")) n++;
        }

        return n;
    }
}
