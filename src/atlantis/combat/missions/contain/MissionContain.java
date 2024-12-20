package atlantis.combat.missions.contain;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.attack.MissionAttackVsCombatBuildings;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;

/**
 * Currently not used, needs fixing.
 */
public class MissionContain extends Mission {

    public MissionContain() {
        super("Contain");
        focusPointManager = new MissionContainFocusPoint();
    }

    // =========================================================

    @Override
    protected Manager managerClass(AUnit unit) {
        return new MissionContainManager(unit);
    }


    @Override
    public double optimalDist() {
        return 6;
//        return (new MoveToContainFocusPoint()).optimalDist();
    }

    // =========================================================

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        if (unit.isRanged() && unit.hp() >= 34 && unit.lastUnderAttackMoreThanAgo(30 * 3)) {
            if (
                unit.lastStartedAttackMoreThanAgo(50)
                    && unit.enemiesNear().inRadius(13, unit).onlyMelee()
            ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (
            unit.isTerranInfantry()
                && (new MissionAttackVsCombatBuildings(unit)).forbiddenForTerranInfantry(enemy)
        ) return false;

        if (unit.isStimmed()) return true;

        if (unit.hasWeaponRangeToAttack(enemy, enemy.isMelee() ? 4 : 2.1)) return true;

        AFocusPoint focusPoint = focusPoint();

        if (enemy.hasWeaponRangeToAttack(unit, unit.isMelee() ? 0.3 : 2.1)) return true;

        if (unit.lastUnderAttackMoreThanAgo(30 * 5) && unit.combatEvalRelative() >= 3) return true;

        if (enemy.friendsNear().combatBuildingsAntiLand().inRadius(7.6, unit).atLeast(1)) {
            if (enemy.combatEvalRelative() >= 0.5) return false;
        }

        if (!enemy.distToNearestChokeLessThan(5)) return true;

        if (wouldCrossChokeToAttack(unit, enemy, focusPoint)) return false;

        // Allow to defend bases
//        if (EnemyInfo.isEnemyNearAnyOurBase(enemy)) {
//            return true;
//        }

        // Attack enemies near squad center
        if (
            unit.friendsNear().inRadius(3, unit).atLeast(4)
                || enemy.distTo(unit.squad().center()) <= 12
        ) return true;

        return false;
    }

    // =========================================================

    private boolean wouldCrossChokeToAttack(AUnit unit, AUnit enemy, AFocusPoint focusPoint) {
        if (focusPoint != null && focusPoint.isAroundChoke()) {
            HasPosition squad = unit.squadCenter();
            double squadToEnemy = squad.distTo(enemy);
            double squadToFocus = squad.distTo(focusPoint);

            if (squadToEnemy > 0.8 + squadToFocus) return false;
        }

        return false;
    }

}
