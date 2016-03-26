package atlantis.debug;

import atlantis.util.AtlantisUtilities;
import bwapi.UnitType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisUnitTypesHelper {

    /**
     * Auxiliary function that ensures that damages units inflict are properly calculated.
     */
    public static void displayUnitTypesDamage() {
        HashMap<UnitType, Double> unitsPerGroundDamage = new HashMap<>();
        HashMap<UnitType, Double> unitsPerAirDamage = new HashMap<>();
        HashMap<UnitType, Double> unitsTopRatioAirDamage = new HashMap<>();
        HashMap<UnitType, Double> unitsTopRatioGroundDamage = new HashMap<>();
        
        for (UnitType type : UnitType.getAllUnitTypes()) {
            if (type.getName().startsWith("Hero") || type.getName().startsWith("Special")
                    || type.getName().startsWith("Powerup") || type.getName().startsWith("Critter")) {
                continue;
            }
            
            double dmgGround = type.getGroundWeapon().getDamageNormalized();
            double dmgAir = type.getAirWeapon().getDamageNormalized();
            double unitPrice = type.mineralPrice() + type.gasPrice() * 1.5;
            
            if (dmgGround > 0) {
                unitsPerGroundDamage.put(type, dmgGround);
                unitsTopRatioGroundDamage.put(type, dmgGround / unitPrice);
            }
            if (dmgAir > 0) {
                unitsPerAirDamage.put(type, dmgAir);
                unitsTopRatioAirDamage.put(type, dmgAir / unitPrice);
            }
        }
        
        Map<UnitType, Double> bestGroundDamage = AtlantisUtilities.sortByValue(unitsPerGroundDamage, false);
        Map<UnitType, Double> bestAirDamage = AtlantisUtilities.sortByValue(unitsPerAirDamage, false);
        Map<UnitType, Double> topRatioGroundDamage = AtlantisUtilities.sortByValue(unitsTopRatioAirDamage, false);
        Map<UnitType, Double> topRatioAirDamage = AtlantisUtilities.sortByValue(unitsTopRatioGroundDamage, false);
        
        // =========================================================
        // Display all results
        
        System.out.println("Displaying top damage units and most economical ground and air units "
                + "in terms of offensive power.");
        System.out.println();
        
        System.out.println("===== Best ground damage =====");
        for (UnitType unitType : bestGroundDamage.keySet()) {
            System.out.println(unitType.getShortName() + " damage: " + bestGroundDamage.get(unitType));
        }
        System.out.println();
        
        System.out.println("===== Best air damage =====");
        for (UnitType unitType : bestAirDamage.keySet()) {
            System.out.println(unitType.getShortName() + " damage: " + bestAirDamage.get(unitType));
        }
        System.out.println();
        
        System.out.println("===== Top quality / price ground units =====");
        for (UnitType unitType : bestGroundDamage.keySet()) {
            System.out.println(unitType.getShortName() + " ratio: " + String.format("%.2f", bestGroundDamage.get(unitType)));
        }
        System.out.println();
        
        System.out.println("===== Top quality / price air units =====");
        for (UnitType unitType : bestAirDamage.keySet()) {
            System.out.println(unitType.getShortName() + " ratio: " + String.format("%.2f", bestAirDamage.get(unitType)));
        }
        System.out.println();
    }
    
}
