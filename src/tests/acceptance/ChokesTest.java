package tests.acceptance;

import atlantis.map.choke.Chokes;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

public class ChokesTest extends WorldStubForTests {
    @Test
    public void testMainAndNaturalChoke() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 7, 44)), fakeEnemies(), () -> {
            System.err.println("main choke = " + Chokes.mainChoke());
            System.err.println("natural choke = " + Chokes.natural());
        });
    }
}
