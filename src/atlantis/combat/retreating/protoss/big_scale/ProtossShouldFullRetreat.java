package atlantis.combat.retreating.protoss.big_scale;

import atlantis.combat.micro.avoid.dont.protoss.DontAvoidWhenCannonsNear;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossShouldFullRetreat {
    private static AUnit unit;

    public static boolean shouldFullRetreat(AUnit unit) {
        if (A.isUms() && Count.bases() == 0) return false;

        ProtossShouldFullRetreat.unit = unit;

        if (!unit.isMissionAttack()) return false;
        if (DontAvoidWhenCannonsNear.check(unit)) return false;

//        if (true) return false;

        Selection enemies = unit.enemiesNear().combatUnits().inRadius(Enemy.terran() ? 13 : 9, unit);
        if (enemies.empty()) return false;
        if (enemies.atMost(1)) return false;
        if (OurArmy.strength() >= 600 || A.minerals() >= 2000) return false;

        if (combatEvalIsTooHighToRetreat()) return false;
        if (dontRunNearOurCombatBuildings()) return false;
        if (unit.distToBase() <= 7) return false;
        if (unit.cooldown() <= 4 && unit.combatEvalRelative() >= 0.75 && unit.ourNearestBuildingDist() <= 3)
            return false;

        if (A.s <= 600 && (Enemy.protoss() || Enemy.zerg()) && EnemyUnits.discovered().ranged().empty()) {
            if (enemies.canAttack(unit, 2.8 + unit.woundPercent() / 100.0).empty()) return false;
        }

        if (unit.friendsNear().inRadius(2, unit).atLeast(6)) return false;
        if (unit.friendsNear().inRadius(4, unit).atLeast(10)) return false;

        if (Enemy.zerg() && unit.isMelee() && unit.shields() >= 30 && unit.meleeEnemiesNearCount(1.3) == 1) {
            return false;
        }

//        AChoke naturalChoke = Chokes.natural();
////        APosition naturalBase = DefineNaturalBase.natural();
//        AUnit naturalBase = Bases.natural();
//        if (
//            naturalChoke != null
//                && naturalBase != null
//                && naturalChoke.distTo(unit) >= 2.5
////                && naturalChoke.distTo(unit) <= 8
//                && naturalBase.distTo(unit) <= 8
//        ) return false;

        if (true) return true;

        if (unit.enemiesNear().combatBuildingsAntiLand().empty()) {
            if (unit.combatEvalRelative() >= 2.3) return false;
            if (enemies.atMost(2)) return false;
            if (unit.friendsNear().combatUnits().atLeast(10)) return false;
            if (unit.shieldDamageAtMost(19) && unit.enemiesNear().ranged().empty()) return false;
        }

//        if (
//            enemies.onlyMelee()
//                && unit.combatEvalRelative() >= 0.8
//                && !(new ProtossSmallScaleRetreat(unit).applies())
//        ) {
//            unit.addLog("StillFightSS");
//            return false;
//        }

//        AUnit base = Select.naturalOrMain();
//        if (base == null || (unit.hp() >= 33 && unit.cooldown() <= 5 && base.distTo(unit) <= 8)) return false;

//        if (unit.isMissionSparta()) {
//            AChoke mainChoke = Chokes.mainChoke();
//            if (
//                mainChoke != null
//                    && unit.distTo(mainChoke) >= 2
//                    && unit.distToNearestChokeCenter() <= 5
////                    && base.distTo(unit) <= 25
//            ) return false;
//
//            if (!Enemy.protoss()) {
//                AChoke naturalChoke = Chokes.natural();
//                APosition naturalBase = DefineNaturalBase.natural();
//                if (
//                    naturalChoke != null
//                        && naturalBase != null
//                        && naturalChoke.distTo(unit) >= 2.5
////                        && naturalChoke.distTo(unit) <= 8
//                        && naturalBase.distTo(unit) <= 8
//                ) return false;
//            }
//        }

//        double evalRelative = applyTweaksToCombatEval();
        double evalRelative = unit.combatEvalRelative();

        return evalRelative <= 1.05;
    }

    private static boolean dontRunNearOurCombatBuildings() {
        if (Count.cannons() == 0) return false;

        return unit.friendsNear().cannons().countInRadius(3.2, unit) >= 1;
    }

    private static boolean combatEvalIsTooHighToRetreat() {
        Squad squad = unit.squad();
        if (squad != null && squad.cohesionPercent() <= 60) return false;

        double threshold = unit.isMissionDefendOrSparta() ? 0.95 : 1.12;

        threshold -= unit.squadSize() / 100.0; // 25 units => extra -0.25 allowed

        if (A.supplyUsed() <= 80) threshold += 0.1;

        return unit.combatEvalRelative() >= threshold;
    }

//    private double applyTweaksToCombatEval() {
//        return unit.combatEvalRelative()
//            + (unit.isMissionDefendOrSparta() ? 0 : (unit.distToNearestChokeLessThan(4) ? -0.4 : 0))
//            + (unit.lastRetreatedAgo() <= 30 * 4 ? -0.25 : 0)
////            + (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.1 : 0)
//            + (unit.distToMain() <= 20 ? +0.15 : 0)
//            + (unit.lastUnderAttackLessThanAgo(30 * 4) ? -0.05 : 0)
////            + combatBuildingPenalty(unit)
//            + enemyZerglingBonus(unit);
//    }
}
