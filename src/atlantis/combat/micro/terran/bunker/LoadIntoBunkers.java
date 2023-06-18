package atlantis.combat.micro.terran.bunker;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class LoadIntoBunkers {

    public static boolean tryLoadingInfantryIntoBunkerIfNeeded(AUnit unit) {
        if (unit.lastActionLessThanAgo(10, Actions.LOAD)) {
            unit.addLog("Loading");
            return true;
        }

        // Only Terran infantry get inside
        if (unit.isLoaded() || (!unit.isMarine() && !unit.isGhost())) {
            return false;
        }

        if (UnloadFromBunkers.preventFromActingLikeFrenchOnMaginotLine(unit)) {
            return false;
        }

        // Without enemies around, don't do anything
        Selection enemiesNear = unit.enemiesNear().canAttack(unit, 15);
        if (enemiesNear.excludeMedics().empty()) {
            return false;
        }

        // =========================================================

        AUnit nearestBunker = defineBunkerToLoadTo(unit);
        double maxDistanceToLoad = Missions.isGlobalMissionDefend() ? 5.2 : 8.2;

        if (
            nearestBunker != null
                && nearestBunker.hasFreeSpaceFor(unit)
        ) {
            double bunkerDist = nearestBunker.distTo(unit);
            if (bunkerDist < maxDistanceToLoad
                && (
                nearestBunker.spaceRemaining() >= 1
                    || (
                    enemiesNear.inRadius(1.6, unit).atMost(4)
                        && (!enemiesNear.onlyMelee() || unit.nearestEnemyDist() < 5)
                )
            )) {
//                if (
//                    unit.hp() <= 20 && bunkerDist >= 3 &&
//                    (
//                        bunkerDist > unit.nearestEnemyDist()
//                        || nearestBunker.enemiesNear().inRadius(0.3, nearestBunker).atLeast(5)
//                    )
//                ) {
//                    return false;
//                }

                unit.load(nearestBunker);

                String t = "GetToDaChoppa";
                unit.setTooltipTactical(t);
                unit.addLog(t);
                return true;
            }
        }

        return false;
    }

    // =========================================================

    private static AUnit defineBunkerToLoadTo(AUnit unit) {
        return Select.ourOfType(AUnitType.Terran_Bunker)
            .inRadius(15, unit)
            .havingSpaceFree(unit.spaceRequired())
            .nearestTo(unit);

//        System.out.println("bunker = " + bunker);
//        if (bunker != null) {
//            AUnit mainBase = Select.main();
//
//            // Select the most distance (according to main base) bunker
//            if (Missions.isGlobalMissionDefend() && mainBase != null) {
//                AUnit mostDistantBunker = bunkers
//                        .units()
//                        .sortByGroundDistTo(mainBase.position(), false)
//                        .first();
//                return mostDistantBunker;
//            }
//            else {
//                return bunker;
//            }
//        }

//        return bunker;
    }
}
