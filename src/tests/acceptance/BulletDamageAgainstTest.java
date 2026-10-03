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
        world(1, fakeOurs(
                dragoon = fake(Protoss_Dragoon, 10),
                marine = fake(AUnitType.Terran_Marine, 11.5),
                vulture = fake(AUnitType.Terran_Vulture, 11.7)
            ), fakeEnemies(
                sunken = fake(AUnitType.Zerg_Sunken_Colony, 13),
                hydra = fake(AUnitType.Zerg_Hydralisk, 13.2),
                zergling = fake(AUnitType.Zerg_Zergling, 13.4)
            ), () -> {
            // A Phase Disruptor round does 8 damage and is Explosive, so what it
            // does depends on the size of what it hits: full against a large unit,
            // three quarters against a medium one, half against a small one. The
            // 20/15/10 this test used to expect came from the engine placeholder,
            // which claimed 20 damage against large targets - a number no Dragoon
            // in the game ever has.
            assertEquals(8, BulletDamageAgainst.forBullet(createBullet(dragoon, sunken)),
                "Explosive, large target");
            assertEquals(6, BulletDamageAgainst.forBullet(createBullet(dragoon, vulture)),
                "Explosive, medium target");
            assertEquals(6, BulletDamageAgainst.forBullet(createBullet(dragoon, hydra)),
                "Explosive, medium target");
            assertEquals(4, BulletDamageAgainst.forBullet(createBullet(dragoon, marine)),
                "Explosive, small target");
            assertEquals(4, BulletDamageAgainst.forBullet(createBullet(dragoon, zergling)),
                "Explosive, small target");

            // Zergling claws are Normal damage: 5 whatever the target is.
            assertEquals(5, BulletDamageAgainst.forBullet(createBullet(zergling, sunken)));
            assertEquals(5, BulletDamageAgainst.forBullet(createBullet(zergling, dragoon)));
            assertEquals(5, BulletDamageAgainst.forBullet(createBullet(zergling, marine)));

            // The Vulture's own weapon is the one number this project has no
            // source for (tests/fakes/UnitStatsTable), so 20 below is still the
            // engine placeholder - what these three pin is the Concussive
            // modifier, which is real: full against a small unit, half against a
            // medium one, a quarter against a large one.
            assertEquals(20, BulletDamageAgainst.forBullet(createBullet(vulture, marine)));
            assertEquals(10, BulletDamageAgainst.forBullet(createBullet(vulture, vulture)));
            assertEquals(5, BulletDamageAgainst.forBullet(createBullet(vulture, dragoon)));
        });
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
