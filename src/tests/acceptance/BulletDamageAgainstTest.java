package tests.acceptance;

import atlantis.map.bullets.BulletDamageAgainst;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeBullet;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Protoss_Dragoon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BulletDamageAgainstTest extends AbstractTestWithWorld {
    private FakeUnit hydra;
    private FakeUnit marine;
    private FakeUnit dragoon;
    private FakeUnit vulture;
    private FakeUnit sunken;
    private FakeUnit zergling;

    @Test
    public void unitDamages() {
        createWorld(1, () -> {
                assertEquals(20, BulletDamageAgainst.forBullet(createBullet(dragoon, sunken)));

                assertEquals(15, BulletDamageAgainst.forBullet(createBullet(dragoon, vulture)));
                assertEquals(15, BulletDamageAgainst.forBullet(createBullet(dragoon, hydra)));

                assertEquals(10, BulletDamageAgainst.forBullet(createBullet(dragoon, marine)));
                assertEquals(10, BulletDamageAgainst.forBullet(createBullet(dragoon, zergling)));

                assertEquals(5, BulletDamageAgainst.forBullet(createBullet(zergling, sunken)));
                assertEquals(5, BulletDamageAgainst.forBullet(createBullet(zergling, dragoon)));
                assertEquals(5, BulletDamageAgainst.forBullet(createBullet(zergling, marine)));

                assertEquals(20, BulletDamageAgainst.forBullet(createBullet(vulture, marine)));
                assertEquals(10, BulletDamageAgainst.forBullet(createBullet(vulture, vulture)));
                assertEquals(5, BulletDamageAgainst.forBullet(createBullet(vulture, dragoon)));
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

    private FakeBullet createBullet(FakeUnit attacker, FakeUnit target) {
        FakeBullet bullet = FakeBullet.fromPosition(attacker.position, attacker, target);
        return bullet;
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
//            marine = fake(AUnitType.Terran_Marine, 10),
//            wraith = fake(AUnitType.Terran_Wraith, 90)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 16;
        return fakeEnemies(
//            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy),
//            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy + 1),
//            fakeEnemy(Protoss_Zealot, 11)
        );
    }

//    private FakeUnit[] generateEnemiesWithStasisesAndLockedDown() {
//        int enemyTy = 16;
//        return fakeEnemies(
//            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy),
//            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy + 1),
//            fakeEnemy(Protoss_Zealot, 11),
////            fakeEnemy(Protoss_Dragoon, 92),
////            fakeEnemy(Protoss_Dragoon, 93)
//            fakeEnemy(Protoss_Dragoon, 92).setLockedDown(true),
//            fakeEnemy(Protoss_Dragoon, 93).setStasised(true)
//        );
//    }

}
