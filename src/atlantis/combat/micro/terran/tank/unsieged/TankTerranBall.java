package atlantis.combat.micro.terran.tank.unsieged;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class TankTerranBall extends Manager {
    public TankTerranBall(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTank() && Count.ourCombatUnits() >= 11 && unit.friendsNear().combatUnits().atMost(7);
    }

    protected Manager handle() {
        HasPosition squadCenter = unit.squadCenter();

        if (squadCenter != null && unit.distTo(squadCenter) >= 5) {
            unit.move(squadCenter, Actions.MOVE_FORMATION, "TankTerranBall");
            return usedManager(this);
        }

        return null;
    }
}
