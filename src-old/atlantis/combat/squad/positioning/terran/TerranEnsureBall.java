package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            EnsureBallAsTank::new,
            TooFarFromMedic::new,
//            GoBehindLineOfTanks.class,
//            TooFarFromTank.class,
        };
    }
}
