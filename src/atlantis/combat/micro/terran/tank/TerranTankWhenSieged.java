package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranTankWhenSieged extends Manager {
    public TerranTankWhenSieged(AUnit unit) {
        super(unit);
    }

    protected static Class[] managers = {
        TankRunning.class,
        WouldBlockChoke.class,
        DontThinkAboutUnsieging.class,
        SiegeHereDuringMissionDefend.class,
        UnsiegeToReposition.class,
        SiegeHereDuringMissionDefend.class,
    };
}
