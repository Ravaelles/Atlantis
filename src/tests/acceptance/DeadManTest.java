package tests.acceptance;

import atlantis.game.A;
import atlantis.map.bullets.DeadMan;
import atlantis.units.AUnitType;
import atlantis.units.UnitStateCommander;
import atlantis.units.attacked_by.Bullets;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeBullet;
import tests.fakes.FakeBullets;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Protoss_Dragoon;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeadManTest extends AbstractTestWithWorld {
    private FakeUnit hydra;
    private FakeUnit marine;
    private FakeUnit dragoon;
    private FakeUnit vulture;
    private FakeUnit sunken;
    private FakeUnit zergling;

    @Test
    public void isDeadMan_dragoon() {
        world(15, fakeOurs(
                dragoon = fake(Protoss_Dragoon, 10),
                marine = fake(AUnitType.Terran_Marine, 11.5),
                vulture = fake(AUnitType.Terran_Vulture, 11.7)
            ), fakeEnemies(
                sunken = fake(AUnitType.Zerg_Sunken_Colony, 13),
                hydra = fake(AUnitType.Zerg_Hydralisk, 13.2),
                zergling = fake(AUnitType.Zerg_Zergling, 13.4)
            ), () -> {
            (new UnitStateCommander()).invokedCommander();

            createBullet(dragoon, zergling);
            createBullet(dragoon, marine);
            createBullet(dragoon, vulture);

//                System.out.println("@" + A.now());
//                System.out.println(Bullets.against(marine).size());
//                System.out.println(DeadMan.isDeadMan(marine));
//                System.out.println("---- All bullets: " + Bullets.knownBullets().size());
//                for (ABullet bullet : Bullets.knownBullets()) {
//                    System.out.println("---- " + bullet);
//                }
//                System.err.println("DeadMan.isDeadMan(zergling) = " + DeadMan.isDeadMan(zergling));
//                System.out.println(Bullets.against(zergling).size());

            // One Phase Disruptor round does 10 against a small unit (20 damage,
            // Explosive, halved) and 15 against a medium one, and one round is
            // added per frame. A Zergling has 35 hit points plus the point of
            // armour a non-Terran gets, so it dies on the 4th round; a Marine
            // has 40 and no bonus, so the 4th as well; a Vulture has 80, so
            // the 6th (5 x 15 = 75 is not enough).
            assertEquals(A.now() >= 4, DeadMan.isDeadMan(zergling), "35 hp, 10 a round");
            assertEquals(A.now() >= 4, DeadMan.isDeadMan(marine), "40 hp, 10 a round");
            assertEquals(A.now() >= 6, DeadMan.isDeadMan(vulture), "80 hp, 15 a round");
        });
    }

    // =========================================================

    private FakeBullet createBullet(FakeUnit attacker, FakeUnit target) {
        FakeBullet bullet = FakeBullet.fromPosition(attacker.position, attacker, target);
        FakeBullets.allBullets.add(bullet);
        System.out.println("Fake all: " + FakeBullets.allBullets.size());
        Bullets.updateKnown();
//        FakeBullets.allBullets.put(bullet.id(), bullet);
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
