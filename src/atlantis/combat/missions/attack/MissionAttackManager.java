package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.advance.Advance;
import atlantis.combat.advance.special.ImprovePerformanceHavingBugSupply;
import atlantis.combat.squad.TerranSquadCohesionManager;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
import atlantis.units.AUnit;

public class MissionAttackManager extends Manager {
    public MissionAttackManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AllowTimeToReposition.class,
            ImprovePerformanceHavingBugSupply.class,
            TerranSquadCohesionManager.class,
            Advance.class,
        };
    }
}
