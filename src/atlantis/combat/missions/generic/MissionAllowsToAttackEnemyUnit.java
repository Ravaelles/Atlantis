package atlantis.combat.missions.generic;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public abstract class MissionAllowsToAttackEnemyUnit extends HasUnit {
    protected final Mission mission;
    protected final AFocusPoint focusPoint;

    public MissionAllowsToAttackEnemyUnit(AUnit unit) {
        super(unit);
        this.mission = unit.mission();
        this.focusPoint = mission.focusPoint();
    }

    public abstract boolean allowsToAttackEnemyUnit(AUnit enemy);
}
