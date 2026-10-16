package atlantis.combat.micro.avoid.dont.protoss.dragoon;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import bwapi.Color;

import static atlantis.combat.micro.avoid.dont.protoss.dragoon.DragoonDontAvoid.dontAvoid;
import static atlantis.combat.micro.avoid.dont.protoss.dragoon.DragoonDontAvoid.whenMeleeNear;

public class DragoonDontAvoidVsZerg {
    protected static boolean vsZerg(AUnit unit) {
        if (dontAvoidZerglingsWhenManyDragoonsNear(unit)) return true;
        if (dontAvoidWhenOnlyEnemyZerglingsNearby(unit)) return true;
        if (dontAvoidBigGoonHydraBattle(unit)) return true;

        if (earlyOnDontAvoidLings(unit)) return true;

//        if (unit.isHealthy()) return true;
//        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        if (unit.cooldown() <= 7) {
            int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
            if (meleeEnemiesNearCount > 0) {
                return whenMeleeNear(unit);
            }
        }

        return unit.lastAttackFrameAgo() > 30 * 3
            && dontAvoid(unit, "VsZerg");
    }

    private static boolean earlyOnDontAvoidLings(AUnit unit) {
        if (A.s >= 350) return false;
        if (unit.hp() <= 30) return false;
        if (unit.rangedEnemiesCount(2) == 0) return false;

        return unit.cooldown() == 0;
    }

    private static boolean dontAvoidBigGoonHydraBattle(AUnit unit) {
        if (unit.enemiesNear().ranged().empty()) return false;
        if (unit.meleeEnemiesNearCount(2.5) >= 2 && unit.shields() <= 5) return false;

        int cooldown = unit.cooldown();

//        if (unit.hp() >= 80) return true;
        if (cooldown <= 8) return true;
        if (unit.shieldWound() <= 8) return true;
        if (!unit.shotSecondsAgo(2)) return true;
        if (A.isUms() && cooldown <= 10) return true;
        if (unit.hp() >= 80 && cooldown <= 16) return true;
        if (unit.hp() >= 60 && cooldown <= 10) return true;
//        if (cooldown <= 7 && unit.eval() >= 0.8) return true;
        if (cooldown <= 14 && unit.shields() >= 5) return true;
        if (unit.eval() >= 0.9 && !unit.shotSecondsAgo(4)) return true;

        if (cooldown <= 12 && unit.eval() <= 1.3) {
            if (
                (1 + unit.friendsNear().dragoons().countInRadius(6, unit))
                    >= unit.enemiesNear().hydras().countInRadius(6, unit) * 0.44
            ) return true;
        }

        return unit.eval() >= 0.8 && unit.hp() >= 18 && unit.enemiesNear().hydras().atLeast(4);
    }

    private static boolean dontAvoidWhenOnlyEnemyZerglingsNearby(AUnit unit) {
        if (!Enemy.zerg()) return false;
        if (unit.hp() <= 25) return false;

        Selection enemiesNear = unit.enemiesNear().havingAntiGroundWeapon();

        if (!enemiesNear.onlyMelee()) return false;
        if (!unit.isHealthy() && enemiesNear.inRadius(1.8, unit).atLeast(3)) return false;
        if (unit.enemiesNear().ranged().inRadius(7.8, unit).empty()) return dontAvoid(unit, "OnlyLingsA");

        return unit.shields() >= 5
            && unit.cooldown() <= 8
            && dontAvoid(unit, "OnlyLingsB");
    }

    private static boolean dontAvoidZerglingsWhenManyDragoonsNear(AUnit unit) {
//        if (!unit.isTarget(AUnitType.Zerg_Zergling)) return false;
//        AUnit ling = unit.target();
        if (unit.hp() <= 30) return false;
        if (unit.cooldown() >= 8) return false;

        if (unit.shields() >= 25) {
            Selection enemies = unit.enemiesNear().inRadius(8, unit);
            if (enemies.onlyMelee() && enemies.countInRadius(2, unit) <= 2) {
                return dontAvoid(unit, "GoodGoonLingz");
            }
        }

        AUnit ling = unit.enemiesNear().zerglings().inRadius(OurDragoonRange.range(), unit).nearestTo(unit);
        if (ling == null) return false;

        ling.paintTextCentered("_" + ling.enemiesNear().dragoons().countInRadius(7, ling), Color.Red);

        if (unit.shieldWound() <= 9 && ling.enemiesNear().dragoons().countInRadius(7, ling) >= 2) {
//            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - \"GoonCo-op\"");
            return dontAvoid(unit, "GoonCo-op");
        }

        return false;
    }
}
