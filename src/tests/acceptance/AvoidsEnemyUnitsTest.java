package tests.acceptance;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertTrue;

public class AvoidsEnemyUnitsTest extends AbstractTestFakingGame {
    private FakeUnit zealot;

    /**
     * With buildings there is a problem - JFAP returns "no danger" status
     * if the unit is 0.1 tiles outside of range of a Sunken Colony, but the
     * eval gets drastically worse once within range.
     */
    @Test
    public void marinesAreAvoidingZealots() {
        createWorld(100, () -> {
            FakeUnit unit = ourFirst;
            unit.setSquad(Alpha.get());
            (new CombatUnitManager(unit)).handle();

            double distToZealot = distToNearestEnemy(unit);
            boolean isSafe = distToZealot > 1.7;
            boolean alwaysShow = false;
//            boolean alwaysShow = true;

            if (!isSafe || alwaysShow) {
                System.out.println(A.now() + " -       " + unit.tooltip()
                    + "\n   " + unit.manager() + " / " + unit.lastCommand()
                    + ",\n   tx:" + unit.txWithPrecision() + ", dist_to_zealot:" + A.dist(distToZealot)
                    + (unit.target == null ? "" : ",\n   dist_to_target:" + A.dist(unit, unit.target))
                    + (unit.targetPosition == null ? "" : ",\n   target_position:" + unit.targetPosition)
                    + "\n   marine eval = " + unit.combatEvalRelative()
                    + "\n   zealot eval = " + zealot.combatEvalRelative()
                );
                System.out.println("_______________________________________");
            }

            assertTrue(isSafe);
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            fake(AUnitType.Terran_Marine, 10)
        );
    }

    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
            zealot = fake(AUnitType.Protoss_Zealot, 15),
            fake(AUnitType.Protoss_Zealot, 18)
        );
    }

}
