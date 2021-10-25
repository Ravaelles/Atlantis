package atlantis.combat.missions;

import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;

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
        APosition enemyBase = AEnemyUnits.getEnemyBase();
        if (enemyBase != null) {
//            System.out.println("1 = " + enemyBase);
            return enemyBase;
        }

        // Try going near any enemy building
        AFoggedUnit enemyBuilding = AEnemyUnits.getNearestEnemyBuilding();
        if (enemyBuilding != null) {
//            System.out.println("2 = " + enemyBuilding);
            return enemyBuilding.getPosition();
        }

        // Try going to any known enemy unit
        AUnit anyEnemyUnit = Select.enemy().first();
        if (anyEnemyUnit != null) {
//            System.out.println("3 = " + anyEnemyUnit);
            return anyEnemyUnit.getPosition();
        }

        if (Select.mainBase() == null) {
            return null;
        }

        // Try to go to some starting location, hoping to find enemy there.
        APosition startLocation = AMap.getNearestUnexploredStartingLocation(Select.mainBase().getPosition());
        return startLocation;
    }

}