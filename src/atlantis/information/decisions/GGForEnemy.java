package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.debug.profiler.RealTime;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class GGForEnemy extends Commander {
    public static boolean allowed = false;

    @Override
    public boolean applies() {
        return allowed
            && A.s >= 600
            && A.now() % 128 == 0
            && (OurArmy.strength() >= 900 || A.resourcesBalance() >= 2000)
            && Count.ourCombatUnits() >= 25
            && EnemyUnits.combatUnits() <= 3
            && Select.enemy().count() >= 5 // Make sure any enemy units are visible
            && Count.workers() >= 30
            && (Count.ourCombatUnits() * 13) >= EnemyUnits.combatUnits();
    }

    @Override
    protected void handle() {
        AGame.sendMessage("We won - force GG for enemy");
//        AGame.exit("We won - force GG for enemy, our strength: " + OurArmy.strength());
        ErrorLog.printErrorOnce("We won - force GG for enemy, our strength: " + OurArmy.strength());
        Atlantis.getInstance().onEnd(true);
    }
}
