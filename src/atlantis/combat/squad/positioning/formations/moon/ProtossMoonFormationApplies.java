package atlantis.combat.squad.positioning.formations.moon;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.positioning.choking.ShouldDoChoking;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class ProtossMoonFormationApplies {
    //    public static final double DELTA_MARGIN = 0.1;
//    public static final double MAX_PREFERED_DIST = 10.5;
//    public static final double MAX_PREFERED_DIST_WHEN_ONLY_MELEE_ENEMIES = 8;
    public static final double START_BATTLE_DIST_THRESHOLD = 0.8;

    private static boolean _lastState = true;

    private HasPosition ourCenter;

    public boolean applies(AUnit unit, AUnit leader) {
        if (leader == null) return false;
//        if (A.now <= 2) return true;
//        if (true) return true;
//        if (unit.isMissionDefendOrSparta()) return false;

        if (leader.friendsNear().combatUnits().empty()) return false;

        Selection enemies = leader.enemiesNear();
//        if (enemies.atLeast(1)) return false;
//        if (unit.squad() != null && unit.squad().lastAttackedLessThanAgo(40)) return f("justAttacked");
        if (unit.squad() != null && unit.squad().lastUnderAttackLessThanAgo(40)) return f("justUAttack");
//        if (unit.eval() >= 3 && Army.strength() >= 140) return f("eval");
        if (evalAndStrengthForbid(unit)) return f("eval(" + A.digit(unit.eval()) + ")");

//        if (unit.enemiesNear().combatUnits().inShootRangeOf(unit).notEmpty()) {
//            return f("unit has range");
//        }

        if (leader.enemiesNear().combatUnits().inShootRangeOf(unit.eval() >= 3 ? 1.8 : 1.1, leader).notEmpty()) {
            return f("leader has range (" + leader.nearestEnemyDist() + ")");
        }

        if (unit.enemiesNear().combatUnits().inShootRangeOf(unit.eval() >= 6 ? 0.15 : 0, unit).notEmpty()) {
            return f("unit has range (" + unit.nearestEnemyDist() + ")");
        }

        if (Enemy.protoss()) {
            if (leader.isRanged() && enemies.atMost(3) && enemies.onlyMelee()) return f("onlyMelee");
        }

        if (Enemy.zerg()) {
            if (Count.ourCombatUnits() <= 7 && unit.eval() >= 1.3) return f("PvZ bravery");
        }

        if (ShouldDoChoking.check(unit)) {
            return true;
        }

        if (!A.isUms()) {
            if (unit.isMissionSparta()) return f("sparta");
            if (Missions.isGlobalMissionDefend() && unit.distToBase() >= 15) return f("defending");
        }

        if (Enemy.protoss() && enemies.dts().effUndetected().countInRadius(5, unit) > 0) return f("DTs");

        if (leader.enemiesNear().combatUnits().countInRadius(leader.isRanged() ? 8.5 : 4, leader) > 0) {
            if (unit.cooldown() > 0) return f("unit cooldown");
            if (leader.cooldown() > 0) return f("leader cooldown");
            if (leader.lastAttackFrameLessThanAgo(40)) return f("leader af");
            if (unit.lastAttackFrameLessThanAgo(40)) return f("unit af");
            if (unit.lastUnderAttackLessThanAgo(40)) return f("under attack");
        }

        if (squadHasTargetInRange(leader)) return f("target in range");
//        if (unit.eval() >= 3.5) return false;
        if (unit.distToMain() <= 7) return f("close to main");
//        if (unit.isReaver()) return false;
        if (unit.type().isTransport()) return f("is transport");

//        leader = unit.squadLeader();
//        if (leader == null) return false;
        if (leader.isRetreating()) return f("leader retreating");
        if (leader.lastUnderAttackLessThanAgo(50)) return f("l under attack");

        if (leader.isAction(Actions.MOVE_FORMATION)) return true;

//        if (leader.isRunning()) return false;
//        if (leader.distToBuilding() <= 5) return false;
//        if (muchMoreRangedUnitsThanEnemy(unit, leader)) return false;

//        if (leader.enemiesNearInRadius(OurDragoonRange.range() + 0.5) > 0) return false;
        if (leader.enemiesThatCanAttackMe(START_BATTLE_DIST_THRESHOLD).notEmpty()) return f("close enemy battle");
        if (leader.enemiesICanAttack(0.1).notEmpty()) return f("leader can attack");

        ourCenter = unit.squadCenter();
        if (ourCenter == null) return f("no our center");

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

    private static boolean evalAndStrengthForbid(AUnit unit) {
        double eval = unit.eval();

        return eval >= 2
            && eval >= Math.max(3, (6.5 - Army.strengthRatio()))
            && Army.strength() >= 150
            && (eval >= 10 || unit.rangedEnemiesCount(8) <= 1);
//            && (Army.strength() >= 150 || unit.rangedEnemiesCount(8) <= 1);
//            && (Army.strength() >= 150 || unit.enemiesThatCanAttackMe(7).atMost(2));
    }

    private boolean f(String failReason) {
//        if (_lastState) System.out.println("MoonDon't: " + failReason);

        return false;
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
