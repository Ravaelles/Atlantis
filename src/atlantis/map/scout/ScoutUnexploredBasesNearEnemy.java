package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.map.base.define.UnexploredBaseLocationNearEnemy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ScoutUnexploredBasesNearEnemy extends Manager {
    private HasPosition baseLocation;

    public ScoutUnexploredBasesNearEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        baseLocation = UnexploredBaseLocationNearEnemy.get();

        return baseLocation != null;
    }

    @Override
    protected Manager handle() {
        if (unit.move(
            baseLocation, Actions.MOVE_SCOUT, "ScoutEnemyUnexploredBase", true
        )) return usedManager(this);

        return null;
    }
}
