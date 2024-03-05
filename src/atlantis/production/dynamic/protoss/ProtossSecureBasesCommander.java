package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.dynamic.reinforce.protoss.ProtossBaseNeedsSecuring;
import atlantis.production.dynamic.reinforce.protoss.ReinforceProtossBaseWithCannons;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Forge;

public class ProtossSecureBasesCommander extends Commander {
    @Override
    public boolean applies() {
        return A.everyNthGameFrame(43)
            && A.supplyUsed() >= 14
            && (EnemyInfo.hasHiddenUnits() || Count.basesWithUnfinished() >= 2);
    }

    @Override
    protected void handle() {
        if (!Have.forge()) {
            buildForge();
            return;
        }

        for (AUnit base : Select.ourBasesWithUnfinished().reverse().list()) {
            if (base.isMainBase()) continue;

            ProtossBaseNeedsSecuring protossBase = new ProtossBaseNeedsSecuring(base);

            if (protossBase.needsSecuring()) {
                reinforceWithCannons(base);
            }
        }
    }

    private void reinforceWithCannons(AUnit base) {
        (new ReinforceProtossBaseWithCannons(base)).reinforce();
    }

    private void buildForge() {
        if (Count.inQueueOrUnfinished(Protoss_Forge, 10) > 0) return;

//        System.err.println("@@@@@ " + A.now() + " - force Forge");
        AddToQueue.withTopPriority(Protoss_Forge);
    }
}
