package atlantis.production.dynamic.protoss.reinforce;

import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Select;

public class ProtossSecureBaseWithCannons {
    private HasPosition initialPosition;
    private HasPosition aroundPosition;
    private APosition natural;

    public ProtossSecureBaseWithCannons(HasPosition base) {
        this.initialPosition = base;

        if (isForNatural()) {
            this.aroundPosition = forNaturalBase();
        }
        else {
            this.aroundPosition = forNonNaturalBase();
        }
    }

    private HasPosition forNonNaturalBase() {
        APosition center = Select.minerals().inRadius(10, initialPosition).center();

//        System.err.println("center = " + center);
        if (center == null) return initialPosition.makeBuildable(10);
//        System.err.println("BB");

        AChoke choke = center.position().nearestChoke();

        return initialPosition
            .translatePercentTowards(center, 50)
            .translatePercentTowards(initialPosition, 110)
            .translatePercentTowards(initialPosition, 110)
            .translateTilesTowards(2, choke)
            .makeBuildable(10);
    }

    public ProductionOrder reinforce() {
//        System.out.println(A.minSec() + " --------- Reinforce " + initialPosition + " with CANNON at " + aroundPosition);

        return RequestCannonAt.at(aroundPosition);
    }

    private HasPosition forNaturalBase() {
        AChoke choke = Chokes.natural();
        if (choke == null) return initialPosition.makeBuildable(10);

        HasPosition position = translateChokeTowardsOurSide(choke);
        if (position == null) position = choke;

        return position.makeBuildable(10);
    }

    private APosition translateChokeTowardsOurSide(AChoke choke) {
        return initialPosition.translateTilesTowards(choke, 2);
    }

    private boolean isForNatural() {
        natural = DefineNaturalBase.natural();
//        System.err.println("main = " + Select.main());
//        System.err.println("main bl = " + BaseLocations.main());
//        System.err.println("natural bl = " + BaseLocations.natural());
//        System.err.println("natural = " + natural);

        return natural != null && initialPosition.groundDist(natural) <= 9;
    }
}
