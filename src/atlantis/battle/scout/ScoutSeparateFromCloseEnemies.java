package atlantis.battle.scout;

import atlantis.architecture.Manager;
import atlantis.battle.avoid.DoAvoidEnemies;
import atlantis.map.scout.ScoutCommander;
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
        enemies = unit.enemiesNear().havingWeapon().canAttack(unit, safetyMargin());
        center = enemies.limit(2).center();

        return enemies.notEmpty();
    }


    /**
     * Chase band (owner 2026-10-23, NEXT #187): an enemy chases us while it
     * can hit us from (its range + 3 tiles). Beyond that the scout holds
     * its circle around the enemy main instead of donating the intel.
     */
    private double safetyMargin() {
        return 3
            + (Enemy.zerg() ? 2 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 15) ? 2.5 : 0)
            + unit.woundPercent() / 10.0;
    }

    @Override
    public Manager handle() {
//        if (unit.moveAwayFrom(center, 14, Actions.MOVE_SAFETY, "ScoutSeparateA")) return usedManager(this);

        // Hurt and chased (owner 2026-10-23, NEXT #187): home first, micro
        // second. A dodge alone keeps a slow scout next to a faster pursuer;
        // the home order sticks while the scout can still walk it.
        if (isHurt() && unit.moveToMain(Actions.RUN_ENEMY)) return usedManager(this, "ScoutHurtHome");

        Manager manager = (new DoAvoidEnemies(unit, null)).handle();
        if (manager != null) return usedManager(this, "ScoutSeparateS");

        AUnit nearestEnemy = nearestEnemy();
        if (nearestEnemy == null) return null;

        if (nearestEnemy.distTo(unit) >= (unit.shieldHealthy() ? 3.8 : 6.5)) {
            if (
//                unit.distToMain() >= 60 &&
                unit.moveToSafety(Actions.RUN_ENEMY)
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
            if (unit.moveAwayFrom(center, 6, Actions.RUN_ENEMY, "ScoutSeparateA")) {
                return usedManager(this);
            }
        }

//        if (unit.enemiesThatCanAttackMe(2.4 + unit.woundPercent() / 30.0).empty()) {
        if (unit.moveToMain(Actions.RUN_ENEMY)) return usedManager(this);
//        }

        return null;
    }

    private AUnit nearestEnemy() {
        int killed = ScoutCommander.killedScouts();
        if (unit.hp() >= (killed == 0 ? 34 : (killed == 1 ? 37 : 40))) {
            return enemies.visibleOnMapOrCombatBuilding().havingAtLeastHp(1).nearestTo(unit);
        }

        return enemies.nearestTo(unit);
    }

    /**
     * Hurt uses the same killed-scaled ladder as {@link #nearestEnemy}:
     * with no scouts lost yet, anything below 34 hp (of 40) retreats
     * home instead of microing locally.
     */
    private boolean isHurt() {
        int killed = ScoutCommander.killedScouts();
        return unit.hp() < (killed == 0 ? 34 : (killed == 1 ? 37 : 40));
    }
}
