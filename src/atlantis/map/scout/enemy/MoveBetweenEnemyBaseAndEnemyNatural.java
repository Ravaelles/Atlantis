package atlantis.map.scout.enemy;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.position.APosition;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MoveBetweenEnemyBaseAndEnemyNatural extends Manager {
    private static CurrentlyGoTo currentlyGoTo = CurrentlyGoTo.ENEMY_MAIN;
    private APosition enemyMain;
    private APosition enemyNatural;


    public MoveBetweenEnemyBaseAndEnemyNatural(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (EnemyInfo.combatBuildingsAntiLand() > 0) return false;
        if (ScoutState.scoutsKilledCount >= 2) return false;

        enemyMain = EnemyMainBase.get();
        enemyNatural = BaseLocations.enemyNatural();

        return enemyMain != null || enemyNatural != null;
    }

    @Override
    public Manager handle() {
        if (shouldGoToMain() && goToMain()) {
            return usedManager(this, "ScoutEnemyMain");
        }
        if (shouldGoToNatural() && goToNatural()) {
            return usedManager(this, "ScoutEnemyNatural");
        }

        return null;
    }

    // === Main ===========================================

    private boolean goToMain() {
        return unit.move(enemyMain, Actions.MOVE_SCOUT);
    }

    private boolean shouldGoToMain() {
        if (enemyMain == null) return false;

        double dist = unit.distTo(enemyMain);
        if (dist < 4) {
            currentlyGoTo = CurrentlyGoTo.ENEMY_NATURAL;
            return false;
        }

        return currentlyGoTo == CurrentlyGoTo.ENEMY_MAIN;
    }

    // === Natural ===========================================

    private boolean shouldGoToNatural() {
        if (enemyNatural == null) return false;

        double dist = unit.distTo(enemyNatural);
        if (dist < 4) {
            currentlyGoTo = CurrentlyGoTo.ENEMY_MAIN;
            return false;
        }

        return currentlyGoTo == CurrentlyGoTo.ENEMY_NATURAL;
    }

    private boolean goToNatural() {
        return unit.move(enemyNatural, Actions.MOVE_SCOUT);
    }
}

enum CurrentlyGoTo {
    ENEMY_MAIN,
    ENEMY_NATURAL
}