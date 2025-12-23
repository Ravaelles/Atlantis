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
        return 6
            + (Enemy.zerg() ? 2 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 15) ? 2.5 : 0)
            + unit.woundPercent() / 10.0;
    }

    @Override
    public Manager handle() {
//        if (unit.moveAwayFrom(center, 14, Actions.MOVE_SAFETY, "ScoutSeparateA")) return usedManager(this);

        Manager manager = (new DoAvoidEnemies(unit, null)).handle();
        if (manager != null) return usedManager(this, "ScoutSeparateS");

        AUnit nearestEnemy = nearestEnemy();
        if (nearestEnemy != null && nearestEnemy.distTo(unit) >= (unit.shieldHealthy() ? 3.8 : 6.5)) {
            if (
//                unit.distToMain() >= 60 &&
                unit.moveToSafety(Actions.RUN_ENEMIES)
            ) return usedManager(this, "Scout2Safety");
        }


//        if (
//            nearestEnemy != null
//                && unit.moveAwayFrom(center, 7, Actions.RUN_ENEMIES, "ScoutSeparateN")
//        ) {
//            return usedManager(this);
//        }

//        if (unit.moveAwayFrom(enemies.nearestTo(unit), 6, Actions.RUN_ENEMIES, "ScoutSeparateB")) {
//            return usedManager(this);
//        }

//        if (unit.moveAwayFrom(center, 3.5, Actions.MOVE_SAFETY, "ScoutSeparateA")) return usedManager(this);

        if (center != null) {
            if (unit.moveAwayFrom(center, 6, Actions.RUN_ENEMIES, "ScoutSeparateA")) {
                return usedManager(this);
            }
        }

//        if (unit.enemiesThatCanAttackMe(2.4 + unit.woundPercent() / 30.0).empty()) {
        if (unit.moveToMain(Actions.RUN_ENEMIES)) return usedManager(this);
//        }

        return null;
    }

    private AUnit nearestEnemy() {
        if (unit.isHealthy() && !ScoutCommander.hasAnyScoutBeenKilled()) {
            return enemies.visibleOnMapOrCombatBuilding().havingAtLeastHp(1).nearestTo(unit);
        }

        return enemies.nearestTo(unit);
    }
}
