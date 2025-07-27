package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.DoAvoidEnemies;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.game.player.Enemy;
import atlantis.units.select.Selection;

public class ScoutSeparateFromCloseEnemies extends Manager {
    private HasPosition center;
    private Selection enemies;

    public ScoutSeparateFromCloseEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemies = unit.enemiesNear().nonBuildings().combatUnits().canAttack(unit, safetyMargin());
        center = enemies.limit(2).center();

        return enemies.notEmpty();
    }


    private double safetyMargin() {
        return 2.6
            + (Enemy.zerg() ? 2.9 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 5) ? 3.5 : 0)
            + unit.woundPercent() / 16.0;
    }

    @Override
    public Manager handle() {
//        if (unit.moveAwayFrom(center, 14, Actions.MOVE_SAFETY, "ScoutSeparateA")) return usedManager(this);

        if (center != null) {
            if (unit.moveAwayFrom(center, 6, Actions.MOVE_SAFETY, "ScoutSeparateA")) {
                return usedManager(this);
            }
        }

        if (unit.moveAwayFrom(enemies.nearestTo(unit), 6, Actions.MOVE_SAFETY, "ScoutSeparateB")) {
            return usedManager(this);
        }

        if (
            unit.distToMain() >= 20 && unit.moveToSafety(Actions.MOVE_SAFETY)
        ) return usedManager(this, "Scout2Safety");

//        if (unit.moveAwayFrom(center, 3.5, Actions.MOVE_SAFETY, "ScoutSeparateA")) return usedManager(this);

//        Manager manager = (new DoAvoidEnemies(unit, null)).handle();
//        if (manager != null) return usedManager(this, "ScoutSeparateB");

//        if (unit.enemiesThatCanAttackMe(2.4 + unit.woundPercent() / 30.0).empty()) {
        if (unit.moveToSafety(Actions.MOVE_SAFETY, "ScoutSeparateC")) return usedManager(this);
//        }

        return null;
    }
}
