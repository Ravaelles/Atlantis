package atlantis.wrappers;

import atlantis.AGame;
import atlantis.production.ProductionOrder;
import bwapi.TechType;
import bwapi.UpgradeType;
import java.util.ArrayList;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ATech {
    
    private static ArrayList<TechType> currentlyResearching = new ArrayList<>();
    private static ArrayList<UpgradeType> currentlyUpgrading = new ArrayList<>();
    
    // =========================================================

    public static boolean isResearched(TechType tech) {
        return isResearchedTech(tech);
    }

    public static boolean isResearched(Object techOrUpgrade, ProductionOrder order) {
        if (techOrUpgrade instanceof TechType) {
            TechType tech = (TechType) techOrUpgrade;
            return isResearchedTech(tech);
        }
        else {
            int level = 1;
            if (order.getModifier() != null) {
                try {
                    level = Integer.parseInt(order.getModifier());
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            UpgradeType upgrade = (UpgradeType) techOrUpgrade;
            return isResearchedUpgrade(upgrade, level);
        }
    }
    
    // =========================================================
    
    /**
     * Returns level of given upgrade. 0 is initially, it can raise up to 3.
     */
    public static int getUpgradeLevel(UpgradeType upgrade) {
        return AGame.getPlayerUs().getUpgradeLevel(upgrade);
    }
    
    // =========================================================

    public static void markAsBeingResearched(TechType tech) {
        currentlyResearching.add(tech);
    }

    public static void markAsBeingUpgraded(UpgradeType upgrade) {
        currentlyUpgrading.add(upgrade);
    }
    
    // =========================================================
    
    private static boolean isResearchedTech(TechType tech) {
        return AGame.getPlayerUs().hasResearched(tech);
    }
    
    private static boolean isResearchedUpgrade(UpgradeType upgrade, int expectedUpgradeLevel) {
        return getUpgradeLevel(upgrade) >= Math.min(expectedUpgradeLevel, 3);
    }

    public static ArrayList<TechType> getCurrentlyResearching() {
        return currentlyResearching;
    }

    public static ArrayList<UpgradeType> getCurrentlyUpgrading() {
        return currentlyUpgrading;
    }

}
