package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ScoutEnemyNaturalIfNotExisting extends Manager {
    private HasPosition enemyNatural;
    private int lastVisibleAt = -9388;

    public ScoutEnemyNaturalIfNotExisting(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemyNatural = EnemyInfo.enemyNaturalBase();
        if (enemyNatural != null) return false;

        enemyNatural = EnemyInfo.enemyNatural();
        if (enemyNatural == null) return false;

        if (enemyNatural.isPositionVisible()) lastVisibleAt = A.now;

        // =========================================================

        if (unit.enemiesThatCanAttackMe(5 + unit.woundPercent() / 50.0).notEmpty()) {
            return false;
        }

        return A.ago(lastVisibleAt) >= 15 * 30;
    }

    @Override
    public Manager handle() {
        if (unit.move(enemyNatural, Actions.MOVE_SCOUT)) {
            return usedManager(this);
        }

        return null;
    }
}
