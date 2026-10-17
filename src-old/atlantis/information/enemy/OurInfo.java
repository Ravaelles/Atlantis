package atlantis.information.enemy;

import atlantis.units.select.Count;
import atlantis.util.We;

public class OurInfo {
    public static boolean hasMobileDetection() {
        if (We.protoss()) return Count.observers() > 0;
        if (We.terran()) return Count.scienceVessels() > 0;
        return true;
    }
}
