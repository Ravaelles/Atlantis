package atlantis.combat.missions.defend.protoss.sparta;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.ProtossCohesion;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;

public class ProtossSpartaSpecific extends Manager {
    public ProtossSpartaSpecific(AUnit unit) {
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
            ProtossCohesion.class,
            DragoonSeparateFromZealots.class,
        };
    }

}
