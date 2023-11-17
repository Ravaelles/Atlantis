package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.base.define.DefineNatural;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.constructing.position.terran.TerranPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

import static atlantis.units.AUnitType.Terran_Bunker;

public class NewBunkerPositionFinder {
    private HasPosition positionToSecure;
    private AUnit builder;
    private APosition nearestBasePosition;
    private APosition naturalBase;

    public NewBunkerPositionFinder(HasPosition positionToSecure) {
        this(positionToSecure, null);
    }

    public NewBunkerPositionFinder(HasPosition positionToSecure, AUnit builder) {
        this.positionToSecure = positionToSecure;
        this.builder = builder;

        if (positionToSecure == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("NewBunkerPositionFinder: positionToSecure got null");
            this.positionToSecure = Select.ourBases().last();
        }
        if (builder == null) this.builder = Select.ourWorkers().nearestTo(positionToSecure);
    }

    public APosition find() {
        if (isNotValid()) {
            ErrorLog.printMaxOncePerMinute("NewBunkerPositionFinder: invalid / " + positionToSecure + " / " + builder);
            return null;
        }

        return TerranPositionFinder.findStandardPositionFor(
            builder,
            Terran_Bunker,
            positionToSecure,
            10
        );
    }

    private boolean isForNatural() {
        return nearestBasePosition.distTo(naturalBase) <= 9;
    }

    private boolean isNotValid() {
        if (positionToSecure == null) return true;

        nearestBasePosition = NextBasePosition.nextBasePosition();
        naturalBase = DefineNatural.natural();

        return nearestBasePosition == null || naturalBase == null;
    }
}
