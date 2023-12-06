package atlantis.terran.chokeblockers;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;

public class ChokeToBlock {
    public static AChoke get() {
        AChoke choke = Chokes.mainChoke();

        if (choke == null || choke.width() >= 4.5) return null;

        return choke;
    }
}
