package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.UnitsArchive;
import atlantis.information.generic.Army;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.util.log.ErrorLog;

public class GGForEnemy extends Commander {
    public static boolean allowed = false;

    @Override
    public boolean applies() {
        if (true) return false; // Disabled, but fully working

        if (!allowed) return false;
        if (UnitsArchive.enemyBasesDestroyed() == 0) return false;

        if (A.s >= 60 * 13) return false;
        if (EnemyInfo.combatBuildingsAntiLand() > 0) return false;

        if (Enemy.zerg() && A.s >= 60 * 7 && Atlantis.KILLED_BUILDINGS <= 4) {
            return false;
        }

        if (
            A.resourcesBalance() >= 4500
                && Army.strength() >= 910
                && Count.ourCombatUnits() >= 30
                && EnemyUnits.combatUnits() <= 0
                && EnemyUnits.discovered().combatBuildingsAntiLand().empty()
        ) {
            return true;
        }

        return A.s >= 600
            && A.now() % 128 == 0
            && (Army.strength() >= 900 || A.resourcesBalance() >= 3000)
            && (A.supplyUsed() >= 150 || A.minerals() >= 1500 || A.resourcesBalance() >= 4000)
            && EnemyUnits.combatUnits() <= 0
            && Count.ourCombatUnits() >= 25
            && Select.enemy().count() <= 5 // Make sure any enemy units are visible
            && Count.workers() >= 30
            && Select.enemy().workers().count() <= 5 // Make sure any enemy units are visible
//            && (Count.ourCombatUnits() * 13) >= EnemyUnits.combatUnits()
            && EnemyUnits.combatUnits() <= 0;
    }

    @Override
    protected void handle() {
        AGame.sendMessage("We won - force GG for enemy");
//        AGame.exit("We won - force GG for enemy, our strength: " + Army.strength());
        ErrorLog.printErrorOnce("We won - force GG for enemy, our strength: " + Army.strength());
        Atlantis.getInstance().onEnd(true);
    }
}
