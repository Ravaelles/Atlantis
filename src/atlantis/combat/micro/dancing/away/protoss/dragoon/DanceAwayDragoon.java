package atlantis.combat.micro.dancing.away.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.combat.micro.dancing.away.DanceAway;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import bwapi.Color;

public class DanceAwayDragoon extends DanceAway {
    public DanceAwayDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        boolean applies = (new DanceAwayDragoonApplies(unit)).applies();

        if (applies && unit.hp() >= 62) {
            Selection enemies = unit.enemiesThatCanAttackMe(1);
            if (enemies.size() == 1) {
                AUnit enemy = enemies.first();
                if (enemy == null || enemy.isDeadMan()) return false;

//                if (!enemy.isMoving() || !unit.isOtherUnitFacingThisUnit(enemy)) {
                if (!unit.isOtherUnitFacingThisUnit(enemy)) {
//                    unit.paintCircleFilled(9, Color.Purple);
//                    enemy.paintCircleFilled(9, Color.Purple);
//                    enemy.paintLine(unit, Color.Purple);
//                    System.out.println("not facing enemy");
                    return false;
                }
            }
        }

        return applies;
    }

    @Override
    public Manager handle() {
        ForceStopDancingDragoon forceStopDancingDragoon = new ForceStopDancingDragoon(unit);
        if (forceStopDancingDragoon.applies()) {
            return usedManager(forceStopDancingDragoon);
        }

        return super.handle();
    }

    @Override
    protected boolean allowedToNotifyNearUnitsToMakeSpace() {
        return unit.shields() <= 40;
    }
}
