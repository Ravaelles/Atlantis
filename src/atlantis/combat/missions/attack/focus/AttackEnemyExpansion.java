package atlantis.combat.missions.attack.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.player.Enemy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

import java.util.List;

public class AttackEnemyExpansion {
    public boolean shouldFocusIt() {
        APosition expansion = expansion();
        if (expansion == null) return false;
//        if (A.s <= 60 * 4) return false;

        return (!expansion.isPositionVisible() || Select.enemyRealUnits().groundUnits().inRadius(10, expansion).notEmpty())
            && !forbidEngagingIfZergSquadCloserToOurMainThanAlpha();
    }

    public AFocusPoint expansion() {
        HasPosition expansion = EnemyExistingExpansion.get();
        if (expansion == null || !expansion.hasPosition()) return null;

        if (!expansion.isWalkable() && !(expansion instanceof AUnit)) {
            ErrorLog.printMaxOncePerMinute("Enemy expansion is not walkable: " + expansion);
            return null;
        }

//        A.errPrintln(A.s + "s:  ENEMY EXPANSION FOUND: " + expansion);

        if (
            expansion.isPositionVisible()
                && (
                Select.enemy().buildings().inRadius(AUnit.NEAR_DIST, expansion).empty()
            )
        ) return null;

        return new AFocusPoint(
            expansion,
            "AttackExpansion"
        );
    }

    private boolean forbidEngagingIfZergSquadCloserToOurMainThanAlpha() {
        if (!Enemy.zerg()) return false;

        HasPosition alphaCenter = Alpha.alphaCenter();
        if (alphaCenter == null) return false;

        List<AUnit> enemyUnits = Select.enemyCombatUnits().groundUnits().list();
        if (enemyUnits.size() <= 8) return false;

        int enemiesCloserToMain = countEnemiesGroundCloserToOurMainThan(alphaCenter.groundDistToMain(), enemyUnits);

        return enemiesCloserToMain >= Alpha.count() / 4;
    }

    private int countEnemiesGroundCloserToOurMainThan(double minDist, List<AUnit> enemies) {
        int total = 0;
        AUnit main = Select.mainOrAnyBuilding();

        for (AUnit enemy : enemies) {
            if (enemy.groundDist(main) <= minDist) total++;
        }

        return total;
    }
}
