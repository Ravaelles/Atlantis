package atlantis.combat.micro.terran;

import atlantis.combat.micro.terran.bunker.LoadIntoBunkers;
import atlantis.combat.micro.terran.bunker.UnloadFromBunkers;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.TechType;


public class TerranInfantry {

    public static boolean update(AUnit unit) {
        if (TerranFirebat.update(unit)) {
            return true;
        }

        if (shouldStimpack(unit)) {
            unit.useTech(stim());
            return true;
        }

        if (UnloadFromBunkers.tryUnloadingFromBunkerIfNeeded(unit)) {
            return true;
        }

        if (LoadIntoBunkers.tryLoadingInfantryIntoBunkerIfNeeded(unit)) {
            return true;
        }

        if (goToNearMedic(unit)) {
            return true;
        }

        return false;
//        return tryLoadingInfantryIntoBunkerIfPossible(unit);
    }

    // =========================================================

    private static boolean goToNearMedic(AUnit unit) {
        if (unit.isHealthy() && unit.distToLeader() <= 6) {
            return false;
        }

        if (unit.cooldownRemaining() <= 3 || unit.hp() >= 26) {
            return false;
        }

//        if (unit.enemiesNear().canAttack(unit, 7).isNotEmpty()) {
//            return false;
//        }

        if (unit.friendsInRadius(10).bunkers().notEmpty() && unit.enemiesNearInRadius(2.7) > 0) {
            return false;
        }

        AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).inRadius(8, unit).havingEnergy(25).nearestTo(unit);
        if (medic != null && medic.distToMoreThan(unit, 2)) {
            return unit.move(medic, Actions.MOVE_SPECIAL, "BeHealed", false);
        }

        return false;
    }

    private static boolean shouldStimpack(AUnit unit) {
        if (!ATech.isResearched(stim()) || !unit.isMarine()) {
            return false;
        }

        if (unit.hp() <= 20 || unit.isStimmed()) {
            return false;
        }

        Selection enemies = unit.enemiesNear().inRadius(9, unit);

        if (
            enemies.atLeast(Enemy.zerg() ? 3 : 2)
        ) {
            if (unit.lastActionMoreThanAgo(5, Actions.USING_TECH)) {
                if (Select.ourOfType(AUnitType.Terran_Medic).inRadius(5, unit).havingEnergy(40).atLeast(2)) {
                    return true;
                }
            }
        }

        if (Enemy.protoss() && unit.hp() >= 40 && unit.id() % 3 == 0 && unit.enemiesNearInRadius(4) >= 2) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static TechType stim() {
        return TechType.Stim_Packs;
    }

}
