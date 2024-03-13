package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.race.MyRace;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.base.BaseLocations;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;

public class ProtossShouldExpand {
    private static Cache<Boolean> cache = new Cache<>();

    private static int bases;
    private static int basesInProduction;

    public static boolean shouldExpand() {
        return cache.get(
            "shouldExpand",
            91,
            () -> {
                if (A.minerals() <= 140) return false;

                Count.clearCache();

                bases = Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE);
                basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

                if (tooManyInProgress()) return no("HaveInProgress");
                if (bases >= 7) return no("TooManyBases");

                // === Second base ===========================================

                if (bases <= 1) return forSecondBase();

                // =========================================================

                return forThirdAndLaterBases();
            }
        );
    }

    private static boolean tooManyInProgress() {
        if (basesInProduction == 0) return false;

        int minerals = A.minerals();

        if (minerals <= 700) return true;

        return (minerals / basesInProduction) <= 450;
    }

    private static boolean forThirdAndLaterBases() {
        if (Count.workers() <= 17 * bases || (!A.hasMinerals(550) && Count.workers() <= 35)) return no("TooFewWorkers");
        if (Enemy.protoss() && Have.observatory()) return no("NoObservatory");

//        boolean hasPlentyOfMinerals = AGame.hasMinerals(580);
        int minMinerals = 100 + (MyRace.isPlayingAsZerg() ? 268 : 356);

        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!A.canAffordWithReserved(minMinerals, 0)) return no("CannotAfford");

        // === False again ===========================================

        // If we have plenty of minerals, then every new base is only a hazard
//        if (!AGame.canAffordWithReserved(minMinerals, 1200)) return false;

        int inConstruction = CountInQueue.count(AtlantisRaceConfig.BASE, 8);
        if (inConstruction >= 2) return no("AlreadyConstructing");

        // === Check if we have almost as many bases as base locations; if so, exit ======

        if (bases >= BaseLocations.baseLocations().size() - 2) return no("NotEnoughFree");

        int numberOfUnfinishedBases = ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.BASE);

        boolean haveEnoughMinerals = A.hasMinerals(minMinerals);
//        boolean haveEnoughBases = bases >= 4 && AGame.isPlayingAsZerg() && Select.ourLarva().count() >= 2;
        boolean noBaseToConstruct = numberOfUnfinishedBases == 0;
        boolean allowExtraExpansion = A.hasMinerals(minMinerals + 200) && numberOfUnfinishedBases <= 1;

        boolean result = haveEnoughMinerals && (noBaseToConstruct || allowExtraExpansion);

        if (result) {
            yes("LetsDoIt");
        }
        else {
            no("MaybeNot");
        }

        return result;
    }

    private static boolean forSecondBase() {
        int seconds = A.seconds();
        int armyStrength = OurArmy.strength();

        if (armyStrength <= 90) return no("TooWeak");

//        System.err.println(A.now() + " - armyStrength ok to expand = " + armyStrength);

        if (bases <= 1 && basesInProduction <= 0) {
            if (A.hasMinerals(350) && Count.workers() >= 18 && CountInQueue.bases() == 0) {
                Count.clearCache();
                if (Count.inProductionOrInQueue(AtlantisRaceConfig.BASE) == 0) {
                    return yes("ManyMinerals");
                }
            }
            if (Count.workers() >= 24) {
                Count.clearCache();
                if (Count.inProductionOrInQueue(AtlantisRaceConfig.BASE) == 0) {
                    return yes("ManyWorkers");
                }
            }
            if (seconds >= 750) return yes("GettingLate");
            if (manyGateways()) return yes("ManyGateways");
        }

        boolean secondsAllow = (
            (seconds >= 400 && Count.ourCombatUnits() >= 20)
                || (seconds >= 520 && Count.ourCombatUnits() >= 8)
        );

        if (secondsAllow) return yes("StrongEnough");
        if (basesInProduction == 0 && A.canAfford(360, 0)) return yes("CanAfford");

        if (seconds <= 400 && armyStrength < 100) return no("Weak");

        return no("JustDont");
    }

    private static boolean manyGateways() {
        return A.hasMinerals(230 + (OurStrategy.get().isRushOrCheese() ? 120 : 0))
            && Count.gateways() >= 3;
    }

    private static boolean yes(String reason) {
        ShouldExpand.reason = reason;
        return true;
    }

    private static boolean no(String reason) {
        ShouldExpand.reason = reason;
        return false;
    }

    public static boolean needToSaveMineralsForExpansion() {
//        boolean needToSave = !A.hasMinerals(530) && ReservedResources.minerals() >= 400;
        boolean needToSave = A.minerals() < 100 + ReservedResources.minerals();

//        if (needToSave) System.err.println("@ " + A.now() + " - needToSave (" + A.minerals());

        return needToSave;
    }
}
