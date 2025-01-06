package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.reinforce.protoss.ShouldSecureProtossBase;
import atlantis.production.dynamic.reinforce.protoss.ProtossSecureBaseWithCannons;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class ProtossSecureBasesCommander extends Commander {
    private static boolean hasMutas = false;

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(45)
            && Have.forge()
            && (hasMutas() || Count.basesWithUnfinished() >= 2)
            && (A.supplyUsed() >= 80 || A.minerals() >= 200 || EnemyInfo.hasHiddenUnits() || forgeFE())
            && (A.minerals() >= 260 || notTooManyStarted());
    }

    private static boolean notTooManyStarted() {
        int maxNotStartedAtOnce = (hasMutas ? 3 : 0)
            + (A.hasMinerals(450) ? 2 : 1)
            + (A.hasMinerals(650) ? 2 : 0);

        return ConstructionRequests.countNotStartedOfType(Protoss_Photon_Cannon) <= maxNotStartedAtOnce;
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

    public static boolean hasMutas() {
        if (hasMutas) return true;

        return hasMutas = (Enemy.zerg() && EnemyUnits.mutas() > 0);
    }

    @Override
    protected void handle() {
        for (AUnit base : Select.ourBasesWithUnfinished().reverse().list()) {
            if ((new ShouldSecureProtossBase(base)).needsSecuring()) {
                (new ProtossSecureBaseWithCannons(base)).reinforce();
            }
        }
    }
}
