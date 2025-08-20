package tests.unit;

import atlantis.units.select.BaseSelect;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.AbstractTestWithWorld;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BaseSelectTest extends WorldStubForTests {
    @Test
    public void ourUnits() {
        ourUnits = fakeExampleOurs();
        enemyUnits = fakeExampleEnemies();

        createWorld(ourUnits, enemyUnits, () -> {
            assertNotEquals(ourUnits.length, 0);
            assertEquals(ourUnits.length, BaseSelect.ourUnitsWithUnfinishedList().size());
        });
    }

    @Test
    public void enemyUnits() {
        ourUnits = fakeExampleOurs();
        enemyUnits = fakeExampleEnemies();

        createWorld(ourUnits, enemyUnits, () -> {
            assertNotEquals(enemyUnits.length, 0);
            assertEquals(enemyUnits.length, BaseSelect.enemyUnits().size());
        });
    }

    @Test
    public void neutralUnits() {
        AbstractTestWithWorld.baseSelect.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());

        assertNotEquals(neutralUnits.length, 0);
        assertEquals(neutralUnits.length, BaseSelect.neutralUnits().size());
    }

}
