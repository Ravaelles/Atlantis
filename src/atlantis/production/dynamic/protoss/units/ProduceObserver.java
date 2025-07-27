package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.production.AbstractDynamicUnits.buildToHave;
import static atlantis.units.AUnitType.*;

public class ProduceObserver {

    private static int observers;

    public static boolean needObservers() {
        if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) return true;
        if (detectedBuilding()) return true;
        if (A.supplyUsed() <= 45) return false;

        observers = Count.observers();

        if (earlyGamePressureDontInvest()) return false;

        if (observers >= 2 && Count.reavers() == 0 && Have.roboticsSupportBay() && ProduceReavers.reavers())
            return false;

        if (shouldPrepareForObserver()) return true;
        if (A.supplyUsed() >= 78 && observers == 0) return true;

        return observers < (4 + EnemyUnits.discovered().lurkers().count() >= 2 ? 4 : 0);
    }

    public static boolean earlyGamePressureDontInvest() {
        if (observers >= 1 && !EnemyInfo.goesOrHasHiddenUnits()) return true;

        int cannons = Count.cannons();
        if (cannons <= 0 && Army.strength() >= 140) return false;

        if (A.s <= 60 * 8.5 && Army.strengthWithoutCB() <= 110) return true;

        return A.s <= 60 * (7.5 + 3 * cannons)
            && Count.ourOfTypeUnfinished(Protoss_Reaver) == 0;
    }

    // =========================================================

    private static boolean detectedBuilding() {
        return EnemyUnits.discovered().ofType(
            Protoss_Templar_Archives,
            Zerg_Lurker,
            Zerg_Lurker_Egg
        ).notEmpty();
    }

    private static boolean shouldPrepareForObserver() {
        int minSupply = (Have.cannon() ? 65 : 47)
            + (Count.cannons() >= 2 ? 20 : 0)
            + (Count.ourCombatUnits() <= 7 ? 10 : 0)
            + (Army.strength() <= 160 ? 10 : 0)
            + (Army.strength() <= 150 ? 10 : 0)
            + (Army.strength() <= 130 ? 10 : 0)
            + Math.min(6, (A.resourcesBalance() / 100))
            + (EnemyInfo.noRanged() ? 8 : 0);

        return A.supplyUsed() >= minSupply
            && Army.strength() >= 130;
    }

    public static void observers() {
        if (!needObservers()) return;

        if (produceFirstObserver()) return;

        int limit = observersNeeded();
        if (Count.withPlanned(AUnitType.Protoss_Observer) < limit) {
            buildToHave(AUnitType.Protoss_Observer, limit);
        }

        if (Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility)) {
            AddToQueue.withTopPriority(AUnitType.Protoss_Robotics_Facility);
            if (Have.notEvenPlanned(AUnitType.Protoss_Observatory)) {
                AddToQueue.toHave(AUnitType.Protoss_Observatory, 1, ProductionOrderPriority.HIGH);
            }
            return;
        }

        else if (Have.notEvenPlanned(AUnitType.Protoss_Observatory)) {
            AddToQueue.withTopPriority(AUnitType.Protoss_Observatory);
//                if (Have.notEvenPlanned(AUnitType.Protoss_Observer)) {
//                    AddToQueue.toHave(AUnitType.Protoss_Observer, 1, ProductionOrderPriority.HIGH);
//                }
            return;
        }

        else if (Have.notEvenPlanned(AUnitType.Protoss_Observer)) {
            produceObserver();

//            AddToQueue.toHave(AUnitType.Protoss_Observer, A.supplyUsed() >= 70 ? 2 : 1, ProductionOrderPriority.TOP);

//            AUnit building = Select.ourFree(AUnitType.Protoss_Robotics_Facility).first();
//            if (building != null) {
//                building.pro
//            }
            return;
        }
    }

    private static boolean produceFirstObserver() {
        if (Have.observatory() && Count.observers() == 0) {
            return produceObserver();
        }

        return false;
    }

    private static int observersNeeded() {
        if (Enemy.terran()) return observersNeededVsTerran();

        if (!EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) return A.supplyUsed() <= 100 ? 1 : 2;

        return Math.max(
            1 + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 2 : 0),
            A.supplyTotal() / 40
        );
    }

    private static int observersNeededVsTerran() {
        return 1 + A.supplyUsed() / 40;
    }

    private static boolean produceObserver() {
        AUnit building = Select.ourFree(Protoss_Robotics_Facility).nearestTo(Select.mainOrAnyBuilding());
        if (building == null) return false;

//        System.err.println("YES< zealot");
        return building.train(
            Protoss_Observer, ForcedDirectProductionOrder.create(Protoss_Observer)
        );
    }
}
