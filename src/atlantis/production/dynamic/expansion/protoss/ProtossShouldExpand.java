package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class ProtossShouldExpand {
    private static Cache<Boolean> cache = new Cache<>();

    private static int bases;
    private static int basesInProduction;

    public static boolean shouldExpand() {
        if (A.minerals() < 300) return false;

        bases = Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE);
        basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

        // =========================================================

        if (!A.hasMinerals(384) && defendRush()) return no("DefendRush");
        if (tooManyInProgress()) return no("HaveInProgress");

        // === Second base ===========================================

        if (bases <= 1) return forSecondBase();

        // =========================================================

        if (bases >= 7) return no("TooManyBases");

        // =========================================================

        return forThirdAndLaterBases();
    }

    // =========================================================

    private static boolean defendRush() {
        if (defendVsProtossRush()) return true;
        if (defendVsTerranRush()) return true;
        if (defendVsZergRush()) return true;

        return false;
    }

    private static boolean defendVsTerranRush() {
        if (!Enemy.terran()) return false;
        if (A.s >= 9 * 60) return false;
        if (Army.strength() <= 115) return true;

        return false;
    }

    private static boolean defendVsProtossRush() {
        if (!Enemy.protoss()) return false;
        if (A.s >= 9 * 60) return false;
        if (Army.strength() <= 115) return true;

        return false;
    }

    private static boolean defendVsZergRush() {
        if (!Enemy.zerg()) return false;
        if (A.s >= 9 * 60) return false;

        if (Army.strength() <= 115) return true;

        return false;
    }

    private static boolean tooManyInProgress() {
        if (basesInProduction == 0) return false;

        int minerals = A.minerals();

        return (double) (minerals / (400 * basesInProduction)) <= 1.15;
    }

    private static boolean forThirdAndLaterBases() {
        int workers = Count.workers();

        if (forThirdTooFewGateways()) return no("TooFewGateways");

        if (thirdAndLaterVsTerran()) return true;

        if (genericThird()) return yes("Get3rd");
        if (thirdBecauseEnemyHasLotsOfCb()) return yes("Get3rd");

        if (Count.ourCombatUnits() <= 15 && !A.hasMinerals(500)) return no("TooFewUnits");

        if (workers <= 17 * bases || (!A.hasMinerals(550) && workers <= 35)) {
            if (A.minerals() <= 700) return no("TooFewWorkers");
        }

        if (Enemy.protoss() && Have.observatory()) return no("NoObservatory");

        if (genericThirdLotsOfMinerals()) return yes("SuperbMinerals");

//        boolean hasPlentyOfMinerals = AGame.hasMinerals(580);
        int minMinerals = 100 + (We.zerg() ? 268 : 356);

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

    private static boolean thirdAndLaterVsTerran() {
        if (!Enemy.terran()) return false;

        if (AGame.killsLossesResourceBalance() >= 400 && bases <= 2 && basesInProduction == 0 && A.s <= 9 * 60) {
            return yes("GoodVsTerran");
        }

        return false;
    }

    private static boolean forThirdTooFewGateways() {
        return (
            (Count.gatewaysWithUnfinished() * 2 < Count.basesWithUnfinished())
                || (Count.freeGateways() <= 0)
        )
            && Army.strengthWithoutCB() <= 135;
    }

    private static boolean thirdBecauseEnemyHasLotsOfCb() {
        if (EnemyInfo.combatBuildingsAntiLand() >= 2) {
            return Army.strength() >= 95 && Count.basesWithPlanned() <= 2;
        }

        return false;
    }

    private static boolean genericThirdLotsOfMinerals() {
        if (A.minerals() >= 750) {
            if ((double) (A.minerals() / (400 * basesInProduction)) >= 1.2) return true;
        }
        return false;
    }

    private static boolean genericThird() {
        return A.supplyUsed() >= 120
            && Army.strength() >= A.supplyUsed()
            && Count.basesWithPlanned() <= 2;
    }

    private static boolean forSecondBase() {
        int seconds = A.seconds();
        int armyStrength = Army.strength();

        if (seconds >= 10.5 * 60) return yes("GettingLate");
        if (enemyGoesCombatBuildingsEarly()) return yes("EnemyManyEarlyCB");

        if (cautionAgainstZealotRush()) return no("CautionZealots");
        if (cautionAgainstZergArmy()) return no("CautionZerg");

        if (bases <= 1 && basesInProduction <= 0) {
            int minNeeded = Count.ourCombatUnits() <= 9 ? 442 : 376;
            if (A.hasMinerals(minNeeded) && Count.workers() >= 18 && CountInQueue.bases() == 0) {
                Count.clearCache();
                if (Count.inProductionOrInQueue(AtlantisRaceConfig.BASE) == 0) {
                    return yes("ManyMinerals");
                }
            }
        }

        if (notEnoughGateways()) return no("NotEnoughGates");
        if (tooFewArmy()) return no("TooFewUnits");
        if (mainChokeOverwhelmed()) return no("MainChokeOverwhelm");

        if (
            !A.hasMinerals(500) && !Have.existingOrUnfinished(AUnitType.Protoss_Cybernetics_Core)
        ) return no("CoreFirst");

        if (armyStrength <= 90) return no("TooWeak");
        if (seconds <= 500 && OurStrategy.get().isRushOrCheese()) no("RushPlan");
        if (enemyHasNoCombatBuilding()) return no("NoEnemyCB");

//        System.err.println(A.now() + " - armyStrength ok to expand = " + armyStrength);

        if (bases <= 1 && basesInProduction <= 0) {
            if (Count.workers() >= 24) {
                Count.clearCache();
                if (Count.inProductionOrInQueue(AtlantisRaceConfig.BASE) == 0) {
                    return yes("ManyWorkers");
                }
            }
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

    private static boolean cautionAgainstZergArmy() {
        if (!Enemy.zerg()) return false;

        if (A.hasMinerals(500)) return false;

        return Army.strength() <= 140 && Count.ourCombatUnits() <= 13;
    }

    private static boolean notEnoughGateways() {
        int gateways = Count.gateways();

        if (Enemy.protoss()) {
            if (EnemyInfo.combatBuildingsAntiLand() == 0) {
                return gateways <= 6 || Army.strength() >= 140;
            }

            return gateways <= 1;
        }

        if (Enemy.zerg()) {
            return gateways <= 4 && Army.strength() < 170 && !A.hasMinerals(344);
        }

        return gateways <= 1;
    }

    private static boolean enemyGoesCombatBuildingsEarly() {
        int secondLimit = 420;

        if (Enemy.terran()) {
            return A.s <= secondLimit && EnemyInfo.combatBuildingsAntiLand() >= 1;
        }

        else if (Enemy.protoss()) {
            return A.s <= secondLimit
                && EnemyInfo.combatBuildingsAntiLand() >= 2
                && !EnemyInfo.goesOrHasHiddenUnits();
        }

        else if (Enemy.zerg()) {
            return A.s <= secondLimit
                && EnemyInfo.combatBuildingsAntiLand() >= 3
                && !EnemyInfo.goesOrHasHiddenUnits();
        }

        return false;
    }

    private static boolean tooFewArmy() {
        if (A.hasMinerals(550)) return false;

        return Count.zealots() <= 9 && Count.dragoons() <= 6;
    }

    private static boolean cautionAgainstZealotRush() {
        if (!Enemy.protoss()) return false;
        if (EnemyUnits.discovered().dragoons().notEmpty()) return false;
        if (A.hasMinerals(400)) return false;

        return Count.dragoons() <= 5;
    }

    private static boolean mainChokeOverwhelmed() {
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) return false;

        return EnemyUnits.discovered().inRadius(15, mainChoke).atLeast(4);
    }

    private static boolean enemyHasNoCombatBuilding() {
        return !A.hasMinerals(380) && EnemyUnits.discovered().combatBuildingsAntiLand().empty();
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
