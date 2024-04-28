package atlantis.information.decisions;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class GG extends Commander {
    @Override
    public boolean applies() {
        return A.s >= 300
            && A.now() % 128 == 0
            && OurArmy.strength() <= 8
            && Count.ourCombatUnits() <= 1
            && Count.workers() <= 30
            && Count.dragoons() == 0
            && Select.enemyCombatUnits().atLeast(10);
    }

    @Override
    protected void handle() {
        AGame.sendMessage("gg");
        A.quit();
    }
}
