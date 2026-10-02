package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.terran.tank.sieging.SiegeHereDuringMissionDefend;
import atlantis.combat.micro.terran.tank.sieging.WouldBlockChokeBySieging;
import atlantis.combat.micro.terran.tank.unsieging.DontThinkAboutUnsieging;
import atlantis.combat.micro.terran.tank.unsieging.SiegeTankRun;
import atlantis.combat.micro.terran.tank.unsieging.SiegeTankRunCritical;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TankVsTank::new,
            SiegeTankRunCritical::new,
            DontThinkAboutUnsieging::new,
            SiegeTankRun::new,
            WouldBlockChokeBySieging::new,
            SiegeHereDuringMissionDefend::new,
            UnsiegeToReposition::new,
            UnsiegeCauseLonely::new,
        };
    }
}
