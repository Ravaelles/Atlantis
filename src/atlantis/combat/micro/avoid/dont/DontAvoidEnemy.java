package atlantis.combat.micro.avoid.dont;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.terran.TerranDontAvoidEnemy;
import atlantis.units.AUnit;

public class DontAvoidEnemy extends Manager {
    public DontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranDontAvoidEnemy.class,
        };
    }

    @Override
    public Manager handle() {
        return null;
    }
}
