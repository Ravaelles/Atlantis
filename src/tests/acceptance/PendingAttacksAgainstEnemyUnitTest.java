package tests.acceptance;

import atlantis.combat.state.AttackState;
import atlantis.map.bullets.ABullet;
import atlantis.map.bullets.DeadMan;
import atlantis.units.AUnitType;
import atlantis.units.attacked_by.Bullets;
import atlantis.units.attacked_by.PendingAttacksAgainstEnemyUnit;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeBullet;
import tests.fakes.FakeUnit;

import java.util.Collection;

import static atlantis.units.AUnitType.Protoss_Dragoon;
import static org.junit.jupiter.api.Assertions.*;

public class PendingAttacksAgainstEnemyUnitTest extends WorldStubForTests {
    private FakeUnit hydra;
    private FakeUnit marine;
    private FakeUnit dragoon;
    private FakeUnit vulture;
    private FakeUnit sunken;
    private FakeUnit zergling;

    @Test
    public void dragoonNotStartedAttacksAreAccountedAsBullets() {
        createWorld(1,
            fakeOurs(
                dragoon = fake(Protoss_Dragoon, 10),
                marine = fake(AUnitType.Terran_Marine, 11.5),
                vulture = fake(AUnitType.Terran_Vulture, 11.7)
            ),
            fakeEnemies(
                sunken = fake(AUnitType.Zerg_Sunken_Colony, 13),
//                hydra = fake(AUnitType.Zerg_Hydralisk, 13.2),
                zergling = fake(AUnitType.Zerg_Zergling, 13.4).setHp(9)
            ),
            () -> {
                assertFalse(zergling.isDeadMan());
                assertEquals(0, PendingAttacksAgainstEnemyUnit.against(zergling).size());

                (new DeadMan()).clearCache();

                dragoon.setFakeTarget(zergling);
                dragoon.setAttackState(AttackState.STARTING);
                assertEquals(zergling, dragoon.target());

                Collection<ABullet> bullets = PendingAttacksAgainstEnemyUnit.against(zergling);

                assertEquals(1, bullets.size());
                assertTrue(zergling.isDeadMan());
            }
        );
    }
}