package atlantis.combat.micro.terran.bunker.position;

import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.constructing.position.terran.TerranPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
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
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("NewBunkerPositionFinder: positionToSecure got null");
            this.positionToSecure = Select.ourBases().last();
        }
        if (builder == null) this.builder = FreeWorkers.get().nearestTo(positionToSecure);
    }

    public APosition find() {
        if (isInputInvalid()) {
            ErrorLog.printMaxOncePerMinute("NewBunkerPositionFinder: invalid / " + positionToSecure + " / " + builder);
            return null;
        }

        APosition foundPosition = TerranPositionFinder.findStandardPositionFor(
            builder,
            Terran_Bunker,
            positionToSecure,
            10
        );

        return validateOutput(foundPosition, positionToSecure);
    }

    private APosition validateOutput(APosition output, HasPosition near) {
        if (output != null) {
            double distTo = output.distTo(this.positionToSecure);
            int MAX_DIST = 12;

            if (distTo > MAX_DIST || (near != null && near.distTo(output) > MAX_DIST)) {
                ErrorLog.printMaxOncePerMinute(
                    "NewBunkerPositionFinder: position too far (" + distTo + ") / " +
                        "found:" + output + ", securing: " + this.positionToSecure
                );
                return null;
            }

            if (
                this.positionToSecure.distTo(near) >= 10
                    && !this.positionToSecure.regionsMatch(output)
            ) {
                ErrorLog.printMaxOncePerMinute("NewBunkerPositionFinder: wrong region");
                return null;
            }
        }

        return output;
    }

//    private boolean isForNatural() {
//        return nearestBasePosition.distTo(naturalBase) <= 9;
//    }

    private boolean isInputInvalid() {
        if (positionToSecure == null) return true;

        nearestBasePosition = NextBasePosition.nextBasePosition();
        naturalBase = DefineNaturalBase.natural();

        // @FIX?
        return nearestBasePosition == null && naturalBase == null;
//        return nearestBasePosition == null || naturalBase == null;
    }
}
