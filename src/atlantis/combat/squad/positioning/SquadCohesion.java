package atlantis.combat.squad.positioning;

import atlantis.combat.squad.HasSquad;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Count;
import atlantis.util.We;

public class SquadCohesion extends HasSquad {

    public SquadCohesion(Squad squad) {
        super(squad);
    }

    public boolean isSquadCohesionOkay() {
        HasPosition squadCenter = squad.center();
        if (squad == null || squadCenter == null) return true;

        int cohesionPercent = squad.cohesionPercent();
        return cohesionPercent >= minCohesion();
    }

    private int minCohesion() {
        if (GamePhase.isEarlyGame()) {
            if (A.supplyUsed() <= 25) {
                return 85;
            }

            return 76;
        }

        return 70;
    }

    public double squadMaxRadius() {
        double base = 0;

//        if (We.terran()) {
//            base = 0;
//        }
        if (We.protoss()) {
//            base = (squad.size() >= 8 ? 3 : 0);
            return Math.max(2.7, base + Math.sqrt(squad.size()));
        }
        else if (We.zerg()) {
            base = Math.min(8, 2 + (squad.size() / 3));
        }

        double tanksBonus = Math.min(6, (Count.tanks() >= 2 ? (1 + Count.tanks() / 1.6) : 0));

        return Math.max(2.7, base + Math.sqrt(squad.size()) + tanksBonus);
    }
}
