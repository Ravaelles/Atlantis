package atlantis.debug;

import atlantis.units.AUnitType;
import atlantis.util.AtlantisUtilities;
import atlantis.util.WeaponUtil;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AUnitTypesHelper {

    /**
     * Auxiliary function that ensures that damages units inflict are properly calculated.
     */
    public static void displayUnitTypesDamage() {
        System.out.println("Displaying damages of all units (" + AUnitType.getAllUnitTypes().size() + " units)");
        
        HashMap<AUnitType, Double> unitsPerGroundDamage = new HashMap<>();
        HashMap<AUnitType, Double> unitsPerAirDamage = new HashMap<>();
        HashMap<AUnitType, Double> unitsTopRatioAirDamage = new HashMap<>();
        HashMap<AUnitType, Double> unitsTopRatioGroundDamage = new HashMap<>();
        
        for (AUnitType type : AUnitType.getAllUnitTypes()) {
            if (type.getName().startsWith("Hero") || type.getName().startsWith("Special")
                    || type.getName().startsWith("Powerup") || type.getName().startsWith("Critter")) {
                continue;
            }
            
            double dmgGround = WeaponUtil.getDamageNormalized(type.getGroundWeapon());
            double dmgAir = WeaponUtil.getDamageNormalized(type.getAirWeapon());
            double unitPrice = type.getMineralPrice() + type.getGasPrice() * 1.5;
            
            if (dmgGround > 0) {
                unitsPerGroundDamage.put(type, dmgGround);
                unitsTopRatioGroundDamage.put(type, dmgGround / unitPrice);
            }
            if (dmgAir > 0) {
                unitsPerAirDamage.put(type, dmgAir);
                unitsTopRatioAirDamage.put(type, dmgAir / unitPrice);
            }
        }
        
        Map<AUnitType, Double> bestGroundDamage = AtlantisUtilities.sortByValue(unitsPerGroundDamage, false);
        Map<AUnitType, Double> bestAirDamage = AtlantisUtilities.sortByValue(unitsPerAirDamage, false);
        Map<AUnitType, Double> topRatioGroundDamage = AtlantisUtilities.sortByValue(unitsTopRatioAirDamage, false);
        Map<AUnitType, Double> topRatioAirDamage = AtlantisUtilities.sortByValue(unitsTopRatioGroundDamage, false);
        
        // =========================================================
        // Display all results
        
        System.out.println("Displaying top damage units and most economical ground and air units "
                + "in terms of offensive power.");
        System.out.println();
        
        System.out.println("===== Best ground damage =====");
        for (AUnitType unitType : bestGroundDamage.keySet()) {
            System.out.println(unitType.getShortName() + " damage: " + bestGroundDamage.get(unitType));
        }
        System.out.println();
        
        System.out.println("===== Best air damage =====");
        for (AUnitType unitType : bestAirDamage.keySet()) {
            System.out.println(unitType.getShortName() + " damage: " + bestAirDamage.get(unitType));
        }
        System.out.println();
        
        System.out.println("===== Top quality / price ground units =====");
        for (AUnitType unitType : bestGroundDamage.keySet()) {
            System.out.println(unitType.getShortName() + " ratio: " + String.format("%.2f", bestGroundDamage.get(unitType)));
        }
        System.out.println();
        
        System.out.println("===== Top quality / price air units =====");
        for (AUnitType unitType : bestAirDamage.keySet()) {
            System.out.println(unitType.getShortName() + " ratio: " + String.format("%.2f", bestAirDamage.get(unitType)));
        }
        System.out.println();
    }
    
}
