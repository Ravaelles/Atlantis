package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.*;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.position.Positions;
import atlantis.production.orders.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranMissileTurretsForNonMain extends TerranMissileTurret {

    public static boolean buildIfNeeded() {
        if (!Have.engBay()) {
            return false;
        }

        if (Count.bases() == 1) {
            return false;
        }

        if (handleReinforcePosition(turretForNatural(), 7)) {
            System.out.println("Requested TURRET for NATURAL");
            return true;
        }

        return false;
    }

    // =========================================================

    protected static HasPosition turretForNatural() {
        APosition natural = Bases.natural();
        if (natural == null) {
            return null;
        }

        return natural.translateTilesTowards(Chokes.nearestChoke(natural), 10);
    }

}
