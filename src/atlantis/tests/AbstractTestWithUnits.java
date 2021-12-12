package atlantis.tests;

import atlantis.Atlantis;
import org.junit.Before;

public class AbstractTestWithUnits extends UnitTestHelper {

    @Before
    public void before() {
        Atlantis.getInstance().setGame(gameMock());
    }

}
