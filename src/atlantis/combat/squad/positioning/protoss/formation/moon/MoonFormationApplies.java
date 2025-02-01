package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.positioning.protoss.formation.ProtossShouldCreateFormation;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;

public class MoonFormationApplies {
    //    public static final double DELTA_MARGIN = 0.1;
//    public static final double MAX_PREFERED_DIST = 10.5;
//    public static final double MAX_PREFERED_DIST_WHEN_ONLY_MELEE_ENEMIES = 8;
    public static final double START_BATTLE_DIST_THRESHOLD = 0.8;

    private HasPosition ourCenter;

    public boolean applies(AUnit unit, AUnit leader) {
        if (leader == null) return false;
        if (A.s <= 2) return true;
        Selection enemies = leader.enemiesNear();
        if (enemies.atMost(1)) return false;
        if (leader.isRanged() && enemies.atMost(3) && enemies.onlyMelee()) return false;
        if (leader.friendsNear().combatUnits().empty()) return false;
        if (unit.friendsInRadiusCount(2) < 0) return true;

        if (!A.isUms()) {
            if (unit.isMissionSparta()) return false;
            if (Missions.isGlobalMissionDefend() && unit.distToBase() >= 15) return false;
        }

        if (Enemy.protoss() && enemies.dts().effUndetected().countInRadius(5, unit) > 0) return false;

        if (leader.enemiesNear().combatUnits().countInRadius(leader.isRanged() ? 8.5 : 4, leader) > 0) {
            if (unit.cooldown() > 0) return false;
            if (leader.cooldown() > 0) return false;
            if (leader.lastAttackFrameLessThanAgo(40)) return false;
            if (unit.lastAttackFrameLessThanAgo(40)) return false;
            if (unit.lastUnderAttackLessThanAgo(40)) return false;
        }

        if (squadHasTargetInRange(leader)) return false;
//        if (unit.eval() >= 3.5) return false;
        if (unit.distToMain() <= 7) return false;
//        if (unit.isReaver()) return false;
        if (unit.type().isTransport()) return false;

//        leader = unit.squadLeader();
//        if (leader == null) return false;
        if (leader.isRetreating()) return false;
        if (leader.lastUnderAttackLessThanAgo(50)) return false;

        if (leader.isAction(Actions.MOVE_FORMATION)) return true;

//        if (leader.isRunning()) return false;
//        if (leader.distToBuilding() <= 5) return false;
//        if (muchMoreRangedUnitsThanEnemy(unit, leader)) return false;

//        if (leader.enemiesNearInRadius(OurDragoonRange.range() + 0.5) > 0) return false;
        if (leader.enemiesThatCanAttackMe(START_BATTLE_DIST_THRESHOLD).notEmpty()) return false;
        if (leader.enemiesICanAttack(0).notEmpty()) return false;

        ourCenter = unit.squadCenter();
        if (ourCenter == null) return false;

//        conventionalPoint = ConventionalPoint.get(unit.squad());
//        if (conventionalPoint == null) return false;
//        AAdvancedPainter.paintCircleFilled(conventionalPoint, 9, Color.Orange);

//        preferredDistToConventionalPoint = preferredDistToConventionalPoint();
//        distToConventionalPoint = unit.distTo(conventionalPoint);
//        deltaDist = preferredDistToConventionalPoint - distToConventionalPoint;
//
//        if (Math.abs(deltaDist) < DELTA_MARGIN) return false;

        return true;
//        if (true) return true;
//        return ProtossShouldCreateFormation.check(unit);
    }

    private boolean muchMoreRangedUnitsThanEnemy(AUnit unit, AUnit leader) {
        return leader.ourToEnemyRangedUnitRatio() >= 2
            || (unit.eval() >= 1.8 && ((leader.rangedEnemiesCount(5) + 3) <= leader.friendsNear().ranged().count()));
    }

    private boolean squadHasTargetInRange(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null) return false;

        AUnit target = squad.targeting().lastTargetIfAlive();
        if (target == null) return false;

        return target.enemiesNear().groundUnits().inRadius(OurDragoonRange.range() + 0.3, target).notEmpty();
    }

//    private boolean squadHasTargetInRange(AUnit unit) {
//        Squad squad = unit.squad();
//        if (squad == null) return false;
//
//        AUnit target = squad.targeting().lastTargetIfAlive();
//        if (target == null) return false;
//
//        if (target.enemiesNear().groundUnits().inRadius(OurDragoonRange.range() + 0.3, target).notEmpty()) {
//            return target;
//        }
//
//        return null;
//    }
}
