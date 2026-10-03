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

            // One Phase Disruptor round does 4 against a small unit (8 damage,
            // Explosive, halved) and 6 against a medium one, and one round is
            // added per frame. A Zergling has 35 hit points plus the point of
            // armour a non-Terran gets, so it dies on the 9th round; a Marine has
            // 45 and no bonus, so the 12th; a Vulture 80, so the 14th.
            //
            // The thresholds this replaces (frames 3 and 7) came from the engine
            // placeholder, whose Dragoon did 20 damage per round - one bullet was
            // enough to kill anything, so the test could not tell a dead man from
            // a lucky shot.
            assertEquals(A.now() >= 9, DeadMan.isDeadMan(zergling), "35 hp, 4 a round");
            assertEquals(A.now() >= 12, DeadMan.isDeadMan(marine), "45 hp, 4 a round");
            assertEquals(A.now() >= 14, DeadMan.isDeadMan(vulture), "80 hp, 6 a round");
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
