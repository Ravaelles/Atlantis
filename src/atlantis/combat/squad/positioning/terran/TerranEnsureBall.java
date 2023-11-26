package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.GoBehindLineOfTanks;
import atlantis.combat.squad.positioning.TooFarFromTank;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;

public class TerranEnsureBall extends Manager {
    public TerranEnsureBall(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerranInfantry() && EnemyInfo.weKnowAboutAnyRealUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            EnsureBallAsTank.class,
            TooFarFromMedic.class,
//            GoBehindLineOfTanks.class,
//            TooFarFromTank.class,
        };
    }
}
