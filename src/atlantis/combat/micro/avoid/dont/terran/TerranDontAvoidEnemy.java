package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranDontAvoidEnemy extends Manager {
    public TerranDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ScvDontAvoidEnemy.class,
            WraithDontAvoidEnemy.class,
            TerranRangedDontAvoidEnemy.class,
        };
    }

    @Override
    public Manager handle() {
        return null;
    }
}
