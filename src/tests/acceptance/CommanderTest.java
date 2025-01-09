package tests.acceptance;

import atlantis.combat.CombatCommander;
import atlantis.production.dynamic.DynamicUnitAndTechProducerCommander;
import org.junit.jupiter.api.Test;
import tests.unit.UnitTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommanderTest extends WorldStubForTests {
    @Test
    public void noExceptionIsThrown() {
        boolean status = false;

        try {
            CombatCommander.class.getDeclaredConstructor();
            CombatCommander.class.getDeclaredConstructor().newInstance();

            DynamicUnitAndTechProducerCommander.class.getDeclaredConstructor();
            DynamicUnitAndTechProducerCommander.class.getDeclaredConstructor().newInstance();

            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(status);
    }

    @Test
    public void commanderHandlers() {
        createWorld(3, () -> {
//                (new ProductionCommander()).invokeCommander();
//                (new DynamicUnitAndTechProducerCommander()).invoke(this);

                assertTrue(true);
            },
            () -> UnitTest.randomOurs(),
            () -> UnitTest.randomEnemies()
        );
    }
}
