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
            boolean queued = AddToQueue.withHighPriority(Protoss_Citadel_of_Adun) != null;
            if (queued) {
                twinArchivesBoundCitadel();
            }

            return queued;
        }
        if (!Have.a(Protoss_Citadel_of_Adun)) return false;

        if (Have.notEvenPlanned(type())) {
            return AddToQueue.withHighPriority(type()) != null;
        }

        return false;
    }

    /**
     * Names the Archives exception when the templar line orders its own
     * Citadel below the standalone floor (owner 2026-11-09): the V2 mint
     * door twins the same row at warp time (CITADEL_PREREQ), this names
     * the legacy decision that queued it.
     */
    private static void twinArchivesBoundCitadel() {
        try {
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.CITADEL_PREREQ)
                .param("type", Protoss_Citadel_of_Adun.name())
                .param("supplyUsed", atlantis.game.A.supplyUsed())
                .param("via", "templar-archives-queue")
                .log(atlantis.game.A.now(), atlantis.game.A.minSec());
        } catch (Exception | Error ignored) {
        }
    }

    private static AUnitType type() {
        return Protoss_Templar_Archives;
    }
}
