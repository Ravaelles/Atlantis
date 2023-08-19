package tests.acceptance;

import atlantis.combat.CombatCommander;
import atlantis.production.ProductionCommander;
import atlantis.production.dynamic.DynamicProductionOfUnitsCommander;
import org.junit.Test;
import tests.unit.UnitTestHelper;

import static org.junit.Assert.assertTrue;

public class CommanderTest extends NonAbstractTestFakingGame {
    @Test
    public void noExceptionIsThrown() {
        boolean status = false;

        try {
            CombatCommander.class.getDeclaredConstructor();
            CombatCommander.class.getDeclaredConstructor().newInstance();

            DynamicProductionOfUnitsCommander.class.getDeclaredConstructor();
            DynamicProductionOfUnitsCommander.class.getDeclaredConstructor().newInstance();

            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(status);
    }

    @Test
    public void commanderHandlers() {
        createWorld(3, () -> {
                (new ProductionCommander()).invoke();
//                (new DynamicProductionOfUnitsCommander()).invoke();

                assertTrue(true);
            },
            () -> UnitTestHelper.randomOurs(),
            () -> UnitTestHelper.randomEnemies()
        );
    }
}
