package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static atlantis.production.AbstractDynamicUnits.buildToHave;
import static atlantis.units.AUnitType.*;

public class ProduceObserver {
    public static boolean needObservers() {
        if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) return true;
        if (detectedBuilding()) return true;

        if (A.supplyUsed() <= 45) return false;
        if (earlyGamePressureDontInvest()) return false;

        if (Count.observers() >= 2 && Count.reavers() == 0 && Have.roboticsSupportBay() && ProduceReavers.reavers())
            return false;

        if (shouldPrepareForObserver()) return true;
        if (A.supplyUsed() >= 78 && Count.observers() == 0) return true;

        return Count.observers() < (4 + EnemyUnits.discovered().lurkers().count() >= 2 ? 4 : 0);
    }

    public static boolean earlyGamePressureDontInvest() {
        int cannons = Count.cannons();
        if (cannons <= 0 && OurArmy.strength() >= 140) return false;

        if (A.s <= 60 * 8.5 && OurArmy.strengthWithoutCB() <= 110) return true;

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
            + (OurArmy.strength() <= 160 ? 10 : 0)
            + (OurArmy.strength() <= 150 ? 10 : 0)
            + (OurArmy.strength() <= 130 ? 10 : 0)
            + Math.min(6, (A.resourcesBalance() / 100))
            + (EnemyInfo.noRanged() ? 8 : 0);

        return A.supplyUsed() >= minSupply
            && OurArmy.strength() >= 130;
    }

    public static void observers() {
        if (!needObservers()) return;

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

        int limit = maxObservers();

        if (Count.withPlanned(AUnitType.Protoss_Observer) < limit) {
            buildToHave(AUnitType.Protoss_Observer, limit);
        }
    }

    private static int maxObservers() {
        if (!EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) return A.supplyUsed() <= 100 ? 1 : 2;

        return Math.max(
            1 + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 2 : 0),
            A.supplyTotal() / 40
        );
    }

    private static boolean produceObserver() {
        AUnit building = Select.ourFree(Protoss_Robotics_Facility).random();
        if (building == null) return false;

//        System.err.println("YES< zealot");
        return building.train(
            Protoss_Observer, ForcedDirectProductionOrder.create(Protoss_Observer)
        );
    }
}
