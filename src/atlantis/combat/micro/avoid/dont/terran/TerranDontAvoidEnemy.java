package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.util.We;

//public class TerranDontAvoidEnemy extends HasUnit {
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
            DontAvoidEnemyWhenCloseToTank.class,
            ScvDontAvoidEnemy.class,
            WraithDontAvoidEnemy.class,
            TerranMarineDontAvoidEnemy.class,
            TerranWraithDontAvoidEnemy.class,
            TerranGroundDontAvoidEnemy.class,
        };
    }

//    @Override
//    public Manager handle() {
//        return handleSubmanagers();
//    }
}
