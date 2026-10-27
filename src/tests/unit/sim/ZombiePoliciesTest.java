package tests.unit.sim;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The zombie-policy library pins here: each policy answers a fixed
 * fixture deterministically, so scenarios built on them stay
 * reproducible and gates cannot silently overfit one script.
 */
@Tag("combat")
public class ZombiePoliciesTest extends tests.unit.AbstractTestWithUnits {

    private static FakeUnit ling(double tx, double ty, int hp) {
        FakeUnit l = fake(AUnitType.Zerg_Zergling, tx, ty);
        l.setEnemy();
        l.hp = hp;

        return l;
    }

    @Test
    public void chaseNearestRetargetsEveryTick() {
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 40, 20);
        FakeUnit f = ling(31, 20, 35);
        List<FakeUnit> foes = new ArrayList<>(Collections.singletonList(f));
        List<FakeUnit> ours = new ArrayList<>(Arrays.asList(a, b));

        ZombiePolicies.chaseNearest(foes, ours);
        assertEquals(a.id(), f.target.id(), "nearest first");

        a.hp = 0;
        ZombiePolicies.chaseNearest(foes, ours);
        assertEquals(b.id(), f.target.id(), "re-targets the next-nearest when the mark dies");
    }

    @Test
    public void standAndTradeSticksToFirstContact() {
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 40, 20);
        FakeUnit f = ling(31, 20, 35);
        List<FakeUnit> foes = new ArrayList<>(Collections.singletonList(f));
        List<FakeUnit> ours = new ArrayList<>(Arrays.asList(a, b));

        ZombiePolicies.standAndTrade(foes, ours);
        assertEquals(a.id(), f.target.id(), "first contact sticks");

        FakeUnit c = fake(AUnitType.Protoss_Zealot, 31.5, 20);
        ours.add(c);
        ZombiePolicies.standAndTrade(foes, ours);
        assertEquals(a.id(), f.target.id(), "no re-forming on the closer newcomer");
    }

    @Test
    public void focusWeakestPilesOntoLowHp() {
        FakeUnit healthy = fake(AUnitType.Protoss_Zealot, 30, 20);
        healthy.hp = 100;
        FakeUnit weak = fake(AUnitType.Protoss_Zealot, 32, 20);
        weak.hp = 12;
        FakeUnit f1 = ling(31, 21, 35);
        FakeUnit f2 = ling(33, 21, 35);
        List<FakeUnit> foes = new ArrayList<>(Arrays.asList(f1, f2));
        List<FakeUnit> ours = new ArrayList<>(Arrays.asList(healthy, weak));

        ZombiePolicies.focusWeakest(foes, ours);
        assertEquals(weak.id(), f1.target.id(), "every gun answers the weak one");
        assertEquals(weak.id(), f2.target.id(), "every gun answers the weak one");
    }

    @Test
    public void kiteBackYieldsInsideHoldTradesOutside() {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 120;
        FakeUnit close = ling(31, 20, 35);
        FakeUnit far = ling(45, 20, 35);
        List<FakeUnit> ours = new ArrayList<>(Collections.singletonList(d));

        ZombiePolicies.kiteBack(new ArrayList<>(Collections.singletonList(close)), ours, 4);
        assertEquals("Move", close.lastCommand, "inside hold range: walk away, no shot");
        assertTrue(close.targetPosition.distTo(d) > close.distTo(d),
            "the step opens distance instead of closing it");

        ZombiePolicies.kiteBack(new ArrayList<>(Collections.singletonList(far)), ours, 4);
        assertEquals("AttackUnit", far.lastCommand, "outside hold range: stand and trade");
        assertEquals(d.id(), far.target.id(), "trades with the mark");
    }

    @Test
    public void holdStillIsInert() {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit f = ling(31, 20, 35);
        List<FakeUnit> foes = new ArrayList<>(Collections.singletonList(f));

        ZombiePolicies.holdStill(foes, new ArrayList<>(Collections.singletonList(d)));
        assertEquals("Stop", f.lastCommand, "no orders issued");
        assertNull(f.target, "no target acquired");
    }
}
