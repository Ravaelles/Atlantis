package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class GG extends Commander {
    @Override
    public boolean applies() {
        return A.s >= 400
            && A.now() % 128 == 0
            && OurArmy.strength() <= 8
            && Count.workers() <= 35
            && Select.enemyCombatUnits().atLeast(15)
//            && Count.dragoons() <= 2
            && (Count.ourCombatUnits() * 8) <= EnemyUnits.combatUnits();
    }

    @Override
    protected void handle() {
        AGame.sendMessage("gg");
        Atlantis.getInstance().onEnd(false);
    }
}
