package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.OnWrongSideOfFocusPoint;
import atlantis.combat.squad.squad_scout.SquadScoutProceed;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ChokeBlockerMoveAway extends Manager {
    public static final int MOVE_AWAY_DISTANCE = 4;
    private final APosition chokeBlockPoint;
    private AChoke choke;

    public ChokeBlockerMoveAway(AUnit unit) {
        super(unit);
        this.chokeBlockPoint = unit.specialPosition();
    }

    @Override
    public boolean applies() {
        if (chokeBlockPoint == null) return false;
        if (!NeedChokeBlockers.check()) return false;

        if (shouldRunFromNearEnemy()) return true;

        return unit.enemiesNear().inRadius(7, unit).empty()
            && (choke = ChokeToBlock.get()) != null
            && (needToMoveSpaceForWorkers() || needToMoveForCombatUnits());
    }

    private boolean shouldRunFromNearEnemy() {
        return unit.hp() <= 16
            && (!unit.isZealot() || Count.dragoons() <= 3)
            && unit.enemiesNear().inRadius(2, unit).notEmpty();
    }

    private boolean needToMoveForCombatUnits() {
        return unit.friendsNear()
            .combatUnits()
            .exclude(unit)
            .notSpecialAction()
            .havingActiveManager(OnWrongSideOfFocusPoint.class, SquadScoutProceed.class)
            .notEmpty();
    }

    private boolean needToMoveSpaceForWorkers() {
        return unit.friendsNear()
            .workers()
            .exclude(unit)
            .notRepairing()
            .notProtectors()
            .notSpecialAction()
//            .notConstructing()
//            .notScout()
            .inRadius(7, choke.center())
            .atLeast(1);
    }

    @Override
    public Manager handle() {
        if (unit.distTo(this.chokeBlockPoint) >= MOVE_AWAY_DISTANCE) return null;

        HasPosition goTo = Select.mainOrAnyBuilding();

        if (goTo != null && goTo.distTo(unit) > 0.03) {
            if (A.now() % 5 == 0) {
                unit.move(goTo, Actions.SPECIAL, "ChokeBlock");
            }
        }
        else {
            unit.holdPosition("ChokeBlock");
        }
        unit.setAction(Actions.SPECIAL);

        return usedManager(this);
    }
}
