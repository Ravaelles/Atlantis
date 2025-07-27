package atlantis.combat.advance.focus_choke;

import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;

import java.util.ArrayList;

public class MiddleFocusChoke {
    public static AChoke get() {
        ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();

        int index = chokes.size() / 2 + 1;

        if (index >= 3 && index < chokes.size() - 2) {
            return chokes.get(index);
        }

        return null;
    }
}
