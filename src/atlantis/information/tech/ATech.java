package atlantis.information.tech;

import atlantis.game.AGame;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;

public class ATech {

    private static final ArrayList<TechType> currentlyResearching = new ArrayList<>();
    private static final ArrayList<UpgradeType> currentlyUpgrading = new ArrayList<>();
    private static final Cache<Boolean> cacheBoolean = new Cache<>();

    /**
     * What our own player has researched and upgraded.
     *
     * <p>In a game the engine knows: {@code AGame.playerUs()}. The stub world has
     * no player with a tech tree, and the harness used to solve that by mocking
     * this whole class statically - which meant every test ran against a facade
     * whose unstubbed methods returned null and 0, including
     * {@code getCurrentlyResearching()} and {@code costOf(...)}. Two questions
     * were enough to answer with a port instead, and the rest of the facade runs
     * for real again ({@code tests.fakes.FakeResearch}).</p>
     */
    public interface Source {
        boolean hasResearched(TechType tech);

        /** 0 initially, up to 3 for most upgrades. */
        int upgradeLevel(UpgradeType upgrade);
    }

    private static Source source = null;

    public static void useSource(Source newSource) {
        source = newSource;
        cacheBoolean.clear();
    }

    public static void useEngine() {
        useSource(null);
    }

    private static Source source() {
        return source != null ? source : EngineSource;
    }

    private static final Source EngineSource = new Source() {
        @Override
        public boolean hasResearched(TechType tech) {
            return AGame.playerUs().hasResearched(tech);
        }

        @Override
        public int upgradeLevel(UpgradeType upgrade) {
            return AGame.playerUs().getUpgradeLevel(upgrade);
        }
    };

    // =========================================================

    public static boolean isResearched(Object techOrUpgrade) {
        return cacheBoolean.get(
            CacheKey.toKey(techOrUpgrade),
            31,
            () -> {
                if (techOrUpgrade instanceof TechType) {
                    TechType tech = (TechType) techOrUpgrade;
                    return isResearchedTech(tech);
                }
                else if (techOrUpgrade instanceof UpgradeType) {
                    UpgradeType upgrade = (UpgradeType) techOrUpgrade;
                    return isResearchedUpgrade(upgrade, upgrade.maxRepeats());
                }
                else {
                    System.out.println("techOrUpgrade = " + techOrUpgrade);
                    throw new RuntimeException("Neither a tech, nor an upgrade.");
//                        ErrorLog.printMaxOncePerMinute("Neither a tech, nor an upgrade: " + techOrUpgrade);
//                        return false;
                }
            }
        );
    }

    public static boolean isNotResearchedOrPlanned(Object techOrUpgrade) {
        return cacheBoolean.get(
            "isNotResearchedOrPlanned:" + techOrUpgrade,
            11,
            () -> {
                if (isResearched(techOrUpgrade)) return false;

                if (techOrUpgrade instanceof TechType) {
                    return Count.inQueueOrUnfinished((TechType) techOrUpgrade, 5) == 0;
                }
                else if (techOrUpgrade instanceof UpgradeType) {
                    return Count.inQueueOrUnfinished((UpgradeType) techOrUpgrade, 5) == 0;
                }
                else {
                    throw new RuntimeException("Neither a tech, nor an upgrade.");
//                        ErrorLog.printMaxOncePerMinute("Neither a tech, nor an upgrade: " + techOrUpgrade);
//                        return false;
                }
            }
        );
    }

    public static boolean isResearchedOrPlanned(Object techOrUpgrade) {
        return !isNotResearchedOrPlanned(techOrUpgrade);
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
        return source().upgradeLevel(upgrade);
    }

    public static Integer[] costOf(Object techOrUpgrade) {
        if (techOrUpgrade instanceof TechType) {
            return new Integer[]{
                ((TechType) techOrUpgrade).mineralPrice(), ((TechType) techOrUpgrade).gasPrice()
            };
        }
        else {
            return new Integer[]{
                ((UpgradeType) techOrUpgrade).mineralPrice(), ((UpgradeType) techOrUpgrade).gasPrice()
            };
        }
    }

    // =========================================================

    public static void markAsBeingResearched(TechType tech) {
        currentlyResearching.add(tech);
        cacheBoolean.clear();
    }

    public static void markAsBeingUpgraded(UpgradeType upgrade) {
        currentlyUpgrading.add(upgrade);
        cacheBoolean.clear();
    }

    // =========================================================

    private static boolean isResearchedTech(TechType tech) {
        return source().hasResearched(tech);
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
