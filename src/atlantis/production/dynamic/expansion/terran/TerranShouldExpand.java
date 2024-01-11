package atlantis.production.dynamic.expansion.terran;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.map.base.BaseLocations;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class TerranShouldExpand {
    public static boolean shouldExpand() {
        int bases = Count.bases();
        int basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

        if (basesInProduction > 0) return false;
        if (bases <= 1) return forSecond();

        return forThirdAndLater();

//        if (Count.tanks() <= 0 && !A.hasMinerals(350)) return false;
//        if (A.minerals() <= 1100 && GamePhase.isLateGame()) return false;
//
//        if (Count.inProductionOrInQueue(AUnitType.Terran_Command_Center) > 0) return false;
//
//        // =========================================================
//
//        if (
//            GamePhase.isEarlyGame()
//                && (
//                ArmyStrength.ourArmyRelativeStrength() <= 80
//                    || EnemyUnits.count(AUnitType.Protoss_Zealot) >= 5
//                    || !Have.factory()
//            )
//        ) return false;
//
//        int bases = Count.bases();
//        int basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);
//
//        if (bases >= 5 || basesInProduction >= 1) return false;
//        if (Count.inQueue(AtlantisRaceConfig.BASE) > 0) return false;
//
////        if (ProductionQueue.size() >= 3) {
////            return false;
////        }
//
//        // === First base ===========================================
//
//        if (bases == 0 && basesInProduction == 0) {
//            if (We.terran() && EnemyStrategy.get().isRushOrCheese() && A.seconds() <= 450) return false;
////            return true;
//        }
//
//        if (bases <= 1 && basesInProduction == 0) {
//            boolean secondsAllow =
//                (
//                    (A.seconds() >= 400 && Count.ourCombatUnits() >= 20)
//                        || (A.seconds() >= 520 && Count.ourCombatUnits() >= 8)
//                        || (A.seconds() >= 650)
//                );
////                (
////                    (A.seconds() >= 500 && Count.ourCombatUnits() >= 20)
////                    || (A.seconds() >= 600 && Count.ourCombatUnits() >= 8)
////                    || (A.seconds() >= 700)
////                );
//            if (AGame.canAfford(330, 0) || secondsAllow) {
//                return decisionTrue("Getting late, better expand");
//            }
//        }
//
//        // =========================================================
//
//        if (bases >= 3 && Count.workers() <= 17 * (bases + basesInProduction)) return false;
//
//        boolean hasPlentyOfMinerals = AGame.hasMinerals(580);
//        int minMinerals = 100 + (AGame.isPlayingAsZerg() ? 268 : 356);
//
//        // It makes sense to think about expansion only if we have a lot of minerals.
//        if (!AGame.canAffordWithReserved(minMinerals, 0)) return false;
//
//        // === False again ===========================================
//
//        // If we have plenty of minerals, then every new base is only a hazard
//        if (!AGame.canAffordWithReserved(minMinerals, 1200)) return false;
//
//        int inConstruction = CountInQueue.count(AtlantisRaceConfig.BASE, 8);
//        if (inConstruction >= 1) return false;
//
//        // === Force decent army before 3rd base =========================================
//
//        // Enforce to have a lot of tanks before expansion
//        if (!hasPlentyOfMinerals && AGame.isPlayingAsTerran() && bases >= 2) {
//            if (Select.ourTanks().count() <= 8) return false;
//        }
//
//        // === Check if we have almost as many bases as base locations; if so, exit ======
//
//        if (bases >= BaseLocations.baseLocations().size() - 2) return false;
//
//        int numberOfUnfinishedBases = ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.BASE);
//
//        boolean haveEnoughMinerals = AGame.hasMinerals(minMinerals);
////        boolean haveEnoughBases = bases >= 4 && AGame.isPlayingAsZerg() && Select.ourLarva().count() >= 2;
//        boolean noBaseToConstruct = numberOfUnfinishedBases == 0;
//        boolean allowExtraExpansion = AGame.hasMinerals(minMinerals + 200) && numberOfUnfinishedBases <= 1;
//
//        if (haveEnoughMinerals && (noBaseToConstruct || allowExtraExpansion)) {
//            return decisionTrue("Lots of minerals to expand");
//        }
//
//        return false;
    }

    private static boolean forThirdAndLater() {

    }

    private static boolean forSecond() {
        if (TerranShouldExpandToNatural.shouldExpandToNatural()) return decisionTrue("Expand to natural");

        return false;
    }

    private static boolean decisionTrue(String reason) {
        ErrorLog.printMaxOncePerMinute("Expand: " + reason);
        return true;
    }
}
