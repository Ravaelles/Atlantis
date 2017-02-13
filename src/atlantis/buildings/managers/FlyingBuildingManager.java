package atlantis.buildings.managers;

import atlantis.AtlantisGame;
import atlantis.combat.squad.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class FlyingBuildingManager {

    private static ArrayList<AUnit> flyingBuildings = new ArrayList<>();
    
    // =========================================================
    
    public static void update() {
        if (AtlantisGame.playsAsTerran()) {
            if (shouldLiftABuilding()) {
                liftABuildingAndFlyAmongTheStars();
            }
            
            for (AUnit flyingBuilding : flyingBuildings) {
                updateFlyingBuilding(flyingBuilding);
            }
        }
    }
    
    // =========================================================
    
    private static void updateFlyingBuilding(AUnit flyingBuilding) {
        Missions.getCurrentGlobalMission().getFocusPoint();
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
    
}
