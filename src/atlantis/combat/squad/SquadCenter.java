package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;

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

    protected boolean isInvalid(AUnit _leader) {
        return _leader == null || _leader.isDead() || (!_leader.isTank() && Count.tanks() >= 2);
    }

    protected AUnit leader() {
        int ttl = 93;
        AUnit leader = cache.get(
            "leader",
            ttl,
            this::defineLeader
        );

//        if (leader != null && leader.isAlive() && !leader.isRunning()) {
        if (leader != null && leader.isAlive()) {
            return leader;
        }

        leader = this.defineLeader();
        cache.set("leader", ttl, leader);
        return leader;
    }

    protected AUnit defineLeader() {
        if (squad.isEmpty()) return null;

        Selection units = Alpha.get().units();
        AUnit building = Select.ourBuildings().first();

//        if (units.tanks().atLeast(2)) {
//            return units.tanks().mostDistantTo(building);
//        }

//        return potentialLeaders(units)
////            .mostDistantTo(building);
//            .mostDistantTo(Select.mainOrAnyBuilding());

//        ArrayList<Integer> xCoords = new ArrayList<>();
//        ArrayList<Integer> yCoords = new ArrayList<>();
//
//        for (AUnit unit : squad.list()) {
//            xCoords.add(unit.x());
//            yCoords.add(unit.y());
//        }
//
//        Collections.sort(xCoords);
//        Collections.sort(yCoords);
//
//        APosition median = new APosition(xCoords.get(xCoords.size() / 2), yCoords.get(yCoords.size() / 2));
//        Selection potentials = Alpha.get()
//            .units()
//            .groundUnits()
//            .excludeMedics();

        APosition median = squad.average();

        AUnit nearestToMedian = potentialLeaders(units)
            .nearestTo(median);

//        if (nearestToMedian == null) {
//            nearestToMedian = potentials.nearestTo(median);
//        }
//
//        if (nearestToMedian != null && We.terran()) {
//            if (Count.tanks() >= 2) {
//                return Select.ourTanks().nearestTo(nearestToMedian);
//            }
//
//            if (Count.medics() >= 2) {
//                return Select.ourOfType(AUnitType.Terran_Medic).nearestTo(nearestToMedian);
//            }
//        }
//
        return nearestToMedian;
    }

    private static Selection potentialLeaders(Selection units) {
        return units
            .groundUnits()
            .excludeMedics()
            .notSpecialAction();
    }

}
