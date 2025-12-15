package atlantis.combat.squad.positioning.formations.moon;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.positioning.choking.ShouldDoChoking;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ProtossMoonFormationApplies {
    //    public static final double DELTA_MARGIN = 0.1;
//    public static final double MAX_PREFERED_DIST = 10.5;
//    public static final double MAX_PREFERED_DIST_WHEN_ONLY_MELEE_ENEMIES = 8;
    public static final double START_BATTLE_DIST_THRESHOLD = 0.8;

    private static boolean _lastState = true;
    private static String _lastReason = "";

    private HasPosition ourCenter;
    private AUnit unit;

    private boolean dontApply(String failReason) {
//        if (_lastState && !failReason.equals(_lastReason)) {
//            System.err.println("MoonDon't: " + failReason);
//            unit.paintTextCentered(failReason, Color.White, -1);
//            _lastReason = failReason;
//        }

        return false;
    }

    public boolean applies(AUnit unit, AUnit leader) {
        this.unit = unit;

        if (Enemy.terran()) return false;
        if (unit.friendsNear().combatUnits().empty()) return false;
        if (unit.enemiesNear().combatBuildingsAntiLand().notEmpty()) return false;

        if (unit.isRunningOrRetreating()) return dontApply("Running/R");
        if (unit.squadSize() <= 2 && unit.eval() >= 1.2) return dontApply("TooSmallSquad");

        if (A.isUms() && A.now <= 25) return apply("Force at init");
        if (unit.friendsNear().combatUnits().countInRadius(3, unit) == 0) return false;

        if (
            leader.isZealot() && unit.enemiesNear().groundUnits().countInRadius(3, unit) >= 2
        ) return dontApply("Leader cloose enemies");

        if (dontApplyEarlyVsZerg(unit)) return dontApply("DontEarlyVsZerg");

//        if (A.now <= 2) return true;
//        if (true) return true;
//        if (unit.isMissionDefendOrSparta()) return false;

//        if (!unit.isMissionAttack()) return false;
        if (unit.friendsNear().buildings().countInRadius(8, unit) > 0) return dontApply("Our building near");
//        if (leader.enemiesThatCanAttackMe(1.1).notEmpty()) return false;
//        if (leader.friendsNear().combatUnits().empty()) return false;
        if (Army.strength() >= 900 && EnemyUnits.combatUnits() <= 4) return dontApply("HiStrength");

//        if (unit.isLeader()) return false;

        Selection enemies = leader.enemiesNear().combatUnits().nonBuildings();
        Squad squad = unit.squad();
        if (squad != null) {
            if (squad.hasMostlyOffensiveRole()) return dontApply("offensiveSq");

            if (squad.lastUnderAttackLessThanAgo(40)) return dontApply("justUAttack");
            if (squad.lastShotLessThanAgo(40)) return dontApply("justShot");
//            if (
//                unit.isRanged()
//                    && squad.lastAttackedLessThanAgo(5)
//                    && (
//                        unit.enemiesICanAttack(1.1).notEmpty()
//                            || leader.enemiesICanAttack(1.1).notEmpty()
//                )
//            ) return dontApply("justAttacked");
        }
        if (evalAndStrengthForbids(unit)) return dontApply("eval(" + A.digit(unit.eval()) + "/" + Army.strength() + ")");

        if (enemies.inShootRangeOf(0.5, leader).notEmpty()) {
            return dontApply("leader has range (" + leader.nearestEnemyDist() + ")");
        }

        if (
            enemies.inShootRangeOf(0.05, unit).notEmpty()
                && unit.eval() >= 1.5
                && unit.friendsInRadiusCount(1) > 0) {
            return dontApply("unit has range (" + unit.nearestEnemyDist() + ")");
        }

        if (leader.isMelee() && enemies.countInRadius(2, leader) > 0) {
            return dontApply("melee leader close enemy");
        }

        if (Enemy.protoss()) {
            if (enemies.dragoons().notEmpty() && unit.eval() <= 10) return apply("Enemy goons");
            if (leader.isRanged() && enemies.atMost(3) && enemies.onlyMelee()) return dontApply("onlyMelee");
        }

        if (Enemy.zerg()) {
            int ourCombatUnits = Count.ourCombatUnits();

            if (
                ourCombatUnits <= 7 && unit.eval() >= 1.6 && unit.groundDistToMain() <= 50
            ) return dontApply("PvZ bravery");

            if (ourCombatUnits <= 20 && unit.eval() <= 2.5) return apply("PvZ no big army");
        }

        if (ShouldDoChoking.check(unit)) {
            return apply("Choking");
        }

        if (!A.isUms()) {
            if (unit.isMissionSparta()) return dontApply("sparta");
            if (Missions.isGlobalMissionDefend() && unit.distToBase() >= 15) return dontApply("defending");
        }

        if (Enemy.protoss() && enemies.dts().effUndetected().countInRadius(5, unit) > 0) return dontApply("DTs");

        if (enemies.countInRadius(leader.isRanged() ? 8.5 : 4, leader) > 0) {
            if (unit.cooldown() > 0) return dontApply("unit cooldown");
            if (leader.cooldown() > 0) return dontApply("leader cooldown");
            if (leader.lastAttackFrameLessThanAgo(40)) return dontApply("leader af");
            if (unit.lastAttackFrameLessThanAgo(40)) return dontApply("unit af");
            if (unit.lastUnderAttackLessThanAgo(40)) return dontApply("under attack");
        }

        if (squadHasTargetInRange(leader)) return dontApply("target in range");
        if (unit.distToMain() <= 7) return dontApply("close to main");
        if (unit.type().isTransport()) return dontApply("is transport");

        if (leader.isRetreating()) return dontApply("leader retreating");
        if (leader.lastUnderAttackLessThanAgo(50)) return dontApply("l_under_attack");

        if (leader.isAction(Actions.MOVE_FORMATION)) return apply("Making Formation");

//        if (leader.isRunning()) return false;
//        if (leader.distToBuilding() <= 5) return false;
//        if (muchMoreRangedUnitsThanEnemy(unit, leader)) return false;

//        if (leader.enemiesNearInRadius(OurDragoonRange.range() + 0.5) > 0) return false;
        if (leader.enemiesThatCanAttackMe(START_BATTLE_DIST_THRESHOLD).notEmpty()) return dontApply("close enemy battle");
        if (leader.enemiesICanAttack(0.6).notEmpty()) return dontApply("leader can attack");
        if (unit.eval() >= 1.1 && unit.enemiesICanAttack(0).notEmpty()) return dontApply("unit can attack");

        ourCenter = unit.squadCenter();
        if (ourCenter == null) return dontApply("no our center");

//        conventionalPoint = ConventionalPoint.get(unit.squad());
//        if (conventionalPoint == null) return false;
//        AAdvancedPainter.paintCircleFilled(conventionalPoint, 9, Color.Orange);

//        preferredDistToConventionalPoint = preferredDistToConventionalPoint();
//        distToConventionalPoint = unit.distTo(conventionalPoint);
//        deltaDist = preferredDistToConventionalPoint - distToConventionalPoint;
//
//        if (Math.abs(deltaDist) < DELTA_MARGIN) return false;

        return apply("GenericAllow");
//        if (true) return true;
//        return ProtossShouldCreateFormation.check(unit);
    }

    private boolean dontApplyEarlyVsZerg(AUnit unit) {
        if (!Enemy.zerg()) return false;

        if (
            !EnemyInfo.hasRanged()
                && Army.strengthWithoutCB() >= 160
                && unit.groundDistToMain() <= 60
        ) return true;

        return false;
    }

    private static boolean evalAndStrengthForbids(AUnit unit) {
        if (A.s <= 10) return false;

        double eval = unit.eval();
        if (Army.strengthWithoutCB() <= 300) return false;

        Selection enemyCombatUnits = unit.enemiesNear().combatUnits();

        if (enemyCombatUnits.size() >= 7 && Army.strengthWithoutCB() <= 500) return false;

        AUnit nearestEnemy = enemyCombatUnits.nearestTo(unit);

        if (nearestEnemy != null) {
            if (enemyCombatUnits.estimate() > 0.3 * nearestEnemy.enemiesNear().combatUnits().estimate()) {
                return false;
            }
        }

        if (eval <= 3 && enemyCombatUnits.atLeast(8)) return false;

        return (eval >= 2 || Army.strength() >= 190)
            && eval >= Math.max(3, (400 - Army.strength()) / 100.0)
            && (eval >= 10 || unit.rangedEnemiesCount(8) <= 1)
            && A.s >= 3;
    }

    private boolean apply(String yesReason) {
//        if (_lastState && !yesReason.equals(_lastReason)) {
//            System.err.println(A.now + ": Moon APPLY: " + yesReason);
//            unit.paintTextCentered(yesReason, Color.White, -1);
//            _lastReason = yesReason;
//        }

        return true;
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
