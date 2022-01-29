package atlantis.combat.micro.terran;

import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.production.orders.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranMissileTurretsForNonMain extends TerranMissileTurret {

    private static final int MIN_TURRETS_PER_BASE = 2;

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

        if (handleTurretForAllBases()) {
            System.out.println("Requested TURRET for NON MAIN");
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean handleTurretForAllBases() {
        for (AUnit base : Select.ourBases().list()) {
            int existing = Count.existingOrPlannedBuildingsNear(turret, 8, base.position());

            if (existing < MIN_TURRETS_PER_BASE) {
                APosition minerals = Select.minerals().inRadius(12, base).center();
                if (minerals != null) {
                    AddToQueue.withHighPriority(turret, base.translateTilesTowards(4, minerals))
                            .setMaximumDistance(12);
                    return true;
                }
            }
        }

        return false;
    }

    protected static HasPosition turretForNatural() {
        APosition natural = Bases.natural();
        if (natural == null) {
            return null;
        }

        return natural.translateTilesTowards(Chokes.nearestChoke(natural), 10);
    }

}
