package atlantis.battle;

import atlantis.battle.advance.focus.AFocusPoint;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of units. Phase 0 of the combat cutover
 * (_AI/__PLAN-COMBAT.md): a plain holder (members + mission + a few
 * timestamps) - the cohesion/scout/leader machinery that used to drive
 * it is deleted. BattleCommander owns the single cluster from step 5;
 * until then squads only answer reads.
 */
public class Squad {
    private final String name;
    private final List<AUnit> units = new ArrayList<>();
    private Mission mission;
    private boolean retreating = false;
    private int lastUnderAttack = -99999;
    private int lastShot = -99999;
    private int lastAttacked = -99999;
    private SquadTargeting targeting = new SquadTargeting();

    public Squad(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean isX() {
        return false;
    }

    public boolean isAlpha() {
        return "Alpha".equals(name);
    }

    public boolean isEmpty() {
        return units.isEmpty();
    }

    public boolean isNotEmpty() {
        return !units.isEmpty();
    }

    public int size() {
        return units.size();
    }

    public void addUnit(AUnit unit) {
        if (unit != null && !units.contains(unit)) units.add(unit);
    }

    public void removeUnit(AUnit unit) {
        units.remove(unit);
    }

    public AUnit leader() {
        return units.isEmpty() ? null : units.get(0);
    }

    public APosition median() {
        return (APosition) center();
    }

    public AUnit alphaLeader() {
        return leader();
    }

    public boolean isMissionDefend() {
        return mission().equals(Missions.DEFEND);
    }

    public boolean isMissionAttack() {
        return mission().equals(Missions.ATTACK);
    }

    public boolean isMissionDefendOrSparta() {
        return isMissionDefend();
    }

    public static HasPosition alphaCenter() {
        return Alpha.alphaCenter();
    }

    public AUnit squadScout() {
        // BATTLE CUTOVER: scout assignment is deleted - no scout attached.
        return null;
    }

    public HasPosition center() {
        if (units.isEmpty()) return null;
        long x = 0, y = 0;
        int n = 0;
        for (AUnit unit : units) {
            if (unit == null || !unit.hasPosition()) continue;
            x += unit.position().getX();
            y += unit.position().getY();
            n++;
        }
        if (n == 0) return null;
        return atlantis.map.position.APosition.createFromPixels((int) (x / n), (int) (y / n));
    }

    public double radius() {
        HasPosition center = center();
        if (center == null) return 0;
        double max = 0;
        for (AUnit unit : units) {
            if (unit == null || !unit.hasPosition()) continue;
            max = Math.max(max, unit.distTo(center));
        }
        return max;
    }

    public int cohesionPercent() {
        return 100;
    }

    public Selection selection() {
        return Select.from(units, "squad:" + name);
    }

    public Mission mission() {
        return mission != null ? mission : Missions.DEFEND;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public SquadTargeting targeting() {
        return targeting;
    }

    public boolean isRetreating() {
        return retreating;
    }

    public void setRetreating(boolean retreating) {
        this.retreating = retreating;
    }

    public void markLastUnderAttackNow() {
        lastUnderAttack = atlantis.game.A.now();
    }

    public void markLastShotNow() {
        lastShot = atlantis.game.A.now();
    }

    public void markLastAttackedNow() {
        lastAttacked = atlantis.game.A.now();
    }

    public AFocusPoint focusPoint() {
        Mission mission = mission();
        return mission != null ? mission.focusPoint() : null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Squad && name.equals(((Squad) other).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Squad{" + name + ", " + units.size() + "}";
    }

    /**
     * Minimal targeting memory: the last target while alive. The shared
     * targeting logic lives in battle.targeting; this only remembers.
     */
    public static class SquadTargeting {
        private AUnit lastTarget;

        public AUnit lastTargetIfAlive() {
            return (lastTarget != null && lastTarget.isAlive()) ? lastTarget : null;
        }

        public void setLastTarget(AUnit target) {
            lastTarget = target;
        }
    }
}
