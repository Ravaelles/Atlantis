package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.define.BaseLocationsNearEnemy;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ScoutPotentialEnemyBases extends Manager {
    private static Map<HasPosition, Integer> lastSeenAtFrame = new HashMap<>();
    private Positions<ABaseLocation> baseLocations;
    private ABaseLocation currentBaseLocationFocus;

    public ScoutPotentialEnemyBases(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        baseLocations = BaseLocationsNearEnemy.get();
        if (baseLocations.empty()) return false;

        currentBaseLocationFocus = longestNotVisited();
        if (currentBaseLocationFocus == null) return false;

        return true;

//        updateLastSeenAtFrame();

//        return A.secondsAgo(lastSeenAtFrame.getOrDefault()) >= 20;
    }

    @Override
    protected Manager handle() {
        updateLastSeenAtFrame();

        if (unit.move(
            currentBaseLocationFocus, Actions.MOVE_SCOUT, "ScoutPotentialBase", true
        )) return usedManager(this);

        return null;
    }

    private void updateLastSeenAtFrame() {
        if (currentBaseLocationFocus.isPositionVisible()) {
            lastSeenAtFrame.put(currentBaseLocationFocus, A.now());
        }
    }

    private ABaseLocation longestNotVisited() {
        return baseLocations
            .list()
            .stream()
            .filter(base -> !base.isExplored())
            .min(Comparator.comparingInt(base -> lastSeenAtFrame.getOrDefault(base, -1)))
            .orElse(null);
    }
}
