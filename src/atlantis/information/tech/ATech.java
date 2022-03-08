package atlantis.information.tech;

import atlantis.game.AGame;
import atlantis.production.ProductionOrder;
import atlantis.util.cache.Cache;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;


public class ATech {
    
    private static final ArrayList<TechType> currentlyResearching = new ArrayList<>();
    private static final ArrayList<UpgradeType> currentlyUpgrading = new ArrayList<>();
    private static final Cache<Boolean> cache = new Cache<>();

    // =========================================================

    public static boolean isResearched(Object techOrUpgrade) {
        return cache.get(
                "isResearched:" + techOrUpgrade,
                40,
                () -> {
                    if (techOrUpgrade instanceof TechType) {
                        TechType tech = (TechType) techOrUpgrade;
                        return isResearchedTech(tech);
                    } else if (techOrUpgrade instanceof UpgradeType) {
                        return isResearchedUpgrade((UpgradeType) techOrUpgrade, 1);
                    } else {
                        AGame.exit("Neither a tech, nor an upgrade.");
                        return false;
                    }
                }
        );
    }

    public static boolean isResearchedWithOrder(Object techOrUpgrade, ProductionOrder order) {
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

    public static Integer[] costOf(Object techOrUpgrade) {
        if (techOrUpgrade instanceof TechType) {
            return new Integer[] {
                    ((TechType) techOrUpgrade).mineralPrice(), ((TechType) techOrUpgrade).gasPrice()
            };
        } else {
            return new Integer[] {
                    ((UpgradeType) techOrUpgrade).mineralPrice(), ((UpgradeType) techOrUpgrade).gasPrice()
            };
        }
    }

    // =========================================================

    public static void markAsBeingResearched(TechType tech) {
        currentlyResearching.add(tech);
        cache.clear();
    }

    public static void markAsBeingUpgraded(UpgradeType upgrade) {
        currentlyUpgrading.add(upgrade);
        cache.clear();
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

    public static boolean isOffensiveSpell(TechType tech) {
        return !tech.name().contains("Warp_") && !tech.name().contains("Meld_");
    }
}
