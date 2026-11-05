package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * No lone attacks, systemically (owner order 2026-11-07): a unit that
 * acquires a target while detached from the ball rejoins instead of
 * engaging - the gate sits in the commander's fight branch, so no
 * behavior, kite or chase can route around it. The ball's anchor is
 * the median, so one stray never drags it.
 */
@Tag("combat")
public class StragglerRecallTest extends WorldStubForTests {

    @AfterEach
    public void cleanAlpha() {
        Alpha.get().clear();
    }

    private static FakeUnit ours(AUnitType type, int tx, int ty) {
        return fake(type, tx, ty);
    }

    private static FakeUnit foe(AUnitType type, int tx, int ty) {
        FakeUnit enemy = fake(type, tx, ty);
        enemy.setEnemy();

        return enemy;
    }

    private static boolean attacked(FakeUnit unit) {
        for (FakeOrderSink.Record record : ((FakeOrderSink) unit.orderSink()).orders()) {
            if (record.operation.contains("ttack")) return true;
        }

        return false;
    }

    @Test
    public void strayRejoinsInsteadOfEngaging() {
        FakeUnit d1 = ours(AUnitType.Protoss_Zealot, 28, 20);
        FakeUnit d2 = ours(AUnitType.Protoss_Zealot, 29, 20);
        FakeUnit d3 = ours(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit stray = ours(AUnitType.Protoss_Zealot, 50, 20);
        FakeUnit enemy = foe(AUnitType.Zerg_Zergling, 52, 20);

        world(3, fakeOurs(d1, d2, d3, stray), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            try {
                Alpha.gather();
                BattleCommander.act(stray, FightOrFlight.Tactic.FIGHT, null, Alpha.get().leader());

                assertFalse(attacked(stray),
                    "20 tiles from the ball: rejoin, never duel alone: "
                        + ((FakeOrderSink) stray.orderSink()).orders());
                assertTrue(stray.lastCommand.equals("Move"),
                    "the stray is ordered back to the ball: " + stray.lastCommand);
                assertTrue(atlantis.util.log.EventLog.toJsonLines().contains("COHESION_RECALL"),
                    "the recall is twinned, not invisible");
            }
            finally {
                Alpha.get().clear();
            }
        });
    }

    @Test
    public void ballMemberEngagesNormally() {
        FakeUnit d1 = ours(AUnitType.Protoss_Zealot, 28, 20);
        FakeUnit d2 = ours(AUnitType.Protoss_Zealot, 29, 20);
        FakeUnit d3 = ours(AUnitType.Protoss_Zealot, 30, 20);
        d2.setHp(d2.maxHp() - 20);
        FakeUnit enemy = foe(AUnitType.Zerg_Zergling, 32, 20);

        world(3, fakeOurs(d1, d2, d3), fakeEnemies(enemy), () -> {
            if (atlantis.game.A.now() != 2) return;

            try {
                Alpha.gather();
                BattleCommander.act(d2, FightOrFlight.Tactic.FIGHT, null, Alpha.get().leader());

                assertTrue(attacked(d2),
                    "inside the ball and really hit: gate and hold-fire both release: "
                        + ((FakeOrderSink) d2.orderSink()).orders());
            }
            finally {
                Alpha.get().clear();
            }
        });
    }

    @Test
    public void medianIgnoresTheOutlier() {
        FakeUnit d1 = ours(AUnitType.Protoss_Zealot, 28, 20);
        FakeUnit d2 = ours(AUnitType.Protoss_Zealot, 29, 20);
        FakeUnit d3 = ours(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit stray = ours(AUnitType.Protoss_Zealot, 90, 20);

        world(3, fakeOurs(d1, d2, d3, stray), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            try {
                Alpha.gather();

                atlantis.map.position.HasPosition median = Alpha.get().median();
                double strayDist = stray.distTo(median);
                double ballDist = d2.distTo(median);

                assertTrue(ballDist <= 2,
                    "the median sits with the ball, not between ball and stray: " + ballDist);
                assertTrue(strayDist > 6,
                    "the stray reads as detached from its own median: " + strayDist);
            }
            finally {
                Alpha.get().clear();
            }
        });
    }
}
