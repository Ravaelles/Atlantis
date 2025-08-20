package tests.unit;

import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(zealotA.distTo(choke), zealotA.nearestChokeDist());
        assertEquals(zealotB.distTo(choke), zealotB.nearestChokeDist());

        assertEquals(2.0, zealotA.distTo(choke));
        assertEquals(-1.0, zealotB.distTo(choke));

        assertEquals(2.0, zealotA.nearestChokeDist());
        assertEquals(-1.0, zealotB.nearestChokeDist());

        assertEquals(5.0, zealotA.nearestChokeCenterDist());
        assertEquals(2.0, zealotB.nearestChokeCenterDist());

        assertEquals(false, zealotA.isWithinChoke());
        assertEquals(true, zealotB.isWithinChoke());
        assertEquals(3.0, zealotA.distTo(zealotB));
    }
}
