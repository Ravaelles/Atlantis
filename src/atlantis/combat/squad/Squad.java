package atlantis.combat.squad;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents battle squad (unit squad) that contains multiple battle units (could be one unit as well).
 */
public abstract class Squad extends Units {

    private final int ID = firstFreeID++;
    private static int firstFreeID = 1;
    private static Cache<AUnit> cache = new Cache<>();
    private static Cache<Double> cacheDouble = new Cache<>();

    /**
     * Auxilary name for the squad e.g. "Alpha", "Bravo", "Delta".
     */
    private String name;

    /**
     * Current mission object for this squad.
     */
    private Mission mission;

    // =========================================================

    public Squad(String name, Mission mission) {
        super();
        this.name = name;
        this.setMission(mission);
        ASquadManager.squads.add(this);
    }

    // =========================================================

    /**
     * Returns median <b>position</b> of all units. It's better than the average, because the outliners
     * don't affect the end result so badly.
     */
    private APosition _getMedianUnitPosition = null;

    // === Getters =============================================

    public static ArrayList<Squad> getSquads() {
        return ASquadManager.squads;
    }

    public static void setSquads(ArrayList<Squad> squads) {
        ASquadManager.squads = squads;
    }

    public static APosition alphaCenter() {
        return Alpha.get() != null ? Alpha.get().center() : null;
    }

    /**
     * Average
     */
//    public APosition center() {
//        if (size() == 0) {
//            return null;
//        }
//
//        int totalX = 0;
//        int totalY = 0;
//        for (AUnit unit : list()) {
//            totalX += unit.x();
//            totalY += unit.y();
//        }
//
//        return _getMedianUnitPosition = new APosition(totalX / size(), totalY / size());
//    }

    /**
     * Median
     */
    public APosition center() {
        if (size() <= 0) {
            return null;
        }

        AUnit medianUnit = medianUnit();

        return _getMedianUnitPosition = (medianUnit == null ? null : medianUnit.position());
    }

    private AUnit medianUnit() {
        int ttl = 600;
        AUnit medianUnit = cache.get(
                "medianUnit",
                ttl,
                this::defineMedianUnit
        );

        if (medianUnit != null && medianUnit.isAlive()) {
            return medianUnit;
        }

        medianUnit = this.defineMedianUnit();
        cache.set("medianUnit", ttl, medianUnit);
        return medianUnit;
    }

    private AUnit defineMedianUnit() {
        ArrayList<Integer> xCoords = new ArrayList<>();
        ArrayList<Integer> yCoords = new ArrayList<>();

        for (AUnit unit : list()) {
            xCoords.add(unit.x());
            yCoords.add(unit.y());
        }

        Collections.sort(xCoords);
        Collections.sort(yCoords);

        APosition median = new APosition(xCoords.get(xCoords.size() / 2), yCoords.get(yCoords.size() / 2));
        AUnit nearestToMedian = Select.ourCombatUnits().nearestTo(median);
        return nearestToMedian;
    }

    // =========================================================

    public abstract int expectedUnits();

    /**
     * Convenience name for the squad e.g. "Alpha", "Bravo", "Charlie", "Delta".
     */
    public String name() {
        return name;
    }

    /**
     * Convenience name for the squad e.g. "Alpha", "Bravo", "Charlie", "Delta".
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Current mission object for this squad.
     */
    public Mission mission() {
        return mission;
    }

    /**
     * Current mission object for this squad.
     */
    public void setMission(Mission mission) {
        if (mission == null) {
            throw new RuntimeException("Assigned null Mission to squad");
        }
        this.mission = mission;
    }

    /**
     * Manager for microing units.
     */
//    public AbstractMicroManager getMicroManager() {
//        return microManager;
//    }
//
//    /**
//     * Manager for microing units.
//     */
//    public void setMicroManager(AbstractMicroManager microManager) {
//        if (microManager == null) {
//            throw new RuntimeException("Assigned null MicroManager to squad");
//        }
//        this.microManager = microManager;
//    }
    public boolean isMainSquad() {
        return name.equals("Alpha");
    }

    /**
     * Returns ID for this battle squad (1, 2, 3, 4 etc).
     */
    public int getID() {
        return ID;
    }

    public String letter() {
        return name.charAt(0) + "";
    }

    // =========================================================

    @Override
    public String toString() {
        return "Squad " + name + " (" + size() + " units)";
    }

    // =========================================================

    public boolean isMissionContain() {
        return Missions.CONTAIN.equals(mission());
    }

    public boolean isMissionDefend() {
        return Missions.DEFEND.equals(mission());
    }

    public boolean isMissionAttack() {
        return Missions.ATTACK.equals(mission());
    }

    public boolean isFirstUnitInSquad(AUnit unit) {
        return unit.equals(first());
    }

    public AUnit getSquadScout() {
        if (!isMainSquad() || Count.ourCombatUnits() < 3) {
            return null;
        }

        Selection groundUnits = Select.from(this).groundUnits();
        AUnit ranged = groundUnits.ranged().first();
        if (ranged != null) {
//            System.out.println("ranged = " + ranged);
            return ranged;
        }

        return groundUnits.melee().first();
    }

    public boolean lessThanUnits(int units) {
        return size() < units;
    }

    public int wantsMoreUnits() {
        return expectedUnits() - size();
    }

    public double distToCenter(AUnit unit) {
        return cacheDouble.get(
                "distToCenter",
                5,
                () -> {
                    APosition center = center();
                    if (center == null) {
                        return 0;
                    }

                    return unit.distTo(center());
                }
        );
    }

    public double distToFocusPoint() {
        return cacheDouble.get(
                "distToFocusPoint",
                5,
                () -> {
                    Mission mission = mission();
                    if (mission == null) {
                        return 0;
                    }

                    AFocusPoint focusPoint = mission.focusPoint();
                    if (focusPoint == null) {
                        return 0;
                    }

                    return focusPoint.distTo(center());
                }
        );
    }

    public double groundDistToFocusPoint() {
        return cacheDouble.get(
                "groundDistToFocusPoint",
                8,
                () -> {
                    Mission mission = mission();
                    if (mission == null) {
                        return 0;
                    }

                    AFocusPoint focusPoint = mission.focusPoint();
                    if (focusPoint == null) {
                        return 0;
                    }

                    return focusPoint.groundDist(center());
                }
        );
    }
}
