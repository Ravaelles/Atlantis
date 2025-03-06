package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.formations.ProtossFormation;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossCohesion extends Manager {
    public ProtossCohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isCombatUnit()
            && unit.enemiesNear().combatBuildingsAnti(unit).inRadius(10, unit).empty();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            ProtossFormation.class,

//            AllowTimeToReposition.class,

//            ProtossTooFarFromSquadCenter.class,

//            ProtossMeleeTooFarFromRanged.class,

//            ProtossTooFarFromLeader.class,
        };
    }
}
