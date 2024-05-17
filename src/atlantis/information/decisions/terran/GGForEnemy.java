package atlantis.information.decisions.terran;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class GGForEnemy extends Commander {
    public static boolean allowed = false;

    @Override
    public boolean applies() {
        return allowed
            && A.resourcesBalance() >= 2000
            && A.s >= 300
            && A.now() % 128 == 0
            && OurArmy.strength() >= 800
            && Count.ourCombatUnits() >= 30
            && EnemyUnits.combatUnits() <= 3
            && Count.workers() >= 35
            && (Count.ourCombatUnits() * 13) >= EnemyUnits.combatUnits();
    }

    @Override
    protected void handle() {
        AGame.sendMessage("We won - force GG for enemy");
        A.quit();
    }
}
