package tests.unit;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static junit.framework.TestCase.assertEquals;

public class ChokeTest extends AbstractTestWithUnits {
    @Test
    public void distToChokes() {
        FakeChoke choke = new FakeChoke(APosition.create(15, 10), 3);
        Chokes.fakeChokes(choke);

        FakeUnit zealotA = fake(AUnitType.Protoss_Zealot, 10, 10);
        FakeUnit zealotB = fake(AUnitType.Protoss_Zealot, 13, 10);

//        double distToChoke = zealotA.distTo(choke);
//        System.err.println("choke = " + choke + " / tx:" + choke.tx() + ", ty:" + choke.ty());
//        System.err.println("zealotA = " + zealotA);
//        System.out.println(distToChoke);

        assertEquals(zealotA.distTo(choke), zealotA.distToNearestChoke());
        assertEquals(zealotB.distTo(choke), zealotB.distToNearestChoke());

        assertEquals(2.0, zealotA.distTo(choke));
        assertEquals(-1.0, zealotB.distTo(choke));

        assertEquals(2.0, zealotA.distToNearestChoke());
        assertEquals(-1.0, zealotB.distToNearestChoke());

        assertEquals(5.0, zealotA.distToNearestChokeCenter());
        assertEquals(2.0, zealotB.distToNearestChokeCenter());

        assertEquals(false, zealotA.isWithinChoke());
        assertEquals(true, zealotB.isWithinChoke());
        assertEquals(3.0, zealotA.distTo(zealotB));
    }
}
