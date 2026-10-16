package atlantis.combat.squad.commanders;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.game.A;
import atlantis.units.AUnit;

/**
 * Commands all existing battle squads.
 */
public class ASquadCommander extends Commander {
    private Squad squad;

    public ASquadCommander(Squad squad) {
        this.squad = squad;
    }

    /**
     * Acts with all units that are part of given battle squad.
     *
     * @return
     */
    @Override
    public boolean handle() {
        if (A.everyNthGameFrame(11)) {
            SquadMissionChanger.changeSquadMissionIfNeeded(squad);
        }

        // Act with every combat unit
        for (AUnit unit : squad.units().list()) {
            Manager manager = (new CombatUnitManager(unit)).invokeFrom(this);

//            System.err.println(A.now() + " - " + unit + " / " + manager);

//            AAdvancedPainter.paintTextCentered(
//                unit.position().translateByTiles(0, 0.6),
//                A.now() + "-" + unit.manager().toString(),
//                Color.Teal
//            );
        }
        return false;
    }
}
