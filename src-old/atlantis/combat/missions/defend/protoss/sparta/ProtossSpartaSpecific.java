package atlantis.combat.missions.defend.protoss.sparta;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.units.AUnit;
import atlantis.util.We;

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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
//            ProtossCohesion.class,
            DragoonSeparateFromZealots::new,
        };
    }

}
