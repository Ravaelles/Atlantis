package atlantis.terran.chokeblockers;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;

public class ChokeToBlock {
    public static AChoke get() {
        AChoke choke = Chokes.mainChoke();

        if (choke == null || choke.width() >= 4.1) return null;

        return choke;
    }
}
