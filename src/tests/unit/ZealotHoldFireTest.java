package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Zealot hold-fire discipline (NEXT #179, owner 2026-10-21):
 * ZealDrag_v_Zeal lost because all 5 zealots charged first and died
 * one by one (frames 157-457) while dragoons watched. Until a ranged
 * weapon fires inside the cluster, zealots hold instead of
 * initiating; the moment one is really hit (>10 missing), the
 * formation breaks and it attacks. Lone-zealot armies never hold.
 */
@Tag("combat")
public class ZealotHoldFireTest extends WorldStubForTests {

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
    public void holdsWhileRangedIsSilent() {
        FakeUnit dragoon = ours(AUnitType.Protoss_Dragoon, 28, 20);
        FakeUnit zealot = ours(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit enemy = foe(AUnitType.Protoss_Zealot, 32, 20);

        world(1, fakeOurs(dragoon, zealot), fakeEnemies(enemy), () -> {
            BattleCommander.act(zealot, FightOrFlight.Tactic.FIGHT, null, zealot);
            assertFalse(attacked(zealot),
                "no ranged shot yet, unwounded: hold formation, don't initiate: "
                    + ((FakeOrderSink) zealot.orderSink()).orders());
        });
    }

    @Test
    public void releasedWhenRangedFires() {
        FakeUnit dragoon = ours(AUnitType.Protoss_Dragoon, 28, 20);
        dragoon.cooldown = 20;
        FakeUnit zealot = ours(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit enemy = foe(AUnitType.Protoss_Zealot, 32, 20);

        world(1, fakeOurs(dragoon, zealot), fakeEnemies(enemy), () -> {
            BattleCommander.act(zealot, FightOrFlight.Tactic.FIGHT, null, zealot);
            assertTrue(attacked(zealot),
                "dragoon fired: the ball engages together: "
                    + ((FakeOrderSink) zealot.orderSink()).orders());
        });
    }

    @Test
    public void releasedWhenReallyHit() {
        FakeUnit dragoon = ours(AUnitType.Protoss_Dragoon, 28, 20);
        FakeUnit zealot = ours(AUnitType.Protoss_Zealot, 30, 20);
        zealot.setHp(zealot.maxHp() - 20);
        FakeUnit enemy = foe(AUnitType.Protoss_Zealot, 32, 20);

        world(1, fakeOurs(dragoon, zealot), fakeEnemies(enemy), () -> {
            BattleCommander.act(zealot, FightOrFlight.Tactic.FIGHT, null, zealot);
            assertTrue(attacked(zealot),
                "20 damage is a wound: break formation and fight: "
                    + ((FakeOrderSink) zealot.orderSink()).orders());
        });
    }

    @Test
    public void loneZealotsFightAsBefore() {
        FakeUnit zealot = ours(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit enemy = foe(AUnitType.Protoss_Zealot, 32, 20);

        world(1, fakeOurs(zealot), fakeEnemies(enemy), () -> {
            BattleCommander.act(zealot, FightOrFlight.Tactic.FIGHT, null, zealot);
            assertTrue(attacked(zealot),
                "no ranged allies: unchanged behavior, no staring contest: "
                    + ((FakeOrderSink) zealot.orderSink()).orders());
        });
    }
}
