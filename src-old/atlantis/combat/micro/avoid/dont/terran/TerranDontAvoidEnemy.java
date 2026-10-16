package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranCheeseDontAvoidEnemy::new,
            DontAvoidEnemyWhenCloseToTank::new,
            ScvDontAvoidEnemy::new,
            WraithDontAvoidEnemy::new,
            TerranMarineDontAvoidEnemy::new,
            TerranWraithDontAvoidEnemy::new,
            TerranGroundDontAvoidEnemy::new,
        };
    }
}
