package tests.acceptance;

import atlantis.game.A;
import atlantis.map.bullets.BulletDamageAgainst;
import atlantis.map.bullets.DeadMan;
import atlantis.units.AUnitType;
import atlantis.units.StateCommander;
import atlantis.units.UnitStateManager;
import atlantis.units.attacked_by.KnownBullets;
import org.junit.Test;
import tests.fakes.FakeBullet;
import tests.fakes.FakeBullets;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Protoss_Dragoon;
import static org.junit.Assert.assertEquals;

public class DeadManTest extends AbstractTestFakingGame {
    private FakeUnit hydra;
    private FakeUnit marine;
    private FakeUnit dragoon;
    private FakeUnit vulture;
    private FakeUnit sunken;
    private FakeUnit zergling;

    @Test
    public void isDeadMan_dragoon() {
        createWorld(5, () -> {
                (new StateCommander()).invokeCommander();

                createBullet(dragoon, zergling);
                createBullet(dragoon, marine);
                createBullet(dragoon, vulture);

//                System.out.println("@" + A.now());
//                System.out.println(KnownBullets.against(marine).size());
//                System.out.println(DeadMan.isDeadMan(marine));
//                System.out.println(KnownBullets.knownBullets().size());
//                System.err.println("DeadMan.isDeadMan(zergling) = " + DeadMan.isDeadMan(zergling));
//                System.out.println(KnownBullets.against(zergling).size());

                assertEquals(A.now() <= 3 ? false : true, DeadMan.isDeadMan(zergling));
                assertEquals(A.now() <= 3 ? false : true, DeadMan.isDeadMan(marine));
                assertEquals(A.now() <= 7 ? false : true, DeadMan.isDeadMan(vulture));
            },
            () -> fakeOurs(
                dragoon = fake(Protoss_Dragoon, 10),
                marine = fake(AUnitType.Terran_Marine, 11.5),
                vulture = fake(AUnitType.Terran_Vulture, 11.7)
            ),
            () -> fakeEnemies(
                sunken = fake(AUnitType.Zerg_Sunken_Colony, 13),
                hydra = fake(AUnitType.Zerg_Hydralisk, 13.2),
                zergling = fake(AUnitType.Zerg_Zergling, 13.4)
            )
        );
    }

    // =========================================================

    private FakeBullet createBullet(FakeUnit attacker, FakeUnit target) {
        FakeBullet bullet = FakeBullet.fromPosition(attacker.position, attacker, target);
        FakeBullets.allBullets.add(bullet);
        return bullet;
    }

    protected FakeUnit[] generateOur() {
        return fakeOurs(
        );
    }

    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
        );
    }
}
