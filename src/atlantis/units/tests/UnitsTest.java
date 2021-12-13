package atlantis.units.tests;

import atlantis.units.select.AbstractTestWithUnits;
import atlantis.units.AUnit;
import atlantis.units.Units;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UnitsTest extends AbstractTestWithUnits {

    @Test
    public void testCloneDoesNotModifyOriginal() {
        List<AUnit> unitsList = generateUnitsList(true);
        int size = unitsList.size();

        Units original = new Units(unitsList);

        assertEquals(size, original.size());

        Units clone = original.clone();

        assertEquals(size, clone.size());

        clone.removeUnit(unitsList.get(0));

        assertEquals(size - 1, clone.size());
        assertEquals(size, original.size());
    }

}
