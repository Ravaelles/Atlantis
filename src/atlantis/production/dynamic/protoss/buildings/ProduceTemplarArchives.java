package atlantis.production.dynamic.protoss.buildings;

import atlantis.battle.cluster.Alpha;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Citadel_of_Adun;
import static atlantis.units.AUnitType.Protoss_Templar_Archives;

public class ProduceTemplarArchives {
    public static boolean produce() {

        if (!Enemy.zerg()) return false;
        if (Have.a(type())) return false;

        if (Enemy.zerg() && A.supplyUsed() < 90 && A.gas() <= 230) return false;
        if (Army.strength() <= 140 && Alpha.get().isMissionAttack()) return false;
        if ((Count.ourCombatUnits() <= 15 || Army.strength() <= 140) && !A.canAfford(200, 200)) return false;

        // =========================================================

        if (!Strategy.get().canProduceUnit(type())) return false;

        // =========================================================


        return requestProduce();
    }

    private static boolean requestProduce() {
        if (Have.notEvenPlanned(Protoss_Citadel_of_Adun)) {
            // Queue result is the value (explicit return, no print side-channel).

            return AddToQueue.withHighPriority(Protoss_Citadel_of_Adun) != null;
        }
        if (!Have.a(Protoss_Citadel_of_Adun)) return false;

        if (Have.notEvenPlanned(type())) {
            return AddToQueue.withHighPriority(type()) != null;
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Templar_Archives;
    }
}
