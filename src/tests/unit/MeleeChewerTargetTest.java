package tests.unit;

import atlantis.combat.targeting.AcquireTarget;
import atlantis.combat.targeting.MeleeChewerTarget;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Seam pin for the chewed-melee rule (owner, Steamhammer loss): a melee
 * unit with its shot ready hits the weakest enemy IN RANGE - no type
 * bans, no walk to far targets. Reach is raw engine distance (0.5
 * tiles): the nominal range table truncates melee bodies to zero and
 * can never express contact.
 */
@Tag("combat")
public class MeleeChewerTargetTest extends WorldStubForTests {

    @Test
    public void weakestInRangeWinsOverNearest() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit z = fake(AUnitType.Protoss_Zealot, 30, 20);
        z.hp = 100;
        z.cooldown = 0;
        FakeUnit healthy = fake(AUnitType.Zerg_Zergling, 30 + 0.25, 20);
        healthy.setEnemy();
        healthy.hp = 35;
        FakeUnit weak = fake(AUnitType.Zerg_Zergling, 30 - 0.35, 20);
        weak.setEnemy();
        weak.hp = 5;

        world(3, fakeOurs(nexus, z), fakeEnemies(weak, healthy), () -> {
            if (atlantis.game.A.now() != 1) return;

            AUnit pick = MeleeChewerTarget.pick(z);
            assertEquals(weak.id(), pick == null ? -1 : pick.id(),
                "weakest in reach, not nearest");
            AUnit acquired = AcquireTarget.acquire(z);
            assertEquals(weak.id(), acquired == null ? -1 : acquired.id(),
                "the chain serves the chewer rule first");
        });
    }

    @Test
    public void coolingWeaponDoesNotTrigger() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit z = fake(AUnitType.Protoss_Zealot, 30, 20);
        z.hp = 100;
        z.cooldown = 12;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 30 + 0.25, 20);
        ling.setEnemy();
        ling.hp = 35;

        world(3, fakeOurs(nexus, z), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertNull(MeleeChewerTarget.pick(z),
                "cooldown 12: march/kite as today, the rule owns only the ready shot");
        });
    }

    @Test
    public void rangedUnitsKeepTheirBans() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.hp = 120;
        dragoon.cooldown = 0;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 30 + 0.25, 20);
        ling.setEnemy();
        ling.hp = 35;

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertNull(MeleeChewerTarget.pick(dragoon),
                "dragoons keep focus/SpreadFire discipline - the rule is melee-only");
        });
    }
}
