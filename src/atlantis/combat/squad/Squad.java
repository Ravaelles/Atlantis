package atlantis.combat.squad;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.delta.Delta;
import atlantis.combat.squad.positioning.SquadCohesion;
import atlantis.combat.squad.squad_scout.DefineSquadScout;
import atlantis.combat.squad.transfers.SquadReinforcements;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

/**
 * Represents battle squad (unit squad) that contains multiple battle units (could be one unit as well).
 */
public abstract class Squad extends Units {

    private final int ID = firstFreeID++;
    private static int firstFreeID = 1;
    private static Cache<Boolean> cacheBoolean = new Cache<>();
    private static Cache<Double> cacheDouble = new Cache<>();
    private static Cache<Integer> cacheInteger = new Cache<>();
    private static Cache<AUnit> cacheUnit = new Cache<>();

    /**
     * Auxilary name for the squad e.g. "Alpha", "Bravo", "Delta".
     */
    private String name;

    /**
     * Current mission object for this squad.
     */
    private Mission mission;

    /**
     * Unit that is considered to be "center" of this squad.
     */
    protected AUnit _leader = null;

    private SquadTargeting targeting = new SquadTargeting();

    private SquadCenter squadCenter = new SquadCenter(this);
    private SquadCohesion squadCohesion = new SquadCohesion(this);
    private int _lastAttacked = -76543;
    private int _lastUnderAttack = -87654;

    // =========================================================

    public Squad(String name, Mission mission) {
        this.name = name;
        this.setMission(mission);
        AllSquads.all().add(this);
    }

    // =========================================================

    public abstract boolean shouldHaveThisSquad();

    public boolean allowsSideQuests() {
        return false;
    }

    // =========================================================

    /**
     * Returns median <b>position</b> of all units. It's better than the average, because the outliners
     * don't affect the end result so badly.
     */
    private APosition _centerUnitPosition = null;

    // === Getters =============================================

    public static HasPosition alphaCenter() {
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
//        return _getcenterUnitPosition = new APosition(totalX / size(), totalY / size());
//    }

    /**
     * Center of this squad.
     */
    public HasPosition center() {
        if (size() <= 0) return null;

        APosition median = median();
        if (median == null) return leader();

        AUnit medianUnit = units().groundUnits().nearestTo(median);

        return medianUnit != null ? medianUnit : leader();

//        if (squadCenter.isInvalid(_leader)) _leader = squadCenter.leader();
//
//        return _leader != null ? _leader.position() : null;
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
        if (mission == this.mission) {
            return;
        }

//        if (this.mission != null) {
//            System.err.println("Squad " + name() + " change mission: " + mission);
//        }

//        if (size() > 0 && mission.isMissionDefend()) {
//            A.printStackTrace("Why DEFEND? " + mission);
//        }

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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Squad)) return false;

        Squad squad = (Squad) o;
        return ID == squad.ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

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

    public AUnit squadScout() {
        return cacheUnit.getIfValid(
            "squadScout",
            -1,
            () -> (new DefineSquadScout(this)).define()
        );
    }

    public AUnit leader() {
        return squadCenter.leader();
    }

    public void changeLeader() {
        squadCenter.refreshLeader(leader());
    }

//    public HasPosition tankMedian() {
//
//    }

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
                HasPosition center = center();
                if (center == null) {
                    return 0.0;
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

                return (double) focusPoint.distTo(center());
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

    public int cohesionPercent() {
        return cacheInteger.get(
            "cohesionPercent",
            3,
            () -> {
                HasPosition center = center();
                if (size() <= 1 || center == null) {
                    return 100;
                }

                double withinSquadRadius = selection().inRadius(radius(), center).count();

                return (int) (100 * withinSquadRadius / size());
            }
        );
    }

    public Selection units() {
        return selection();
    }

    public boolean hasMostlyOffensiveRole() {
        return cacheBoolean.get(
            "hasMostlyOffensiveRole",
            -1,
            () -> Delta.get().equals(this)
        );
    }

    public double radius() {
        return cacheDouble.get(
            "radius",
            17,
            () -> squadCohesion.squadMaxRadius()
        );
    }

    public boolean isLeader(AUnit unit) {
        return unit.equals(_leader);
    }

    public boolean isCohesionPercentOkay() {
        return squadCohesion.isSquadCohesionOkay();
    }

    public void handleReinforcements() {
        (new SquadReinforcements(this)).handleReinforcements();
    }

    public SquadTargeting targeting() {
        return targeting;
    }

    public boolean isRetreating() {
        AUnit leader = leader();
        return leader != null && leader.isRetreating();
    }

    public boolean isAlpha() {
        return Alpha.get().equals(this);
    }

    public boolean lastUnderAttackLessThanAgo(int threshold) {
        return A.ago(_lastUnderAttack) <= threshold;
    }

    public boolean lastAttackedLessThanAgo(int threshold) {
        return A.ago(_lastAttacked) <= threshold;
    }

    public void markLastUnderAttackNow() {
        _lastUnderAttack = A.now;
    }

    public void markLastAttackedNow() {
        _lastAttacked = A.now;
    }
}
