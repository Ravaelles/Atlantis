package atlantis.map.choke;

import atlantis.units.AUnitType;
import org.junit.Test;
import tests.acceptance.NonAbstractTestFakingGame;

public class ChokesTest extends NonAbstractTestFakingGame {
    @Test
    public void testMainAndNaturalChoke() {
        createWorld(1,
            () -> {
                System.err.println("main choke = " + Chokes.mainChoke());
                System.err.println("natural choke = " + Chokes.natural());
            },
            () -> fakeOurs(fake(AUnitType.Protoss_Nexus, 7, 44)),
            () -> fakeEnemies()
        );
    }
}
