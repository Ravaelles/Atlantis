package tests.unit;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.PositionUtil;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@Tag("map")
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

        // NEXT #193: choke helpers read ground, which falls back to the
        // Euclidean center distance with no BWEM behind it - so the test
        // world reads 5.0/2.0 (center), not 2.0/-1.0 (width-subtracted
        // air edge). The Euclidean edge answers stay on distTo.
        assertEquals(2.0, zealotA.distTo(choke));
        assertEquals(-1.0, zealotB.distTo(choke));

        assertEquals(5.0, zealotA.nearestChokeDist());
        assertEquals(2.0, zealotB.nearestChokeDist());

        assertEquals(5.0, zealotA.nearestChokeCenterDist());
        assertEquals(2.0, zealotB.nearestChokeCenterDist());

        assertEquals(false, zealotA.isWithinChoke());
        assertEquals(true, zealotB.isWithinChoke());
        assertEquals(3.0, zealotA.distTo(zealotB));
    }

    @Test
    public void chokeHelpersReadGroundNotAir() {
        FakeChoke choke = new FakeChoke(APosition.create(15, 10), 3);
        Chokes.fakeChokes(choke);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 10, 10);

        try (MockedStatic<PositionUtil> mocked = Mockito.mockStatic(PositionUtil.class)) {
            mocked.when(() -> PositionUtil.groundDistanceTo(
                    any(HasPosition.class), any(HasPosition.class)))
                .thenReturn(7.0);

            assertEquals(7.0, zealot.nearestChokeDist(), "live game reads the ground path");
            assertEquals(7.0, zealot.nearestChokeCenterDist(), "live game reads the ground path");
        }
    }

    @Test
    public void flyersReadFarFromChokes() {
        FakeChoke choke = new FakeChoke(APosition.create(15, 10), 3);
        Chokes.fakeChokes(choke);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 13, 10);

        assertEquals(999.0, observer.nearestChokeDist(), "chokes are ground concepts");
        assertEquals(999.0, observer.nearestChokeCenterDist(), "chokes are ground concepts");
        assertEquals(false, observer.isWithinChoke(), "a flyer is never inside a choke");
    }

    @Test
    public void distToMainChokeOrReadsGroundWithFlyerFallback() {
        FakeChoke mainChoke = new FakeChoke(APosition.create(15, 10), 3);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 10, 10);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 13, 10);

        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
             MockedStatic<PositionUtil> mocked = Mockito.mockStatic(PositionUtil.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(mainChoke);
            mocked.when(() -> PositionUtil.groundDistanceTo(
                    any(HasPosition.class), any(AChoke.class)))
                .thenReturn(7.0);

            assertEquals(7.0, zealot.distToMainChokeOr(123), "live game reads the ground path");
            assertEquals(123, observer.distToMainChokeOr(123), "a flyer takes the fallback");
        }
    }
}
