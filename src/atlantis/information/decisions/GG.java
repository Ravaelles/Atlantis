package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class GG extends Commander {
    @Override
    public boolean applies() {
        return (A.s >= 400 || Count.workers() == 0)
            && A.now() % 128 == 0
            && (Army.strength() <= 8 || Count.workers() <= 10)
//            && Count.workers() <= 35
            && Select.enemyCombatUnits().atLeast(15)
//            && Count.dragoons() <= 2
            && (Count.ourCombatUnits() * 8) <= EnemyUnits.combatUnits()
            && (Count.ourCombatUnits() + Count.workers() <= 30);
    }

    @Override
    protected void handle() {
        AGame.sendMessage("gg");
        Atlantis.getInstance().onEnd(false);
    }
}
