package atlantis.combat.squad;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Cache;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.Collections;

public class SquadCenter {

    private Cache<AUnit> cache = new Cache<>();

    private Squad squad;

    // =========================================================

    public SquadCenter(Squad squad) {
        this.squad = squad;
    }

    // =========================================================

    protected boolean isInvalid(AUnit _centerUnit) {
        return _centerUnit == null || _centerUnit.isDead() || (!_centerUnit.isTank() && Count.tanks() >= 2);
    }

    protected AUnit centerUnit() {
        int ttl = 600;
        AUnit centerUnit = cache.get(
            "centerUnit",
            ttl,
            this::defineCenterUnit
        );

        if (centerUnit != null && centerUnit.isAlive() && !centerUnit.isRunning()) {
            return centerUnit;
        }

        centerUnit = this.defineCenterUnit();
        cache.set("centerUnit", ttl, centerUnit);
        return centerUnit;
    }

    protected AUnit defineCenterUnit() {
        ArrayList<Integer> xCoords = new ArrayList<>();
        ArrayList<Integer> yCoords = new ArrayList<>();

        for (AUnit unit : squad.list()) {
            xCoords.add(unit.x());
            yCoords.add(unit.y());
        }

        Collections.sort(xCoords);
        Collections.sort(yCoords);

        APosition median = new APosition(xCoords.get(xCoords.size() / 2), yCoords.get(yCoords.size() / 2));
        AUnit nearestToMedian = Select.ourCombatUnits().nonBuildings().nearestTo(median);

        if (nearestToMedian != null && We.terran()) {
            if (Count.tanks() >= 2) {
                return Select.ourTanks().nearestTo(nearestToMedian);
            }

            if (Count.medics() >= 2) {
                return Select.ourOfType(AUnitType.Terran_Medic).nearestTo(nearestToMedian);
            }
        }

        return nearestToMedian;
    }

}
