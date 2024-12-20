package atlantis.map.choke;

import atlantis.map.AMap;
import bwem.ChokePoint;

import java.util.ArrayList;
import java.util.List;

public class AllChokes {
    public static List<AChoke> get() {
        List<AChoke> chokes = new ArrayList<>();
        for (ChokePoint chokePoint : AMap.getMap().chokes()) {
            AChoke choke = AChoke.from(chokePoint);
            if (isOk(choke)) {
                chokes.add(choke);
            }
        }
        return chokes;
    }

    private static boolean isOk(AChoke choke) {
        return choke.width() >= 1;
    }
}
