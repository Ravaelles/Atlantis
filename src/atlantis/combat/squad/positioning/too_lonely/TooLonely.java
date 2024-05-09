package atlantis.combat.squad.positioning.too_lonely;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.terran.TerranTooFarFromLeader;
import atlantis.units.AUnit;

public class TooLonely extends Manager {
    public TooLonely(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossTooLonely.class,
            TerranTooFarFromLeader.class,
        };
    }
}
