package atlantis.combat.micro.terran;

import atlantis.map.choke.AChoke;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranMissileTurretsForNonMain extends TerranMissileTurret {

    private  final int MIN_TURRETS_PER_BASE = 2;

    public boolean buildIfNeeded() {
        if (!Have.engBay()) {
            return false;
        }

        if (Count.bases() == 1) {
            return false;
        }

        if (handleReinforcePosition(turretForNatural(), 7)) {
//            System.out.println("Requested TURRET for NATURAL");
            return true;
        }

        if (handleTurretForAllBases()) {
//            System.out.println("Requested TURRET for NON MAIN");
            return true;
        }

        return false;
    }

    // =========================================================

    private boolean handleTurretForAllBases() {
        if (!Enemy.zerg()) {
            return false;
        }

        if (exceededExistingAndInProduction()) {
            return false;
        }

        int maxDist = 12;

        for (AUnit base : Select.ourBases().list()) {
            int existing = Count.existingOrPlannedBuildingsNear(type(), maxDist, base.position());

            if (existing < MIN_TURRETS_PER_BASE) {
                APosition minerals = Select.minerals().inRadius(maxDist, base).center();
                if (minerals != null) {
                    AddToQueue.withHighPriority(type(), base.translateTilesTowards(4, minerals))
                            .setMaximumDistance(maxDist);
                    return true;
                }
            }
        }

        return false;
    }

    protected HasPosition turretForNatural() {
        APosition natural = Bases.natural();
        if (natural == null) {
            return null;
        }

        AChoke choke = Chokes.nearestChoke(natural);
        if (choke == null) {
            return null;
        }

        return natural.translateTilesTowards(choke, 10);
    }

}
