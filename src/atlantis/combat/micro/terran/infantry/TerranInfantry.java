package atlantis.combat.micro.terran.infantry;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.infantry.bunker.ConsiderLoadingIntoBunkers;
import atlantis.combat.micro.terran.infantry.bunker.DontGoTooFarFromBunkers;
import atlantis.combat.micro.terran.infantry.bunker.UnloadFromBunkers;
import atlantis.combat.micro.terran.infantry.medic.TerranMedic;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranMedic.class,
            TerranFirebat.class,
            Stimpack.class,
            ConsiderLoadingIntoBunkers.class,
            UnloadFromBunkers.class,
            GoTowardsMedic.class,
            DontGoTooFarFromBunkers.class,
        };
    }
}
