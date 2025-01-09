package atlantis.game.state;

import atlantis.architecture.Commander;
import atlantis.units.attacked_by.Bullets;

public class BulletsCommander extends Commander {
    @Override
    protected void handle() {
        Bullets.updateKnown();
    }
}
