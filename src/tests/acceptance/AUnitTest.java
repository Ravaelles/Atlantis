package tests.acceptance;

import atlantis.combat.targeting.ATargeting;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static org.junit.Assert.*;

public class AUnitTest extends AbstractTestFakingGame {
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

            assertTrue(fake(AUnitType.Terran_Marine).isOur());
            assertFalse(fake(AUnitType.Terran_Marine).isEnemy());

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

            assertEquals(2, cannon.enemiesNear().count());
            assertEquals(2, cannon.friendsNear().count());
        });
    }

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
