package atlantis.combat.squad.positioning.protoss.cohesion;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossCohesion extends Manager {
    public ProtossCohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (A.supplyUsed() >= 190) return false;
        if (A.minerals() >= 1000) return false;
        if (!unit.isCombatUnit()) return false;
        if (!unit.isAlphaSquad()) return false;
        if (unit.isAir()) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.isShuttle()) return false;
        if (Count.ourCombatUnits() >= 20 && Army.strength() >= 500 && unit.eval() >= 3) return false;
//        if (unit.isMissionDefendOrSparta() && Count.ourCombatUnits() >= 10) return false;
        if (unit.unloadedSecondsAgo(10)) return false;
        if (unit.cooldown() <= 15 && unit.meleeEnemiesNearCount(3.7) >= 1) return false;
        if (unit.rangedEnemiesCount(4) >= 1) return false;

        return We.protoss()
            && (A.minerals() <= 1500 && A.supplyUsed() <= 195)
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
            ProtossCombat2Combat.class,
            ProtossTooFarFromLeader.class,
//            ProtossAsLeaderTooFarFromOthers.class,
        };
    }
}
