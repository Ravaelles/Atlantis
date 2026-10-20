package atlantis.battle.cluster;

import atlantis.architecture.Manager;
import atlantis.battle.advance.focus.AFocusPoint;
import atlantis.units.AUnit;

/**
 * What the army is for right now. Phase 0 of the combat cutover
 * (_AI/__PLAN-COMBAT.md): a named value with an optional focus point -
 * the arbitration that used to pick it (MissionChanger, Sparta logic)
 * is deleted, BattleCommander sets it from step 5 on. Until then every
 * mission reads DEFEND.
 */
public class Mission {
    private final String name;
    private AFocusPoint focusPoint;

    public Mission(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean isMissionAttack() {
        return "Attack".equals(name);
    }

    public boolean isMissionDefend() {
        return "Defend".equals(name);
    }

    public boolean isMissionDefendOrSparta() {
        return "Defend".equals(name);
    }

    public String letter() {
        return name.isEmpty() ? "?" : name.substring(0, 1);
    }

    public AFocusPoint focusPoint() {
        return focusPoint;
    }

    public void setFocusPoint(AFocusPoint focusPoint) {
        this.focusPoint = focusPoint;
    }

    /**
     * BATTLE CUTOVER: no manager produces units anymore, so no mission
     * handles one. Callers treat null as "dormant".
     */
    public Manager handleManagerClass(AUnit unit) {
        return null;
    }

    public boolean allowsToRetreat(AUnit unit) {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Mission && name.equals(((Mission) other).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Mission{" + name + "}";
    }
}
