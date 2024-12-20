package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class LeaderGoToCurrentFocusChoke extends MissionManager {
    private AChoke focusChoke;

    public LeaderGoToCurrentFocusChoke(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (EnemyInfo.combatBuildingsAntiLand() == 0) return false;
        if (OurArmy.strength() >= 300) return false;
        if (EnemyUnits.combatUnits() <= 1) return false;

        focusChoke = CurrentFocusChoke.get();

        return focusChoke != null;
    }

    @Override
    protected Manager handle() {
        if (goTowards()) return usedManager(this);
        else goAway();

        return null;
    }

    private void goAway() {
        unit.moveAwayFrom(focusChoke, 0.15, Actions.MOVE_FORMATION, "LeaderAway");
    }

    private boolean goTowards() {
        if (focusChoke.distTo(unit) > preferredDistToChoke()) {
            return unit.move(focusChoke, Actions.MOVE_FORMATION, "LeaderToCFC");
        }

        return false;
    }

    private static int preferredDistToChoke() {
        return 10;
    }
}
