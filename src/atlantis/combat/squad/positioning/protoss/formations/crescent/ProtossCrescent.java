package atlantis.combat.squad.positioning.protoss.formations.crescent;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.positioning.protoss.formations.ProtossShouldCreateFormation;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import bwapi.Color;

public class ProtossCrescent extends Manager {
    public static final double DELTA_MARGIN = 0.1;
    public static final double MAX_PREFERED_DIST = 10.5;
    public static final double MAX_PREFERED_DIST_WHEN_ONLY_MELEE_ENEMIES = 8;
    public static final double START_BATTLE_DIST_THRESHOLD = 1.0;

    protected static HasPosition ourCenter;
    protected static AUnit conventionalPoint;
    protected static double preferredDistToConventionalPoint;
    protected static double distToConventionalPoint;
    protected static double deltaDist;
    private AUnit leader;

    public ProtossCrescent(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.cooldown() > 0) return false;
        if (unit.lastAttackFrameLessThanAgo(60)) return false;
        if (squadHasTarget()) return false;
        if (unit.eval() >= 3.5) return false;
        if (unit.distToMain() <= 6) return false;
        if (unit.isReaver()) return false;
        if (unit.type().isTransport()) return false;
//        if (unit.enemiesNear().inRadius(7.5, unit).notEmpty()) return false;

        leader = unit.squadLeader();
        if (leader == null) return false;
        if (leader.isRunning()) return false;
        if (leader.distToBuilding() <= 5) return false;
        if (weHaveMoreRangedThatEnemySoNoNeedForCrescent()) return false;

        if (leader.enemiesNearInRadius(OurDragoonRange.range() + 0.2) > 0) return false;
        if (leader.enemiesThatCanAttackMe(START_BATTLE_DIST_THRESHOLD).notEmpty()) return false;
        if (leader.enemiesICanAttack(START_BATTLE_DIST_THRESHOLD).notEmpty()) return false;

        ourCenter = unit.squadCenter();
        if (ourCenter == null) return false;

        conventionalPoint = ConventionalPoint.get(unit.squad());
        if (conventionalPoint == null) return false;
        AAdvancedPainter.paintCircleFilled(conventionalPoint, 9, Color.Orange);

        preferredDistToConventionalPoint = preferredDistToConventionalPoint();
        distToConventionalPoint = unit.distTo(conventionalPoint);
        deltaDist = preferredDistToConventionalPoint - distToConventionalPoint;

        if (Math.abs(deltaDist) < DELTA_MARGIN) return false;

        return ProtossShouldCreateFormation.check(unit);
    }

    private boolean weHaveMoreRangedThatEnemySoNoNeedForCrescent() {
        return leader.ourToEnemyRangedUnitRatio() >= 2
            || ((leader.rangedEnemiesCount(5) + 3) <= leader.friendsNear().ranged().count());
    }

    private boolean squadHasTarget() {
        Squad squad = unit.squad();
        if (squad == null) return false;

        AUnit target = squad.targeting().lastTargetIfAlive();
        if (target == null) return false;

        return target.enemiesNear().groundUnits().inRadius(OurDragoonRange.range() + 0.3, target).notEmpty();
    }

    @Override
    protected Manager handle() {
//        System.err.println(unit + " prefDist = " + A.digit(preferredDistToConventionalPoint));

//        if (isTooClose() && whenTooClose()) return usedManager(this, "CrescentTooClose");

        return handleSubmanagers();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossCrescentTooAhead.class,
//            ProtossCrescentFriendTooFar.class,
            ProtossCrescentTooBehind.class,
            ProtossCrescentHoldGroundYet.class,
        };
    }

//    private boolean isTooClose() {
//        distToConventionalPoint = unit.groundDist(conventionalPoint);
//
//        return distToConventionalPoint < preferredDistToConventionalPoint;
//    }
//
//    private boolean whenTooClose() {
//    }

    private double preferredDistToConventionalPoint() {
        double real = ourCenter.groundDist(conventionalPoint);

        if (real >= MAX_PREFERED_DIST_WHEN_ONLY_MELEE_ENEMIES && unit.enemiesNear().onlyMelee()) {
            double base = Math.max(-6 - unit.eval(), leader.shotSecondsAgo() * -0.1);
//            System.err.println("base = " + base + " / " + leader.shotSecondsAgo());
            return base + MAX_PREFERED_DIST_WHEN_ONLY_MELEE_ENEMIES;
        }

        if (real >= MAX_PREFERED_DIST) {
            double base = Math.max(-unit.eval(), leader.shotSecondsAgo() * -0.03);
            return base + MAX_PREFERED_DIST;
        }

        return real;
    }
}
