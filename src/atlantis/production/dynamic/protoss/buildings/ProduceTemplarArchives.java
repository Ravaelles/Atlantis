package atlantis.production.dynamic.protoss.buildings;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.util.log.ErrorLog;

import static atlantis.units.AUnitType.*;

public class ProduceTemplarArchives {
    public static boolean produce() {
//        if (true) return false; // Always disabled

        if (!Enemy.zerg()) return false;
        if (Have.a(type())) return false;

        if (Enemy.zerg() && A.supplyUsed() < 90 && A.gas() <= 230) return false;
        if (Army.strength() <= 140 && Alpha.get().isMissionAttack()) return false;
//        if (Count.bases() <= 1) return false;
        if ((Count.ourCombatUnits() <= 15 || Army.strength() <= 140) && !A.canAfford(200, 200)) return false;

        // =========================================================

        if (!Strategy.get().canProduceUnit(type())) return false;

        // =========================================================

//        if (A.supplyUsed() < 165) return false;
//        if (A.supplyUsed() >= 175) return requestProduce();
//        if (A.hasGas(330) && Have.a(Protoss_Citadel_of_Adun) && Have.a(Protoss_Observatory)) return requestProduce();
//
//        if (A.supplyUsed() >= 165 && (A.hasGas(180) || A.hasMinerals(400)) && A.now % 41 == 0) return requestProduce();

        return requestProduce();
    }

    private static boolean requestProduce() {
        if (Have.notEvenPlanned(Protoss_Citadel_of_Adun)) {
            return AddToQueue.withHighPriority(Protoss_Citadel_of_Adun) != null
                && A.println("@@@@@@@@@@ Make CITADEL for ARCHIVES at " + A.supplyUsed());
        }
        if (!Have.a(Protoss_Citadel_of_Adun)) return false;

        if (Have.notEvenPlanned(type())) {
            return AddToQueue.withHighPriority(type()) != null
                && A.println("@@@@@@@@@@ Make TEMPLAR ARCHIVES at " + A.supplyUsed());
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Templar_Archives;
    }
}
