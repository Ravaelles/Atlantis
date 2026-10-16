package atlantis.production.dynamic.protoss.buildings;

import atlantis.battle.Alpha;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

import static atlantis.units.AUnitType.*;

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
            // The print used to be the value of the expression (its `true`
            // return stood in for the queue result); keep both explicit.
            if (AddToQueue.withHighPriority(Protoss_Citadel_of_Adun) != null) {
                AConsole.println("@@@@@@@@@@ Make CITADEL for ARCHIVES at " + A.supplyUsed());
                return true;
            }
            return false;
        }
        if (!Have.a(Protoss_Citadel_of_Adun)) return false;

        if (Have.notEvenPlanned(type())) {
            if (AddToQueue.withHighPriority(type()) != null) {
                AConsole.println("@@@@@@@@@@ Make TEMPLAR ARCHIVES at " + A.supplyUsed());
                return true;
            }
            return false;
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Templar_Archives;
    }
}
