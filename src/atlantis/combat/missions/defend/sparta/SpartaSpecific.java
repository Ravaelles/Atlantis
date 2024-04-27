package atlantis.combat.missions.defend.sparta;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooLonely;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooLonelyGetCloser;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;

public class SpartaSpecific extends Manager {
    public SpartaSpecific(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMissionSparta()
            && EnemyWhoBreachedBase.noone();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossTooLonely.class,
            DragoonSeparateFromZealots.class,
        };
    }

}
