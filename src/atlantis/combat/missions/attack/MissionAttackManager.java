package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.advance.Advance;
import atlantis.combat.squad.SquadCohesionManager;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
import atlantis.units.AUnit;

public class MissionAttackManager extends Manager {
    public MissionAttackManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            AllowTimeToReposition.class,
            SquadCohesionManager.class,
            Advance.class,
        };
    }
}
