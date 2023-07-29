package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
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
            TankRunning.class,
            WouldBlockChokeHere.class,
            DontThinkAboutUnsieging.class,
            SiegeHereDuringMissionDefend.class,
            UnsiegeToReposition.class,
            SiegeHereDuringMissionDefend.class,
        };
    }
}
