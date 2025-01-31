package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.positioning.protoss.formation.ProtossShouldCreateFormation;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;

public class MoonFormationApplies {
    //    public static final double DELTA_MARGIN = 0.1;
//    public static final double MAX_PREFERED_DIST = 10.5;
//    public static final double MAX_PREFERED_DIST_WHEN_ONLY_MELEE_ENEMIES = 8;
    public static final double START_BATTLE_DIST_THRESHOLD = 1.0;

    private HasPosition ourCenter;

    public boolean applies(AUnit unit, AUnit leader) {
        if (leader == null) return false;
        if (unit.cooldown() > 0) return false;
        if (unit.lastAttackFrameLessThanAgo(60)) return false;
        if (squadHasTarget(leader)) return false;
        if (unit.eval() >= 3.5) return false;
        if (unit.distToMain() <= 7) return false;
        if (unit.isReaver()) return false;
        if (unit.type().isTransport()) return false;
//        if (unit.enemiesNear().inRadius(7.5, unit).notEmpty()) return false;

        leader = unit.squadLeader();
        if (leader == null) return false;
        if (leader.isRunning()) return false;
        if (leader.distToBuilding() <= 5) return false;
        if (muchMoreRangedUnitsThanEnemy(unit, leader)) return false;

        if (leader.enemiesNearInRadius(OurDragoonRange.range() + 0.2) > 0) return false;
        if (leader.enemiesThatCanAttackMe(START_BATTLE_DIST_THRESHOLD).notEmpty()) return false;
        if (leader.enemiesICanAttack(START_BATTLE_DIST_THRESHOLD).notEmpty()) return false;

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

        return ProtossShouldCreateFormation.check(unit);
    }

    private boolean muchMoreRangedUnitsThanEnemy(AUnit unit, AUnit leader) {
        return leader.ourToEnemyRangedUnitRatio() >= 2
            || (unit.eval() >= 1.8 && ((leader.rangedEnemiesCount(5) + 3) <= leader.friendsNear().ranged().count()));
    }

    private boolean squadHasTarget(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null) return false;

        AUnit target = squad.targeting().lastTargetIfAlive();
        if (target == null) return false;

        return target.enemiesNear().groundUnits().inRadius(OurDragoonRange.range() + 0.3, target).notEmpty();
    }
}
