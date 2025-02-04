package tests.acceptance;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvoidsEnemyUnitsTest extends AbstractTestWithWorld {
    private FakeUnit zealot;

    /**
     * With buildings there is a problem - JFAP returns "no danger" status
     * if the unit is 0.1 tiles outside of range of a Sunken Colony, but the
     * eval gets drastically worse once within range.
     */
    @Test
    public void marinesAreAvoidingZealots() {
        createWorld(70, () -> {
//            Select.our().print();
//            Select.enemy().print();

            FakeUnit unit = ourFirst;
            unit.setSquad(Alpha.get());
            (new CombatUnitManager(unit)).invokeFrom(this);

            double distToZealot = distToNearestEnemy(unit);
            boolean isSafe = distToZealot > 1.1;
//            boolean alwaysShow = false;
            boolean alwaysShow = true;

            if (!isSafe || alwaysShow) {
                System.err.println(A.now()
                    + " -       " + unit.tooltip()
                    + "\n   Manager : " + unit.manager()
                    + "\n   Managers: " + unit.managerLogs().toString()
                    + "\n   Command : " + unit.lastCommand()
                    + "\n   tooltip: " + unit.tooltip() + " / " + unit.lastCommand()
                    + ",\n   tx:" + unit.txWithPrecision() + ", dist_to_zealot:" + A.dist(distToZealot)
                    + (unit.target == null ? "" : ",\n   dist_to_target:" + A.dist(unit, unit.target))
                    + (unit.targetPosition == null ? "" : ",\n   target_position:" + unit.targetPosition)
                    + "\n   marine eval = " + unit.eval()
                    + "\n   zealot eval = " + zealot.eval()
                );
                System.err.println("_______________________________________");
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
            zealot = fake(AUnitType.Protoss_Zealot, 14),
            fake(AUnitType.Protoss_Zealot, 18)
        );
    }

}
