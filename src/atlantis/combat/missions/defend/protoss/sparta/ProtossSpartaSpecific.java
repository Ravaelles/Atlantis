package atlantis.combat.missions.defend.protoss.sparta;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.units.AUnit;

public class ProtossSpartaSpecific extends Manager {
    public ProtossSpartaSpecific(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMissionSparta()
            && EnemyUnitBreachedBase.noone();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            ProtossCohesion.class,
            DragoonSeparateFromZealots.class,
        };
    }

}
