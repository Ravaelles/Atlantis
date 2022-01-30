package tests.acceptance;

import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AUnitTest extends AbstractTestFakingGame {

    private FakeUnit zealot;

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
    public void ourOrEnemy() {
        createWorld(1, () -> {
            assertTrue(fake(AUnitType.Terran_Marine).isOur());
            assertFalse(fake(AUnitType.Terran_Marine).isEnemy());

            assertFalse(zealot.isOur());
            assertTrue(zealot.isEnemy());
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
                fake(AUnitType.Terran_Firebat, 11)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 16;
        return fakeEnemies(
                zealot = fake(AUnitType.Protoss_Zealot, enemyTy)
//                fake(AUnitType.Protoss_Zealot, enemyTy + 1)
        );
    }

}
