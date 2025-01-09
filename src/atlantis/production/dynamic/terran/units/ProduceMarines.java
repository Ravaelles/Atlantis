package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.information.decisions.terran.ShouldMakeTerranBio;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.Army;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.Strategy;
import atlantis.map.choke.Chokes;
import atlantis.production.dynamic.terran.TerranDynamicInfantry;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.Terran_Barracks;
import static atlantis.units.AUnitType.Terran_Marine;

public class ProduceMarines {
    //    private static final boolean DEBUG = true;
    private static final boolean DEBUG = false;
    private static int marines;

    public static boolean marines() {
        if (!A.hasMinerals(50)) return false;

        int freeBarracks = Count.freeBarracks();
        if (freeBarracks <= 0) return false;

        if (Strategy.get().isRushOrCheese() || Strategy.get().goingBio()) {
            if (A.s <= 60 * 6 && A.hasMinerals(50)) return forceProduceMarine();
            if (A.hasMinerals(250)) return forceProduceMarine();
        }

        if (MechInsteadOfInfantry.check()) return false;

        if (A.hasMinerals(100) && EnemyUnitBreachedBase.get() != null) return forceProduceMarine();

        marines = Count.marines();

        if (marines <= 3 && A.hasMinerals(100)) return forceProduceMarine();
        if (saveForFactoryWhenQuiteStrong()) return false;

        if (marines >= 5 && !A.hasMinerals(100)) return false;
        if (!A.canAffordWithReserved(55, 0)) return false;

        if (marines <= 20 && A.hasMinerals(150)) return forceProduceMarine();
        if (marines <= 1 && A.hasMinerals(150)) return forceProduceMarine();

        if (earlyGameAndWeAreWeak()) return forceProduceMarine();

//        if (marines == 0 && A.hasMinerals(100)) {
//            return AddToQueue.maxAtATime(Terran_Marine, 1) != null;
//        }

        if (Enemy.terran() && (marines >= 4 && !A.hasMinerals(700 + 100 * marines))) return false;
        if (inRelationToTanks(marines)) return false;

        if (ShouldProduceInfantry.shouldProduceInfantry(marines)) {
            return produceMarine();
        }

        if (!A.supplyUsed(160) && A.hasMinerals(800)) {
            return produceMarine();
        }

        if (marines <= 10 && A.hasMinerals(600)) {
            return produceMarine();
        }

        if (marines >= 8 && A.supplyUsed(170) && !A.hasMinerals(800)) return false;

        if (Enemy.zerg() && A.seconds() >= 300 && marines <= 4) {
            return produceMarine();
        }

        if (!A.hasMinerals(200) && marines >= 4 && !A.canAffordWithReserved(50, 0)) return false;

        if (!TerranDecisions.shouldMakeTerranBio()) {
            if (TerranDynamicInfantry.DEBUG)
                System.out.println("Marines - Dont - shouldNOTMakeTerranBio - " + ShouldMakeTerranBio.reason);
            return false;
        }

        if (TerranDynamicInfantry.needToSaveForFactory()) {
            if (TerranDynamicInfantry.DEBUG) System.out.println("Marines - Dont - saveForFactory");
            return false;
        }

        if (A.supplyUsed() >= 32 && !A.canAffordWithReserved(80, 0)) {
            if (TerranDynamicInfantry.DEBUG) System.out.println("Marines - Dont - cantNOTAffordWithReserved");
            return false;
        }

        Selection barracks = Select.ourOfType(AUnitType.Terran_Barracks).free();
        if (barracks.isNotEmpty() && A.canAffordWithReserved(100 + 50 * barracks.count(), 0)) {
            return produceMarine();
        }

        return trainMarinesForBunkersIfNeeded();
    }

    private static boolean saveForFactoryWhenQuiteStrong() {
        return Army.strength() >= 110
            && !Have.factoryWithUnfinished()
            && Have.bunker()
            && A.canAffordWithReserved(50);
    }

    private static boolean forceProduceMarine() {
        return Select.ourFree(Terran_Barracks).nearestTo(Chokes.naturalOrAnyBuilding()).train(
            Terran_Marine, ForcedDirectProductionOrder.create(Terran_Marine)
        );
    }

    private static boolean earlyGameAndWeAreWeak() {
        return GamePhase.isEarlyGame()
            && (
            (marines < 8 || ArmyStrength.weAreMuchWeaker() || EnemyStrategy.isUnknownOrRush())
                && marines < 10
                && A.hasMinerals(100)
        ) || A.hasMinerals(450);
    }

    private static boolean produceMarine() {
        if (Select.ourFree(Terran_Barracks).empty()) return false;

        return forceProduceMarine();

//        ProductionOrder result = AddToQueue.maxAtATime(Terran_Marine, 3, ProductionOrderPriority.TOP);
//
//        if (DEBUG) ConsoleLog.message(
//            "Produce marine (Marines: " + Count.existingOrInProductionOrInQueue(Terran_Marine) + "," +
//                "minerals: " + A.minerals() + ", reserved: " + A.reservedMinerals() + ")"
//        );
//
//        return result != null;
    }

    private static boolean inRelationToTanks(int marines) {
        int tanks = Count.tanks();

        if (tanks <= 2) {
            if (A.canAfford(450 + marines * 15, 0)) return produceMarine();
        }
        if (tanks >= 5) {
            if (A.canAfford(450 + marines * 15, 0)) return produceMarine();
        }
        if (tanks <= 2 && marines >= 10 && A.hasMinerals(650)) {
            if (Enemy.zerg() && Count.bunkers() >= 1) return produceMarine();
        }

        return false;
    }

    protected static boolean trainMarinesForBunkersIfNeeded() {
        int bunkers = Select.countOurOfTypeWithUnfinished(AUnitType.Terran_Bunker);
        if (bunkers > 0) {
            int marines = Select.countOurOfType(Terran_Marine);
            int shouldHaveMarines = optimalNumberOfMarines(bunkers);

            // Force at least one marine per bunker
            if (marines < shouldHaveMarines) {
                for (int i = 0; i < shouldHaveMarines - marines; i++) {
                    AUnit idleBarrack = Select.ourOneNotTrainingUnits(AUnitType.Terran_Barracks);
                    if (idleBarrack != null) {
//                        return AbstractDynamicUnits.addToQueue(AUnitType.Terran_Marine);
//                        return AddToQueue.maxAtATime(Terran_Marine, 4) != null;
                        return forceProduceMarine();
                    }
                    else {
                        break;
                    }
                }
            }
        }
        return false;
    }

    public static int optimalNumberOfMarines(int bunkers) {
        if (bunkers <= 0) {
            return 0;
        }
        else if (bunkers <= 1) {
            return 1;
        }
        else {
            return 4 * bunkers;
        }
    }
}
