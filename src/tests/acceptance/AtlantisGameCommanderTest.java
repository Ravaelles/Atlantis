package tests.acceptance;

import atlantis.debug.profiler.CodeProfiler;
import atlantis.game.AtlantisGameCommander;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtlantisGameCommanderTest extends AbstractTestWithWorld {
    @Test
    public void mainGameLoopWorksAsExpected() {
        createWorld(1, () -> {
            (new AtlantisGameCommander()).invokeCommander();

            CodeProfiler.printSummary();

            assertTrue(true);
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            fake(AUnitType.Terran_Marine, 10),
            fake(AUnitType.Terran_Vulture, 11)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 19;
        return fakeEnemies(
            fake(AUnitType.Zerg_Sunken_Colony, enemyTy),
            fake(AUnitType.Zerg_Sunken_Colony, enemyTy + 10)
        );
    }

}
