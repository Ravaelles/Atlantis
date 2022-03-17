package atlantis.debug;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.util.WeaponUtil;

import java.util.HashMap;
import java.util.Map;


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
            if (type.fullName().startsWith("Hero") || type.fullName().startsWith("Special")
                    || type.fullName().startsWith("Powerup") || type.fullName().startsWith("Critter")) {
                continue;
            }
            
            double dmgGround = WeaponUtil.damageNormalized(type.groundWeapon());
            double dmgAir = WeaponUtil.damageNormalized(type.airWeapon());
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
        
        Map<AUnitType, Double> bestGroundDamage = A.sortByValue(unitsPerGroundDamage, false);
        Map<AUnitType, Double> bestAirDamage = A.sortByValue(unitsPerAirDamage, false);
        Map<AUnitType, Double> topRatioGroundDamage = A.sortByValue(unitsTopRatioAirDamage, false);
        Map<AUnitType, Double> topRatioAirDamage = A.sortByValue(unitsTopRatioGroundDamage, false);
        
        // =========================================================
        // Display all results
        
        System.out.println("Displaying top damage units and most economical ground and air units "
                + "in terms of offensive power.");
        System.out.println();
        
        System.out.println("===== Best ground damage =====");
        for (AUnitType unitType : bestGroundDamage.keySet()) {
            System.out.println(unitType.name() + " (" + unitType.groundWeapon() + ", range "
                    + (unitType.groundWeapon().maxRange() / 32) + "), damage: " + bestGroundDamage.get(unitType));
        }
        System.out.println();
        
        System.out.println("===== Best air damage =====");
        for (AUnitType unitType : bestAirDamage.keySet()) {
            System.out.println(unitType.name() + "(" + unitType.groundWeapon() + ", range "
                    + (unitType.airWeapon().maxRange() / 32) + "), damage: " + bestAirDamage.get(unitType));
        }
        System.out.println();
        
        System.out.println("===== Top quality / price ground units =====");
        for (AUnitType unitType : bestGroundDamage.keySet()) {
            System.out.println(unitType.name() + " ratio: " + String.format("%.2f", bestGroundDamage.get(unitType)));
        }
        System.out.println();
        
        System.out.println("===== Top quality / price air units =====");
        for (AUnitType unitType : bestAirDamage.keySet()) {
            System.out.println(unitType.name() + " ratio: " + String.format("%.2f", bestAirDamage.get(unitType)));
        }
        System.out.println();
    }

    public static void printUnitsAndRequirements() {
        System.out.println("=== All unit types ===");
        for (AUnitType type : AUnitType.getAllUnitTypes()) {
            System.out.println(type.name() + ", required:" + type.whatIsRequired() + ", buildsIt:" + type.whatBuildsIt());
        }
        System.out.println("=== END OF All unit types ===");
    }
}
