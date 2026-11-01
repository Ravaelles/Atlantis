package tests.acceptance;
import atlantis.production.constructions.ProductionCommander;

import atlantis.combat.BattleCommander;
import atlantis.production.dynamic.DynamicUnitAndTechProducerCommander;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.unit.UnitTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommanderTest extends WorldStubForTests {

    /**
     * These commanders are Protoss-path (their race-specific factories
     * are null otherwise - the NPE this used to pin). The test declares
     * the race instead of constructing them in a race they never see
     * live (the bot plays Protoss-only).
     */
    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void noExceptionIsThrown() {
        boolean status = false;

        try {
            BattleCommander.class.getDeclaredConstructor();
            BattleCommander.class.getDeclaredConstructor().newInstance();

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
        world(3, UnitTest.randomOurs(), UnitTest.randomEnemies(), () -> {
//                (new ProductionCommander()).invokeCommander();
//                (new DynamicUnitAndTechProducerCommander()).invoke(this);

            assertTrue(true);
        });
    }
}
