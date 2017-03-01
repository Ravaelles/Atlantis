package atlantis.buildings.managers;

import atlantis.AGame;
import atlantis.combat.squad.missions.Mission;
import atlantis.combat.squad.missions.Missions;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import java.util.ArrayList;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranFlyingBuildingManager {

    private static ArrayList<AUnit> flyingBuildings = new ArrayList<>();
    
    // =========================================================
    
    public static void update() {
        if (AGame.playsAsTerran()) {
            if (shouldLiftABuilding()) {
                liftABuildingAndFlyAmongTheStars();
            }
            
            for (AUnit flyingBuilding : flyingBuildings) {
                updateFlyingBuilding(flyingBuilding);
            }
        }
    }
    
    // =========================================================
    
    private static boolean updateFlyingBuilding(AUnit flyingBuilding) {
        
        // Define focus point for current mission
        Mission currentMission = Missions.getGlobalMission();
        APosition focusPoint = currentMission.getFocusPoint();
        
        // Move towards focus point if needed
        if (focusPoint != null) {
            double distToFocusPoint = focusPoint.distanceTo(flyingBuilding);
            
            if (distToFocusPoint > 2) {
                flyingBuilding.move(focusPoint, UnitActions.MOVE);
            }
            
            if (distToFocusPoint > 5) {
                return true;
            }
        }
        
        // Fly away from nearest tank if it's too close
        AUnit tankTooNear = Select.ourTanks().nearestToOrNull(flyingBuilding, 4.2);
        if (tankTooNear != null) {
            return flyingBuilding.moveAwayFrom(tankTooNear.getPosition(), 0.6);
        }
        
        return false;
    }
    
    // =========================================================

    private static boolean shouldLiftABuilding() {
        if (!flyingBuildings.isEmpty()) {
            return false;
        }
        
        if (Select.ourTanks().count() < 1) {
            return false;
        }
        
        return true;
    }

    private static void liftABuildingAndFlyAmongTheStars() {
        AUnit barracks = Select.ourOfType(AUnitType.Terran_Barracks).idle().first();
        if (barracks != null) {
            barracks.lift();
            flyingBuildings.add(barracks);
        }
        else {
            AUnit engBay = Select.ourOfType(AUnitType.Terran_Engineering_Bay).idle().first();
            engBay.lift();
            flyingBuildings.add(engBay);
        }
    }
    
    // =========================================================

    public static boolean isFlyingBuilding(AUnit unit) {
        return unit.getType().isBuilding() && flyingBuildings.contains(unit);
    }
    
}
