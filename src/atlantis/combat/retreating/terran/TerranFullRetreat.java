package atlantis.combat.retreating.terran;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;

public class TerranFullRetreat extends Manager {
    public TerranFullRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if ((new TerranShouldNotRetreat(unit)).shouldNotRetreat()) return false;

        return TerranShouldFullRetreat.shouldFullRetreat(unit);
    }

    @Override
    protected Manager handle() {
//        System.err.println("FullRetreat " + unit + " eval: " + unit.combatEvalRelative());
        if ((new TerranStartRetreat(unit)).startRetreatingFrom(enemy())) {
//            unit.paintCircleFilled(14, Color.Red);
            if (unit.isLeader()) RetreatManager.GLOBAL_RETREAT_COUNTER++;

            unit.addLog("PFull");
            return usedManager(this);
        }

        unit.runningManager().stopRunning();
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
