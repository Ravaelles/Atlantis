package atlantis.combat.missions.attack.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.EnemyNaturalBase;
import atlantis.map.position.APosition;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;

public class AttackEnemyThird {
    private int _enemyThirdLastVisibleAtS = -1;

    public boolean shouldFocusIt() {
        if (Army.strength() >= 700) return false;
        if (A.supplyUsed() <= 45) return false;

        APosition enemyThird = enemyThird();
        if (enemyThird == null) return false;

        boolean enemyThirdVisible = enemyThird.isPositionVisible();
        if (enemyThirdVisible) _enemyThirdLastVisibleAtS = A.s;

        if (A.s <= (Enemy.zerg() ? 220 : 300)) return false;

        if (Enemy.zerg()) {
            if (!enemyThird.isExplored()) return true;

            if (EnemyNaturalBase.get() == null && A.s % 40 <= 28) return false;

            if (!enemyThirdVisible && lastSeenSecondsAgo() >= (A.supplyUsed() <= 70 ? 40 : 35)) return true;
        }

        if (enemyThirdVisible) {
            if (Select.enemyRealUnits().groundUnits().inRadius(10, enemyThird).empty()) return false;
            else return true;
        }

        if (lastSeenSecondsAgo() <= 20) return false;
        if (!EnemyInfo.hasDefensiveLandBuilding(true)) return false;
        return true;

//        if (Army.strength() >= 600 && A.seconds() % 30 <= 10) return false;
//        if (EnemyUnits.discovered().combatBuildingsAntiLand().empty() && A.seconds() % 20 <= 9) return null;
//        else return true;


//        boolean periodicallyCheckThird = A.s % 40 <= 10;
//
//        if (EnemyInfo.hasDefensiveLandBuilding(true) || periodicallyCheckThird) {
//            if (periodicallyCheckThird && A.supplyUsed() >= 130 && A.supplyUsed() <= 192) return true;
//            if (Count.ourCombatUnits() >= 15 && !enemyThird.isExplored()) return true;
//        }
//
//        if (Enemy.zerg() && A.s % 36 <= 10) return true;
//
//        HasPosition alphaCenter = Alpha.alphaCenter();
//        if (alphaCenter != null && alphaCenter.distTo(enemyThird) >= 20) return true;
//
//        if (
//            EnemyInfo.hasNaturalBase()
//                && EnemyInfo.hasDefensiveLandBuilding(true)
//        ) return true;
//
//        return Army.strength() <= 400 && EnemyInfo.hasDefensiveLandBuilding(true);
    }

    private double lastSeenSecondsAgo() {
        return A.secondsAgo(_enemyThirdLastVisibleAtS);
    }

    public AFocusPoint enemyThird() {
        APosition enemyThird = BaseLocations.enemyThird();
        if (enemyThird == null || !enemyThird.hasPosition()) return null;

        if (
            enemyThird.isPositionVisible()
                && (
                Select.enemy().buildings().inRadius(8, enemyThird).empty()
            )
        ) return null;

        if (A.seconds() % 30 <= 12 && EnemyUnits.discovered().combatBuildingsAntiLand().empty()) return null;

        return new AFocusPoint(
            enemyThird,
            Select.mainOrAnyBuilding(),
            "AttackEnemyThird"
        );
    }
}
