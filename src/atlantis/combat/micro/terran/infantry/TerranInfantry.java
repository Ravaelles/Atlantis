package atlantis.combat.micro.terran.infantry;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.terran.infantry.bunker.ConsiderLoadingIntoBunkers;
import atlantis.combat.micro.terran.infantry.bunker.DontGoTooFarFromBunkers;
import atlantis.combat.micro.terran.infantry.bunker.UnloadFromBunkers;
import atlantis.combat.micro.terran.infantry.medic.TerranMedic;
import atlantis.combat.micro.terran.infantry.special.SpreadWhenHighTemplarsNear;
import atlantis.units.AUnit;


public class TerranInfantry extends Manager {
    public TerranInfantry(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerranInfantry();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            SpreadWhenHighTemplarsNear::new,
            TerranMedic::new,
            TerranFirebat::new,
            Stimpack::new,
            ConsiderLoadingIntoBunkers::new,
            UnloadFromBunkers::new,
            GoTowardsMedic::new,
            DontGoTooFarFromBunkers::new,
        };
    }
}
