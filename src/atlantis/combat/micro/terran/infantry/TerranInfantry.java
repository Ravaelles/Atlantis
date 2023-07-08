package atlantis.combat.micro.terran.infantry;

import atlantis.combat.micro.terran.Stimpack;
import atlantis.combat.micro.terran.bunker.LoadIntoBunkers;
import atlantis.combat.micro.terran.infantry.medic.TerranMedic;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;


public class TerranInfantry extends Manager {

    public TerranInfantry(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            TerranMedic.class,
            TerranFirebat.class,
            Stimpack.class,
            LoadIntoBunkers.class,
            GoTowardsMedic.class,
        };
    }

    @Override
    public boolean applies() {
        return unit.isTerranInfantry();
    }

}
