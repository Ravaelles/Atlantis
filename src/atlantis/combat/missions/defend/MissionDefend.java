package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.attack.MissionAttackManager;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;

public class MissionDefend extends Mission {

    protected AUnit unit;
    protected AUnit main;
    protected AFocusPoint focusPoint;
    protected MissionDefendAllowsToAttack allowsToAttack;
    protected double focusPointToBase;
    protected double unitToEnemy;
    protected double unitToFocus;
    protected double unitToBase;
    protected double enemyToBase;
    protected double enemyToFocus;

    public MissionDefend(AUnit unit) {
        super("Defend");
        focusPointManager = new MissionDefendFocusPoint();
        allowsToAttack = new MissionDefendAllowsToAttack(this);
    }

    // =========================================================
    @Override
    protected Manager managerClass(AUnit unit) {
        return new MissionDefendManager(unit);
    }

    @Override
    public double optimalDist() {
        return -1;
    }
}
