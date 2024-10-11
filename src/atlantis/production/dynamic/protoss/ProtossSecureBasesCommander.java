package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.reinforce.protoss.IsProtossBaseSecured;
import atlantis.production.dynamic.reinforce.protoss.ProtossReinforceBaseWithCannons;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class ProtossSecureBasesCommander extends Commander {
    private boolean hasMutas = false;

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(65)
            && Have.forge()
            && ((hasMutas = hasMutas()) || Count.basesWithUnfinished() >= 2)
            && (A.supplyUsed() >= 80 || A.minerals() >= 500 || EnemyInfo.hasHiddenUnits() || forgeFE())
            && ConstructionRequests.countNotStartedOfType(Protoss_Photon_Cannon) <= (hasMutas ? 1 : 0);
    }

    private boolean forgeFE() {
        if (!OurStrategy.get().isExpansion()) return false;

        if (Enemy.protoss()) {
            if (EnemyUnits.zealots() >= 3 && Count.cannonsWithUnfinished() < 3) return true;
        }

        if (Enemy.zerg()) {
            if (EnemyUnits.zerglings() >= 12 && Count.cannonsWithUnfinished() < 3) return true;
        }

        return false;
    }

    private boolean hasMutas() {
        return Enemy.zerg() && EnemyUnits.mutas() > 0;
    }

    @Override
    protected void handle() {
        Count.clearCache();

        for (AUnit base : Select.ourBasesWithUnfinished().reverse().list()) {
            if (skipReinforcingMainBase(base)) continue;

            if ((new IsProtossBaseSecured(base)).needsSecuring()) {
                (new ProtossReinforceBaseWithCannons(base)).reinforce();
            }
        }
    }

    private boolean skipReinforcingMainBase(AUnit base) {
        return !hasMutas && base.isMainBase();
    }

//    private void buildForge() {
//        if (Count.inQueueOrUnfinished(Protoss_Forge, 10) > 0) return;
//
////        System.err.println("@@@@@ " + A.now() + " - force Forge");
//        AddToQueue.withTopPriority(Protoss_Forge);
//    }
}
