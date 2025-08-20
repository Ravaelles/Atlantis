package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.prioritize.PrioritizeGatewaysVsProtoss;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossShouldExpandToThirdAndLaterBase extends ProtossShouldExpand {
    protected static boolean forThirdAndLaterBases() {
        int workers = Count.workers();

        if (Count.workers() <= 41) return no("TooFewWorkers");
        if (forThirdTooFewGateways()) return no("TooFewGateways");
        if (tooSmallArmy()) return no("TooSmallArmy");
        if (delayThirdVsZerg()) return no("Delay3dVsZerg");

        if (thirdAndLaterVsTerran()) return yes("3rd+VsTerran");

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

    private static boolean tooSmallArmy() {
        return A.minerals() <= 500 && Count.ourCombatUnits() <= (15 + Count.basesWithUnfinished() * 5);
    }

    private static boolean delayThirdVsZerg() {
        if (!Enemy.zerg()) return false;

        return Count.ourCombatUnits() <= 22 && !A.hasMinerals(600);
    }

    private static boolean thirdAndLaterVsTerran() {
        if (!Enemy.terran()) return false;

        if (AGame.killsLossesResourceBalance() >= 400 && bases <= 2 && basesInProduction == 0 && A.s <= 9 * 60) {
            return yes("GoodVsTerran");
        }

        return false;
    }

    private static boolean forThirdTooFewGateways() {
        int gateways = Count.gatewaysWithUnfinished();

        if (gateways <= 4) return false;
        if (Count.ourCombatUnits() <= 18 && !A.hasMinerals(500)) return false;

        return (
            (gateways * 2 < Count.basesWithUnfinished())
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
        if (A.minerals() >= 650) {
            return (double) (A.minerals() / (400 * basesInProduction)) >= 1.2;
        }
        return false;
    }

    private static boolean genericThird() {
        return A.supplyUsed() >= 120
            && Army.strength() >= A.supplyUsed()
            && Count.basesWithPlanned() <= 2;
    }
}
