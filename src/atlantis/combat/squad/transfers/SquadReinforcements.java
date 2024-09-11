package atlantis.combat.squad.transfers;

import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.units.AUnit;

import java.util.ArrayList;

public class SquadReinforcements {
    protected Squad toSquad;

    public SquadReinforcements(Squad toSquad) {
        this.toSquad = toSquad;
    }

    public void handleReinforcements() {
        int wantsMoreUnits = toSquad.wantsMoreUnits();

        if (wantsMoreUnits > 0) {
            transferFromAlpha(wantsMoreUnits);
        }
    }

    protected void transferUnitToSquad(AUnit unit) {
        if (unit.squad() != null) {
            unit.squad().removeUnit(unit);
        }

        toSquad.addUnit(unit);
        unit.setSquad(toSquad);
    }

    private void transferFromAlpha(int numberOfRecruits) {
        Alpha alpha = prepareAlfaForTransfer(toSquad);
        numberOfRecruits = validateNumberOfRecruits(numberOfRecruits, alpha);

        ArrayList<AUnit> recruits = defineRecruitsFromAlpha(alpha, numberOfRecruits);
        for (AUnit recruit : recruits) {
            transferUnitToSquad(recruit);
        }
    }

    protected int validateNumberOfRecruits(int numberOfRecruits, Alpha alpha) {
        return Math.min(numberOfRecruits, alpha.size());
    }

    private Alpha prepareAlfaForTransfer(Squad toSquad) {
        Alpha alpha = Alpha.get();
        alpha.sortByDistanceTo(toSquad.center(), true);
        return alpha;
    }

    protected ArrayList<AUnit> defineRecruitsFromAlpha(Alpha alpha, int numberOfRecruits) {
        ArrayList<AUnit> recruits = new ArrayList<>();

        for (int i = 0; i < numberOfRecruits; i++) {
            AUnit recruit = alpha.get(i);

            if (isGoodRecruit(recruit)) {
                continue;
            }

            recruits.add(recruit);
        }

        return recruits;
    }

    protected boolean isGoodRecruit(AUnit recruit) {
        return recruit.isAir() || !recruit.hasAnyWeapon() || recruit.isScienceVessel();
    }
}