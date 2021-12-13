package atlantis.units.select;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelectionTest extends AbstractTestWithUnits {

    @Test
    public void ranged() {
        usingMockedOurs(() -> {
//            Select.our().ranged().print();
            assertEquals(8, Select.our().ranged().size());
        });
    }

    @Test
    public void melee() {
        usingMockedOurs(() -> {
//            Select.our().melee().print();
            assertEquals(3, Select.our().melee().size());
        });
    }

}
