package atlantis.combat.retreating.protoss.big_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.combat.retreating.protoss.should.ProtossDontRetreat;
import atlantis.units.AUnit;

public class ProtossFullRetreat extends Manager {
    public ProtossFullRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if ((new ProtossDontRetreat(unit)).applies()) return false;

        return ProtossShouldFullRetreat.shouldFullRetreat(unit);
    }

    @Override
    protected Manager handle() {
//        System.err.println("ProtossFullRetreat " + unit + " eval: " + unit.eval());

//        ProtossCohesion tooLonely = new ProtossCohesion(unit);
//        if (tooLonely.applies() && tooLonely.forceHandle() != null) return usedManager(this);

        if ((new ProtossStartRetreat(unit.squadLeaderOrThisUnit())).startRetreatingFrom(enemy())) {
//            unit.paintCircleFilled(14, Color.Red);
            if (unit.isLeader()) RetreatManager.GLOBAL_RETREAT_COUNTER++;
//            System.err.println("            ------");

            unit.addLog("PFull");

            if (unit.isMoving() && unit.distToTargetPosition() >= 2) {
                return usedManager(this);
            }
        }

        return null;
    }

//
//    private double combatBuildingPenalty(AUnit unit) {
//        Selection combatBuildings = EnemyUnits.discovered().buildings().combatBuildingsAnti(unit);
//        if (combatBuildings.empty()) return 0;
//
//        int basePenalty = Alpha.count() <= 25 ? 4 : 0;
//        basePenalty += Alpha.get().leader().lastRetreatedAgo() <= 30 * 15 ? 2 : 0;
//
//        return basePenalty + combatBuildings.inRadius(17, unit).count() / 1.5;
//    }

    private AUnit enemy() {
        return unit.enemiesNear().groundUnits().combatUnits().nearestTo(unit);
    }
}
