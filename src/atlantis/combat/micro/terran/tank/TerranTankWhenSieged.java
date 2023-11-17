package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.sieging.SiegeHereDuringMissionDefend;
import atlantis.combat.micro.terran.tank.sieging.WouldBlockChokeBySieging;
import atlantis.combat.micro.terran.tank.unsieging.DontThinkAboutUnsieging;
import atlantis.combat.micro.terran.tank.unsieging.SiegeTankRun;
import atlantis.combat.micro.terran.tank.unsieging.UnsiegeToReposition;
import atlantis.combat.micro.terran.tank.sieging.kursk.TankVsTank;
import atlantis.units.AUnit;

public class TerranTankWhenSieged extends Manager {
    public TerranTankWhenSieged(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TankVsTank.class,
            DontThinkAboutUnsieging.class,
            SiegeTankRun.class,
            WouldBlockChokeBySieging.class,
            SiegeHereDuringMissionDefend.class,
            UnsiegeToReposition.class,
            UnsiegeCauseLonely.class,
        };
    }
}
