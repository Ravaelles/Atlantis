package atlantis.information.decisions.zerg;

import atlantis.game.AGame;
import atlantis.units.select.Count;

public class ShouldMakeZerglings {

    public static boolean should() {
        int zerglings = Count.zerglings();

        if (zerglings < 8) {
            return true;
        }

        return AGame.canAffordWithReserved(75, 0);
    }

}
