package atlantis.tests.unit;

import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelectTest extends AbstractTestWithUnits {

    // === Our ======================================================

    @Test
    public void our() {
        usingFakeOurs(() -> {
            assertEquals(ourUnits.length, Select.our().size());
        });
    }

    @Test
    public void ourRealUnits() {
        usingFakeOurs(() -> {
            assertEquals(GROUND_UNITS + AIR_UNITS, Select.ourRealUnits().size());
        });
    }

    // === Enemy ======================================================

    @Test
    public void enemy() {
        usingFakeEnemy(() -> {
            assertEquals(enemyUnits.length, BaseSelect.enemyUnits().size());
        });
    }

    @Test
    public void enemyRealUnits() {
        usingFakeEnemy(() -> {
            assertEquals(enemyUnits.length, Select.enemyUnits().size());

            assertEquals(
                    0,
                    Select.enemyRealUnits(false, false, false).size()
            );

            assertEquals(
                    GROUND_UNITS,
                    Select.enemyRealUnits(true, false, false).size()
            );

            assertEquals(
                    GROUND_UNITS + BUILDINGS,
                    Select.enemyRealUnits(true, false, true).size()
            );

            assertEquals(
                    AIR_UNITS,
                    Select.enemyRealUnits(false, true, false).size()
            );

            assertEquals(
                    REAL_UNITS,
                    Select.enemyRealUnits(true, true, false).size()
            );

            assertEquals(
                    REAL_UNITS + BUILDINGS,
                    Select.enemyRealUnits(true, true, true).size()
            );
        });
    }

    // === Neutral ======================================================

    @Test
    public void neutralUnits() {
        usingFakeNeutral(() -> {
            assertEquals(MINERAL_COUNT, Select.minerals().size());
            assertEquals(GEYSER_COUNT, Select.geysers().size());

            assertEquals(neutralUnits.length, Select.neutral().size());

            assertEquals(MINERAL_COUNT, Select.minerals().size());
            assertEquals(GEYSER_COUNT, Select.geysers().size());
        });
    }

}
