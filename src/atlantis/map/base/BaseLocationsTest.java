package atlantis.map.base;

import atlantis.map.position.APosition;
import atlantis.util.Options;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseLocationsTest extends WorldStubForTests {
    @Test
    public void testNearestTo() {
        ABaseLocation base1 = BaseLocations.nearestTo(APosition.create(9, 46));
        ABaseLocation base2 = BaseLocations.nearestTo(APosition.create(9, 2));

        assertEquals(7, base1.tx());
        assertEquals(44, base1.ty());
        assertEquals(14, base2.tx());
        assertEquals(13, base2.ty());
    }

    @Test
    public void testNearestUnexploredStartingLocation() {
        FakeUnit base = fake(Protoss_Nexus, 9, 46);

        createWorld(1,
            () -> {
                APosition.TESTING_EXPLORED = false;
                APosition position = BaseLocations.nearestUnexploredStartingLocation(base);
                APosition.TESTING_EXPLORED = true;
//                System.err.println("position = " + position);

                assertNotNull(position);
            },
            () -> fakeOurs(
                base,
                fake(Protoss_Pylon, 11, 48)
            ),
            () -> fakeExampleEnemies(),
            Options.create().set("supplyUsed", 49)
        );
    }
}
