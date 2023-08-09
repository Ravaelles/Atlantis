package atlantis.combat.squad;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import bwapi.Color;

/**
 * Commands all existing battle squads.
 */
public class ASquadManager {

    private Squad squad;

    public ASquadManager(Squad squad) {
        this.squad = squad;
    }

    /**
     * Acts with all units that are part of given battle squad.
     */
    public void update() {
        if (A.everyNthGameFrame(11)) {
            SquadMissionChanger.changeSquadMissionIfNeeded(squad);
        }

        // Act with every combat unit
        for (AUnit unit : squad.units().list()) {
            (new CombatUnitManager(unit)).invoke();

//            AAdvancedPainter.paintTextCentered(
//                unit.position().translateByTiles(0, 0.6),
//                A.now() + "-" + unit.manager().toString(),
//                Color.Teal
//            );
        }
    }
}
