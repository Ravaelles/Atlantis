package tests.acceptance;

import atlantis.combat.targeting.generic.ATargeting;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.attacked_by.UnderAttack;
import atlantis.units.select.Select;
import atlantis.util.Angle;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.*;

public class AUnitTest extends AbstractTestWithWorld {
    private FakeUnit zealot;
    private FakeUnit firebat;
    private FakeUnit cannon;

    @Test
    public void meleeOrRanged() {
        createWorld(1, () -> {
            assertTrue(fake(AUnitType.Terran_Marine).isRanged());
            assertFalse(fake(AUnitType.Terran_Marine).isMelee());

            assertFalse(fake(AUnitType.Terran_Firebat).isRanged());
            assertTrue(fake(AUnitType.Terran_Firebat).isMelee());

            assertFalse(fake(AUnitType.Terran_SCV).isRanged());
            assertTrue(fake(AUnitType.Terran_SCV).isMelee());

            assertTrue(fake(AUnitType.Terran_Vulture).isRanged());
            assertFalse(fake(AUnitType.Terran_Vulture).isMelee());

            assertTrue(fake(AUnitType.Terran_Wraith).isRanged());
            assertFalse(fake(AUnitType.Terran_Wraith).isMelee());

            assertTrue(fake(AUnitType.Terran_Siege_Tank_Siege_Mode).isRanged());
            assertFalse(fake(AUnitType.Terran_Siege_Tank_Siege_Mode).isMelee());

            assertFalse(fake(AUnitType.Protoss_Zealot).isRanged());
            assertTrue(fake(AUnitType.Protoss_Zealot).isMelee());
        });
    }

    @Test
    public void ourAndEnemyCount() {
        createWorld(1, () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon = fakeEnemy(Protoss_Photon_Cannon, 18));

//            Select.our().print("Our");
//            Select.enemy().print("Enemiz");

            assertTrue(fake(AUnitType.Terran_Marine).isOur());
            assertFalse(fake(AUnitType.Terran_Marine).isEnemy());

            EnemyUnitsUpdater.weDiscoveredEnemyUnit(fakeEnemy(Protoss_Observer, 98));

            assertFalse(zealot.isOur());
            assertTrue(zealot.isEnemy());

//            firebat.enemiesNear().print("Firebat enemies");
//            firebat.friendsNear().print("Firebat friends");
//            zealot.enemiesNear().print("Zealot enemies");
//            zealot.friendsNear().print("Zealot friends");
//            cannon.enemiesNear().print("Cannon enemies");
//            cannon.friendsNear().print("Cannon friends");

            assertEquals(3, firebat.enemiesNear().count());
            assertEquals(1, firebat.friendsNear().count());

            assertEquals(2, zealot.enemiesNear().count());
            assertEquals(2, zealot.friendsNear().count());

//            EnemyUnits.discovered().print("Discovered");

            assertEquals(4, EnemyUnits.discovered().havingPosition().count());
            assertEquals(4, EnemyUnits.discovered().havingPosition().havingAtLeastHp(1).notDeadMan().count());

//            cannon.enemiesNear().print("Cannon enemies");
//            cannon.friendsNear().print("Cannon friends");
            assertEquals(2, cannon.enemiesNear().count());
            assertEquals(2, cannon.friendsNear().count());
        });
    }

//    @Test
//    public void baseDistance() {
//        FakeUnit base;
//
//        base = fake(AUnitType.Protoss_Nexus, 40, 40).setCompleted(false);
//
//        createWorld(1, () -> {
//                //            Select.our().print("Our");
//                Select.ourWithUnfinished().print("Our with UNF");
//
//                APosition pos42 = APosition.create(42, 40);
//
//                System.err.println(A.digit(base.distTo(pos42)));
//                assertEquals("2.0", A.digit(base.distTo(pos42)));
//            },
//            () -> fakeOurs(
//                base
//            ),
//            () -> fakeEnemies()
//        );
//    }

    @Test
    public void canNotAttackNotDetectedUnits() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Drone, 11).setBurrowed(true).setDetected(false),
            fake(AUnitType.Zerg_Lurker, 12).setCloaked(true).setDetected(false),
            fake(AUnitType.Zerg_Lurker, 13).setBurrowed(true).setDetected(false),
            fake(AUnitType.Protoss_Dark_Templar, 11).setCloaked(true).setDetected(false)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(null, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void canAttackBurrowedDetectedUnits() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        final FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            expectedTarget = fake(AUnitType.Zerg_Drone, 13).setBurrowed(true).setDetected(true)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
//            System.out.println("@canAttackBurrowedDetectedUnits = " + EnemyUnits.discovered().print());
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void canAttackDetectedCloakedUnits() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        final FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            expectedTarget = fake(AUnitType.Protoss_Dragoon, 12).setCloaked(true).setDetected(true)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void canAttackDetectedBurrowedLurkers() {

        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        final FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            expectedTarget = fake(AUnitType.Zerg_Lurker, 13).setBurrowed(true).setDetected(true)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
//            System.out.println("@canAttackDetectedBurrowedLurkers = " + EnemyUnits.discovered().print());
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void canAttackDetectedCloakedDT() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        final FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            expectedTarget = fake(AUnitType.Protoss_Dark_Templar, 11).setCloaked(true).setDetected(true)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void weaponRangeForTank() {
        FakeUnit our = fake(AUnitType.Terran_Siege_Tank_Siege_Mode, 10);
        FakeUnit ling1, ling2, drone, den;

        FakeUnit[] enemies = fakeEnemies(
            den = fake(AUnitType.Zerg_Hydralisk_Den, 11),
            drone = fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Guardian, 12.5),
            ling1 = fake(AUnitType.Zerg_Zergling, 19),
            ling2 = fake(AUnitType.Zerg_Zergling, 23)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertFalse(our.hasWeaponRangeToAttack(den, 0));
            assertTrue(our.hasWeaponRangeToAttack(ling1, 0));
            assertFalse(our.hasWeaponRangeToAttack(ling2, 0));
            assertFalse(our.hasWeaponRangeToAttack(ling2, 0.9));
            assertTrue(our.hasWeaponRangeToAttack(ling2, 1.0));
        });
    }

    @Test
    public void weaponRangeForWraith() {
        FakeUnit our = fake(AUnitType.Terran_Wraith, 10);
        FakeUnit ling1, ling2, drone, den;

        FakeUnit[] enemies = fakeEnemies(
            den = fake(AUnitType.Zerg_Hydralisk_Den, 11),
            fake(AUnitType.Zerg_Guardian, 12.5),
            ling1 = fake(AUnitType.Zerg_Zergling, 14),
            drone = fake(AUnitType.Zerg_Drone, 15),
            ling2 = fake(AUnitType.Zerg_Zergling, 19)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertTrue(our.hasWeaponRangeToAttack(den, 0));
            assertTrue(our.canAttackTarget(den, true, true, true, 0));

            assertTrue(our.hasWeaponRangeToAttack(ling1, 0));
            assertTrue(our.canAttackTarget(ling1, true, true, true, 0));

            assertTrue(our.hasWeaponRangeToAttack(drone, 0));
            assertTrue(our.canAttackTarget(drone, true, true, true, 0));

            assertFalse(our.hasWeaponRangeToAttack(ling2, 0));
            assertFalse(our.canAttackTarget(ling2, true, true, true, 0));

            assertFalse(our.hasWeaponRangeToAttack(ling2, 3.9));
            assertFalse(our.canAttackTarget(ling2, true, true, true, 3.9));

            assertTrue(our.hasWeaponRangeToAttack(ling2, 4.0));
            assertTrue(our.canAttackTarget(ling2, true, true, true, 4.0));
        });
    }

    @Test
    public void facingUnitAndShowingBack() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit zergling1, zergling2, hydra;

        FakeUnit[] enemies = fakeEnemies(
            zergling1 = fake(AUnitType.Zerg_Zergling, 13),
            zergling2 = fake(AUnitType.Zerg_Zergling, 14),
            hydra = fake(AUnitType.Zerg_Hydralisk, 15)
        );

        marine.setAngle(Angle.degreesToRadians(0));
        zergling1.setAngle(Angle.degreesToRadians(50));
        zergling2.setAngle(Angle.degreesToRadians(110));
        hydra.setAngle(Angle.degreesToRadians(180));

        usingFakeOurAndFakeEnemies(marine, enemies, () -> {
//            System.out.println(marine.getAngle() + " / " + zergling1.getAngle() + " / " + zergling2.getAngle() + " / " + hydra.getAngle());

//            System.err.println("zergling1.isOtherUnitFacingThisUnit(marine) = " + zergling1.isOtherUnitFacingThisUnit(marine));
//            System.err.println("zergling1.isOtherUnitShowingBackTo(marine) = " + zergling1.isOtherUnitShowingBackTo(marine));
//            System.err.println("marine.isOtherUnitFacingThisUnit(zergling2) = " + marine.isOtherUnitFacingThisUnit(zergling2));
//            System.err.println("zergling2.isOtherUnitFacingThisUnit(marine) = " + zergling2.isOtherUnitFacingThisUnit(marine));
//            System.err.println("marine.isOtherUnitFacingThisUnit(zergling1) = " + marine.isOtherUnitFacingThisUnit(zergling1));
//            System.err.println("marine.isOtherUnitFacingThisUnit(hydra) = " + marine.isOtherUnitFacingThisUnit(hydra));

//            System.err.println("marine.isOtherUnitFacingThisUnit(hydra) = " + marine.isOtherUnitFacingThisUnit(hydra));
//            System.err.println("hydra.isOtherUnitFacingThisUnit(marine) = " + hydra.isOtherUnitFacingThisUnit(marine));

            assertFalse(marine.isOtherUnitFacingThisUnit(zergling1));
            assertTrue(zergling1.isOtherUnitFacingThisUnit(marine));

            assertTrue(marine.isOtherUnitShowingBackToUs(zergling1));
            assertFalse(zergling1.isOtherUnitShowingBackToUs(marine));

            assertFalse(marine.isOtherUnitFacingThisUnit(zergling2));
            assertTrue(zergling2.isOtherUnitFacingThisUnit(marine));

            assertFalse(marine.isOtherUnitShowingBackToUs(zergling2));

            assertTrue(marine.isOtherUnitFacingThisUnit(hydra));
            assertTrue(hydra.isOtherUnitFacingThisUnit(marine));

            assertFalse(hydra.isOtherUnitShowingBackToUs(marine));
            assertFalse(marine.isOtherUnitShowingBackToUs(hydra));
        });
    }

    @Test
    public void testLastAttackedBy() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit zergling, marine, enemyDragoon;

        FakeUnit[] enemies = fakeEnemies(
            zergling = fake(AUnitType.Zerg_Zergling, 13),
            marine = fake(AUnitType.Terran_Marine, 14),
            enemyDragoon = fake(AUnitType.Protoss_Dragoon, 15)
        );

        usingFakeOurAndFakeEnemies(dragoon, enemies, () -> {
            UnderAttack underAttack = dragoon.underAttack();

            assertTrue(null == underAttack.lastBy());
            assertTrue(99999 == underAttack.lastAgo());
        });
    }

    @Test
    public void real() {
        createWorld(1, () -> {
            assertTrue(fake(AUnitType.Terran_Marine).isRealUnit());
            assertTrue(fake(AUnitType.Terran_Vulture_Spider_Mine).isRealUnit());
            assertTrue(fake(AUnitType.Protoss_Zealot).isRealUnit());
            assertTrue(fake(Protoss_Photon_Cannon).isRealUnit());
            assertTrue(fake(Zerg_Creep_Colony).isRealUnit());

            assertFalse(fake(AUnitType.Protoss_Scarab).isRealUnit());
            assertFalse(fake(AUnitType.Zerg_Egg).isRealUnit());
            assertFalse(fake(AUnitType.Zerg_Lurker_Egg).isRealUnit());
        });
    }

    @Test
    public void combatBuildings() {
        createWorld(1, () -> {
            AUnit sunken = fake(Zerg_Sunken_Colony);
            AUnit spore = fake(Zerg_Spore_Colony);
            AUnit creep = fake(Zerg_Creep_Colony);
            AUnit turret = fake(Terran_Missile_Turret);
            AUnit bunker = fake(Terran_Bunker);
            AUnit cannon = fake(Protoss_Photon_Cannon);

            for (AUnit unit : new AUnit[]{sunken, spore, creep, turret, bunker, cannon}) {
                assertTrue(unit.isCombatBuilding());
                assertFalse(unit.isCombatUnit());
                assertTrue(unit.isRealUnit());
            }
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            firebat = fake(AUnitType.Terran_Firebat, 11),
            fake(AUnitType.Terran_Siege_Tank_Siege_Mode, 12)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 16;
        return fakeEnemies(
            zealot = fake(AUnitType.Protoss_Zealot, enemyTy),
            fake(AUnitType.Protoss_Dragoon, enemyTy + 1)
        );
    }

}
