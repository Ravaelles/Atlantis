package tests.fakes;

import atlantis.map.base.ABaseLocation;

import java.util.ArrayList;
import java.util.List;

public class FakeBaseLocations {
    /**
     * mainBase = BaseLocation at (7, 44) (start_loc)
     * 116, 68: , GD: 143.0
     * 7, 44: , GD: 0.0
     * 93, 118: , GD: 165.9
     * 117, 9: , GD: 161.1
     * 50, 6: , GD: 65.3
     * 23, 100: , GD: 116.6
     * 82, 6: , GD: 97.9
     * 7, 78: , GD: 103.1
     * 112, 93: , GD: 162.7
     * 14, 13: , GD: 32.1
     * 103, 37: , GD: 130.1
     * 51, 113: , GD: 131.8
     */
    public static List<ABaseLocation> get() {
        List<ABaseLocation> baseLocations = new ArrayList<>();
        baseLocations.add(new FakeBaseLocation(116, 68, false, 143.0));
        baseLocations.add(new FakeBaseLocation(7, 44, true, 0)); // Main
        baseLocations.add(new FakeBaseLocation(93, 118, true, 165.9));
        baseLocations.add(new FakeBaseLocation(117, 9, true, 161.1));
        baseLocations.add(new FakeBaseLocation(50, 6, false, 65.3)); // Third (closest)
        baseLocations.add(new FakeBaseLocation(23, 100, false, 116.6));
        baseLocations.add(new FakeBaseLocation(82, 6, false, 97.9));
        baseLocations.add(new FakeBaseLocation(7, 78, false, 103.1));
        baseLocations.add(new FakeBaseLocation(112, 93, false, 162.7));
        baseLocations.add(new FakeBaseLocation(14, 13, false, 32.1)); // Natural
        baseLocations.add(new FakeBaseLocation(103, 37, false, 130.1));
        baseLocations.add(new FakeBaseLocation(51, 113, false, 131.8));
        return baseLocations;
    }
}
