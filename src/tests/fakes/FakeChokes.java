package tests.fakes;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;

import java.util.ArrayList;
import java.util.List;

public class FakeChokes {
    /**
     * tx, ty, width
     * 77, 72, 5
     * 44, 16, 17
     * 61, 60, 5
     * 53, 74, 10
     * 40, 56, 15
     * 31, 96, 17
     * 21, 76, 14
     * 20, 88, 5
     * 110, 79, 5
     * 99, 88, 14
     * 13, 25, 1
     * 28, 18, 6
     * 74, 33, 12
     * 63, 18, 5
     * 79, 21, 12
     * 107, 44, 6
     * 117, 35, 2
     * 51, 106, 6
     * 67, 114, 2
     * 29, 114, 6
     * 119, 103, 5
     * <p>
     * Regex: (\d+), (\d+), (\d+)
     * chokes.add\(new FakeChoke\(APosition.create\($1, $2\), $3\)\);
     */
    public static List<AChoke> get() {
        List<AChoke> chokes = new ArrayList<>();
        chokes.add(new FakeChoke(APosition.create(77, 72), 5));
        chokes.add(new FakeChoke(APosition.create(44, 16), 17));
        chokes.add(new FakeChoke(APosition.create(61, 60), 5));
        chokes.add(new FakeChoke(APosition.create(53, 74), 10));
        chokes.add(new FakeChoke(APosition.create(40, 56), 15));
        chokes.add(new FakeChoke(APosition.create(31, 96), 17));
        chokes.add(new FakeChoke(APosition.create(21, 76), 14));
        chokes.add(new FakeChoke(APosition.create(20, 88), 5));
        chokes.add(new FakeChoke(APosition.create(110, 79), 5));
        chokes.add(new FakeChoke(APosition.create(99, 88), 14));
        chokes.add(new FakeChoke(APosition.create(13, 25), 1));
        chokes.add(new FakeChoke(APosition.create(28, 18), 6));
        chokes.add(new FakeChoke(APosition.create(74, 33), 12));
        chokes.add(new FakeChoke(APosition.create(63, 18), 5));
        chokes.add(new FakeChoke(APosition.create(79, 21), 12));
        chokes.add(new FakeChoke(APosition.create(107, 44), 6));
        chokes.add(new FakeChoke(APosition.create(117, 35), 2));
        chokes.add(new FakeChoke(APosition.create(51, 106), 6));
        chokes.add(new FakeChoke(APosition.create(67, 114), 2));
        chokes.add(new FakeChoke(APosition.create(29, 114), 6));
        chokes.add(new FakeChoke(APosition.create(119, 103), 5));
        return chokes;
    }
}
