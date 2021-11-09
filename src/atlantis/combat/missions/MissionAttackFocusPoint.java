package atlantis.combat.missions;

import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AChoke;
import atlantis.map.BaseLocations;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class MissionAttackFocusPoint extends MissionFocusPoint {

    public APosition focusPoint() {

        // === Handle UMS ==========================================
        
//        if (AGame.isUms()) {
//            AUnit firstUnit = Select.ourRealUnits().first();
//            if (firstUnit != null) {
//                return getUmsFocusPoint(firstUnit.getPosition());
//            }
//            else {
//                return null;
//            }
//        }

        // =========================================================

        // Try going near enemy base
//        Position enemyBase = AtlantisEnemyInformationManager.getEnemyBase();
        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase != null) {
//            System.out.println("1 = " + enemyBase);
            return enemyBase;
        }

        // Try going near any enemy building
        AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
        if (enemyBuilding != null && enemyBuilding.position() != null) {
//            System.out.println("2 = " + enemyBuilding);
            return enemyBuilding.position();
        }

        // Try going to any known enemy unit
        AUnit anyEnemyUnit = Select.enemy().combatUnits().groundUnits().first();
        if (anyEnemyUnit != null) {
//            System.out.println("3 = " + anyEnemyUnit);
            return anyEnemyUnit.position();
        }

        if (Count.ourCombatUnits() <= 40) {
            AChoke mainChoke = Chokes.enemyMainChoke();
            if (mainChoke != null) {
                return mainChoke.position();
            }
        }

        // Try to go to some starting location, hoping to find enemy there.
        if (Select.mainBase() != null) {
            APosition startLocation = BaseLocations.getNearestUnexploredStartingLocation(Select.mainBase().position());
            return startLocation;
        }

        return null;
    }

}