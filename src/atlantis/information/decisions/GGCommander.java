package atlantis.information.decisions;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.generic.OurArmy;
import atlantis.units.select.Count;

public class GGCommander extends Commander {
    @Override
    public boolean applies() {
        return A.s >= 300
            && A.now() % 128 == 0
            && Count.ourCombatUnits() <= 1
            && OurArmy.strength() <= 5;
    }

    @Override
    protected void handle() {
        AGame.sendMessage("GG");
        A.quit();
    }
}
