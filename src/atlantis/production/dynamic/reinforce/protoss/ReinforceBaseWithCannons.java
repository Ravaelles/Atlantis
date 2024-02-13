package atlantis.production.dynamic.reinforce.protoss;

import atlantis.architecture.Commander;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

public class ReinforceBaseWithCannons extends Commander {
    private HasPosition initialPositionToReinforce;
    private HasPosition finalPosition;
    private APosition natural;

    public ReinforceBaseWithCannons(HasPosition position) {
        this.initialPositionToReinforce = position;
        this.finalPosition = position;

        if (isForNatural()) {
            this.finalPosition = modifyPositionForNatural();
        }
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected void handle() {
        System.err.println("Reinforce " + initialPositionToReinforce + " with BUNKER at " + finalPosition);
//        haveCannonAtTheNearestChoke();
        RequestCannonAt.at(finalPosition);
    }

    private HasPosition modifyPositionForNatural() {
        AChoke choke = Chokes.natural();
        if (choke == null) return initialPositionToReinforce;

        return translateChokeTowardsOurSide(choke);
    }

    private APosition translateChokeTowardsOurSide(AChoke choke) {
        return initialPositionToReinforce.translateTilesTowards(choke, 3.4);
    }

//    private void haveCannonAtTheNearestChoke() {
//        AddToQueue.withTopPriority(Protoss_Photon_Cannon, finalPosition);
//    }

    private boolean isForNatural() {
        natural = DefineNaturalBase.natural();

        return natural != null && initialPositionToReinforce.distToLessThan(natural, 5);
    }
}
