package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.race.MyRace;
import atlantis.information.generic.OurArmyStrength;
import atlantis.map.base.BaseLocations;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.expansion.ShouldExpand;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.util.cache.Cache;

public class ProtossShouldExpand {
    private static Cache<Boolean> cache = new Cache<>();

    private static int bases;
    private static int basesInProduction;

    public static boolean shouldExpand() {
        return cache.get(
            "shouldExpand",
            67,
            () -> {
                bases = Count.bases();
                basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

                if (basesInProduction >= 2) return no("InProduction");
                if (bases >= 6) return no("TooManyBases");

                // === Second base ===========================================

                if (bases <= 1) return forSecondBase();

                // =========================================================

                return forThirdAndLaterBases();
            }
        );
    }

    private static boolean forThirdAndLaterBases() {
        if (bases >= 3 && Count.workers() <= 17 * (bases + basesInProduction)) return no("TooFewWorkers");

//        boolean hasPlentyOfMinerals = AGame.hasMinerals(580);
        int minMinerals = 100 + (MyRace.isPlayingAsZerg() ? 268 : 356);

        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!AGame.canAffordWithReserved(minMinerals, 0)) return no("CannotAfford");

        // === False again ===========================================

        // If we have plenty of minerals, then every new base is only a hazard
//        if (!AGame.canAffordWithReserved(minMinerals, 1200)) return false;

        int inConstruction = CountInQueue.count(AtlantisRaceConfig.BASE, 8);
        if (inConstruction >= 2) return no("AlreadyConstructing");

        // === Check if we have almost as many bases as base locations; if so, exit ======

        if (bases >= BaseLocations.baseLocations().size() - 2) return no("NotEnoughFree");

        int numberOfUnfinishedBases = ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.BASE);

        boolean haveEnoughMinerals = AGame.hasMinerals(minMinerals);
//        boolean haveEnoughBases = bases >= 4 && AGame.isPlayingAsZerg() && Select.ourLarva().count() >= 2;
        boolean noBaseToConstruct = numberOfUnfinishedBases == 0;
        boolean allowExtraExpansion = AGame.hasMinerals(minMinerals + 200) && numberOfUnfinishedBases <= 1;

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
        int armyStrength = OurArmyStrength.relative();

        if (armyStrength <= 90) return no("TooWeak");
        if (seconds <= 400 && armyStrength < 110) return no("Weak");

        if (bases <= 1 && basesInProduction <= 0) {
            if (Count.workers() >= 22) return yes("ManyWorkers");
            if (A.hasMinerals(350)) return yes("ManyMinerals");
        }
        if (seconds >= 700 && bases <= 1 && basesInProduction <= 0) return yes("GettingLate");

        boolean secondsAllow = (
            (seconds >= 400 && Count.ourCombatUnits() >= 20)
                || (seconds >= 520 && Count.ourCombatUnits() >= 8)
        );

        if (secondsAllow) return yes("StrongEnough");

        if (AGame.canAfford(360, 0)) return yes("CanAfford");

        return no("JustDont");
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
//        boolean needToSave = !A.hasMinerals(570) && CountInQueue.bases() > 0;
        boolean needToSave = !A.hasMinerals(530) && ReservedResources.minerals() >= 400;

//        if (needToSave) System.err.println("@ " + A.now() + " - needToSave (" + A.minerals());

        return needToSave;
    }
}
