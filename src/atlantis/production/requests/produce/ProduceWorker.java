package atlantis.production.requests.produce;

import atlantis.game.AGame;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.combat.micro.terran.lifted.RebaseToNewMineralPatches.isBaseMinedOut;

public class ProduceWorker {
    public static boolean produceWorker() {
        if (We.zerg()) {
            if (AGame.supplyUsed() >= 10 && Count.larvas() <= 1) return false;
        }

        AUnit base = baseToProduceWorker();

        return AutoProduceWorkersCommander.produceWorker(base);
    }

    private static AUnit baseToProduceWorker() {
        // Owner 2026-10-16: the old `.free()` filter excluded every training
        // base, so a busy Nexus never received a train order and the queue
        // path behind it was unreachable too. Prefer a free, empty base for
        // an immediate train; else fall back to a base that can take a
        // queued worker behind the running one (at most one waits).
        AUnit queueable = null;
        for (AUnit base : Select.ourBases().reverse().notLifted().list()) {
            if (!isSafeToProduceWorkerAt(base)) continue;
            if (isBaseMinedOut(base)) continue;
            if (base.isFree() && base.trainingQueue().isEmpty()) return base;
            if (queueable == null && AutoProduceWorkersCommander.canQueueWorkerAt(base)) {
                queueable = base;
            }
        }

        return queueable;
    }

    protected static boolean isSafeToProduceWorkerAt(AUnit base) {
        return base.enemiesNear().groundUnits().canAttack(base, 6).atMost(1);
    }
}
