package atlantis.protoss.dt;

import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AliveEnemies;
import atlantis.units.select.Selection;

public class DarkTemplarTargeting {
    public static final double MAX_DIST_TO_ENEMY = 1.15;

    public static AUnit targetFor(AUnit unit) {
        Selection distantTargets = AliveEnemies.get()
            .groundUnits()
            .nonBuildingsButAllowCombatBuildings()
            .excludeEggsAndLarvae();
        if (distantTargets.empty()) return null;

        Selection closeTargets = distantTargets
            .inRadius(MAX_DIST_TO_ENEMY, unit)
            .effVisible();

        AUnit target = null;

        // === Terran =======================================================================================

        if (Enemy.terran()) {

            // === Workers ===========================================
            if ((target = closeTargets.workers().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Marines ===========================================
            if ((target = closeTargets.marines().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Tanks ===========================================
            if ((target = closeTargets.tanks().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // ######################## Distant

            // === Turrets ===========================================
            if ((target = closeTargets.turrets().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = distantTargets.turrets().nearestTo(unit)) != null) {
                return target;
            }

            // === Turrets ===========================================
            if ((target = closeTargets.ofType(AUnitType.Terran_Comsat_Station).mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = distantTargets.ofType(AUnitType.Terran_Comsat_Station).mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Bunkers ===========================================
            if ((target = closeTargets.bunkers().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = distantTargets.bunkers().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
        }

        // === Protoss =======================================================================================

        else if (Enemy.protoss()) {

            // === Workers ===========================================
            if ((target = closeTargets.workers().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // ######################## Distant

            // === Cannons ===========================================
            if ((target = closeTargets.cannons().notCompleted().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = distantTargets.cannons().notCompleted().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Observatories ===========================================
            if ((target = closeTargets.observatories().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = closeTargets.observatories().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Forge ===========================================
            if ((target = closeTargets.forge().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = closeTargets.forge().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
        }

        // === Zerg =======================================================================================

        else if (Enemy.zerg()) {

            // === Workers ===========================================
            if ((target = closeTargets.workers().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Lings =============================================
            if ((target = closeTargets.zerglings().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Spore ===========================================
            if ((target = closeTargets.sporeColonies().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = closeTargets.sporeColonies().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Sunkens ===========================================
            if ((target = closeTargets.sunkens().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = closeTargets.sunkens().mostWoundedOrNearest(unit)) != null) {
                return target;
            }

            // === Colonies ===========================================
            if ((target = closeTargets.creepColonies().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
            if ((target = closeTargets.creepColonies().mostWoundedOrNearest(unit)) != null) {
                return target;
            }
        }

        // =========================================================

        Selection niceTargets = distantTargets.inRadius(5, unit);
        if (niceTargets.notEmpty()) {
            target = niceTargets.inRadius(3, unit).notMoving().nearestTo(unit);
            if (target != null) return target;

            target = niceTargets.inRadius(5, unit).notShowingBackToUs(unit).nearestTo(unit);
            if (target != null) return target;
        }

        // =========================================================

        return null;
    }

//    private static Object distantTargets(Selection allTargets) {
//        return allTargets.inRadius(3, allTargets.center());
//    }
}
