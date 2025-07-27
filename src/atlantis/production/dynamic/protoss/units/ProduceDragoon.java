package atlantis.production.dynamic.protoss.units;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.units.select.Select;

import static atlantis.production.AbstractDynamicUnits.trainIfPossible;
import static atlantis.units.AUnitType.*;

public class ProduceDragoon {
    private static int dragoons;
    private static int strength;

    public static boolean dragoon() {
        if (!A.hasGas(50) || !A.hasMinerals(125)) return false;
        if (noProperBuildings()) return false;
        if (waitForDT()) return false;

        dragoons = Count.dragoons();
        strength = Army.strength();

        if (
            (dragoons >= 3 || A.supplyUsed() >= 50)
                && !A.hasGas(200)
                && !singularityChargeResearched()
                && Army.strength() >= 70
        ) {
//            System.out.println(A.supplyUsed() + ": dont produce Dragoon, no Singularity Charge");
            return false;
        }

//        System.out.println("ProduceDragoon: dragoons = " + dragoons + " / res:" + singularityChargeResearched()
//            + " / freeCC:" + Select.ourFree(Protoss_Cybernetics_Core).count());

        if (dragoons <= 6) return produceDragoon();
        if (dragoons <= 9 && A.hasMinerals(228) && A.hasGas(50)) return produceDragoon();

        if (
            dragoons <= 12 && strength <= 105 && Alpha.get().isMissionDefend() && A.hasMinerals(228) && A.hasGas(50)
        ) return produceDragoon();

        if (A.supplyUsed() <= 100 && A.hasMinerals(460) && A.hasGas(250)) return produceDragoon();
        if (dragoons <= 15 && EnemyUnitBreachedBase.notNull() && Army.strength() <= 140) return produceDragoon();

        if (againstEarlyProtossRush()) return produceDragoon();
        if (againstEarlyZergRush()) return produceDragoon();

        if (dragoons <= 6 && A.canAffordWithReserved(type())) return produceDragoon();
        if (dragoons <= 15 && Count.basesWithUnfinished() >= 2) return produceDragoon();

//        if (A.hasGas(50) && A.supplyUsed() <= 38) return produceDragoon();

        if ((!A.hasMinerals(200) || !A.hasGas(100))) return false;
        if (A.supplyUsed() >= 50 && (!A.hasMinerals(210))) return false;

        if (dragoons >= 6) {
            if (!A.hasGas(50) && Decisions.needToProduceZealotsNow()) return false;
            if (!A.hasMineralsAndGas(300, 170) && !A.canAffordWithReserved(125, 50)) return false;
        }

        if ((A.supplyUsed() <= 38 || Count.observers() >= 1)) {
//            trainIfPossible(AUnitType.Protoss_Dragoon, false, 125, 50);
            return produceDragoon();
        }

        return A.hasGas(175) && produceDragoon();
    }

    private static boolean singularityChargeResearched() {
        return ResearchSingularityCharge.isResearched()
            || Select.ourFree(Protoss_Cybernetics_Core).empty();
    }

    private static boolean againstEarlyZergRush() {
        return Enemy.zerg()
            && Army.strength() <= 115
            && A.seconds() <= 6 * 60
            && Count.dragoons() <= 9;
    }

    private static AUnitType type() {
        return Protoss_Dragoon;
    }

    private static boolean waitForDT() {
        return A.seconds() <= 500
            && !A.canAfford(300, 220)
            && CountInQueue.count(Protoss_Dark_Templar, 6) > 0;
    }

    private static boolean againstEarlyProtossRush() {
        return Enemy.protoss()
            && Army.strength() <= 95
            && A.seconds() <= 6 * 60;
    }

    private static boolean noProperBuildings() {
        return Count.freeGateways() == 0 || !Have.cyberneticsCore();
    }

    private static boolean produceDragoon() {
        boolean result = GatewayClosestToEnemy.get().train(
            type(), ForcedDirectProductionOrder.create(type())
        );
//        System.err.println("ProduceDragoon = " + result);
        return result;
    }
}
