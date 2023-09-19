package atlantis.combat.micro.transport;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Color;

public class TransportUnits extends Manager {

    public TransportUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit() &&
            (unit.isMoving() && !unit.isAttackingOrMovingToAttack());
    }

    @Override
    protected Manager handle() {
        if (handleLoad()) {
            return usedManager(this);
        }

        if (unloadFromTransport()) {
            return usedManager(this);
        }

        return null;
    }

    public boolean unloadFromTransport() {
        if (!unit.isLoaded() || unit.loadedInto() == null) return false;

        if (
            unit.isLoaded()
                && !unit.loadedInto().isBunker()
                && unit.lastActionMoreThanAgo(30 * 3, Actions.LOAD)
                && !isBabyInDanger(unit, true)
        ) {
            unit.loadedInto().unload(unit);
            unit.setTooltipTactical("Disembark");
            return true;
        }

        return false;
    }

    public boolean handleTransporting(AUnit transport, AUnit baby) {
        if (transport.isBunker()) return false;

        if (shouldLoadTheBaby(transport, baby)) {
            return loadTheBaby(transport, baby);
        }

        if (shouldDropTheBaby(transport, baby)) {
            return dropTheBaby(transport);
        }

        if (transport.hasCargo() && handleGoToSafety(transport, baby)) return true;

        if (followBaby(transport, baby)) return true;

        transport.setTooltipTactical("Nothing");
        return false;
    }

    // =========================================================

    public boolean handleLoad() {
        if (unit.isLoaded()) return false;

//        if (unit.cooldownRemaining() == 0) {
//            return false;
//        }

//        if (!unit.isRunning()) {
//            return false;
//        }

        if (shouldLoad()) {
            AUnit transport = Select.our().transports(true).inRadius(3, unit).nearestTo(unit);
            if (transport != null && transport.hasFreeSpaceFor(unit) && !transport.hasCargo()) {
                unit.load(transport);
                transport.load(unit);
                APainter.paintCircleFilled(unit, 7, Color.Blue);
                unit.setTooltipTactical("Embark!");
                return true;
            }

            for (AUnit anotherTransport : Select.our().transports(true).inRadius(5, unit).list()) {
                if (anotherTransport.hasFreeSpaceFor(unit)) {
                    unit.load(anotherTransport);
                    anotherTransport.load(unit);
                    unit.setTooltipTactical("Eeembark!");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldLoad() {
        if (!unit.is(AUnitType.Protoss_Reaver, AUnitType.Protoss_High_Templar, AUnitType.Terran_Siege_Tank_Tank_Mode))
            return false;

        // Always load when unit is moving, otherwise it walks instead of flying
        if (unit.isMoving() && unit.targetPositionAtLeastAway(1.5)) return true;

        // Don't load too often
        if (
            unit.lastActionLessThanAgo(8, Actions.LOAD)
                || unit.lastActionLessThanAgo(8, Actions.UNLOAD)
        ) return false;

        // Avoid ranged units
        if (unit.enemiesNear().ranged().canAttack(unit, 2.2).isEmpty()) return false;

        // Only run from melee if they really close
        if (unit.enemiesNear().melee().inRadius(1.5, unit).isEmpty()) return false;

        return false;
    }

    // =========================================================

    private boolean handleGoToSafety(AUnit transport, AUnit baby) {
        AUnit nearEnemy = EnemyUnits.discovered().canAttack(baby, 5).nearestTo(transport);
        if (nearEnemy != null) {
            transport.moveAwayFrom(nearEnemy, 8, Actions.MOVE_SAFETY, "ToSafety");
            APainter.paintLine(transport, transport.targetPosition(), Color.White);
            return true;
        }

        return false;
    }

    private boolean isBabyInDanger(AUnit baby, boolean allowMoreDangerousBehavior) {
        double safetyMargin = (allowMoreDangerousBehavior ? 0.5 : 2.5) + baby.woundPercent() / 100;
        boolean enemiesNear = baby.enemiesNear()
            .canAttack(baby, safetyMargin)
            .isNotEmpty();

        if (!allowMoreDangerousBehavior && baby.woundPercent() < 75 && enemiesNear) return true;

        if (baby.woundPercent() < 20 && enemiesNear) return true;

        return false;
    }

    private boolean isTransportInDanger(AUnit transport) {
        if (transport.woundPercent() < 80) return true;

        return transport.enemiesNear().canAttack(transport, 2.5).isNotEmpty();
    }

    private boolean followBaby(AUnit transport, AUnit baby) {
        if (!baby.isLoaded() && (baby.isMoving() || transport.distToMoreThan(baby, 0.2))) {
            return transport.move(baby, Actions.MOVE_FOLLOW, "Follow", true);
        }

        return false;
    }

    private boolean shouldLoadTheBaby(AUnit transport, AUnit baby) {

        return !baby.isLoaded()
            && transport.hasFreeSpaceFor(baby)
//                && transport.lastActionMoreThanAgo(25, UnitActions.LOAD)
            && transport.lastActionMoreThanAgo(8, Actions.UNLOAD)
            && (baby.isUnderAttack(15))
//                && (baby.cooldownRemaining() > 0 && baby.lastStartedAttackMoreThanAgo(9) && baby.lastFrameOfStartingAttackMoreThanAgo(7))
            && (!isTransportInDanger(transport) && isBabyInDanger(baby, false));
    }

    private boolean shouldDropTheBaby(AUnit transport, AUnit baby) {


        return baby.isLoaded()
            && transport.hasCargo()
//                && baby.cooldownRemaining() <= 8
            && transport.lastActionMoreThanAgo(25, Actions.LOAD)
            && (
            isTransportInDanger(transport)
                || transport.woundPercent() >= 87
                || !isBabyInDanger(baby, true)
                || transport.lastActionMoreThanAgo(30 * 12, Actions.LOAD)
        );
    }

    private boolean loadTheBaby(AUnit transport, AUnit baby) {
        transport.load(baby);
        baby.load(transport);
        baby.runningManager().stopRunning();
        transport.setTooltipTactical("LoadBaby");
        return true;
    }

    private boolean dropTheBaby(AUnit transport) {
        AUnit baby = transport.loadedUnits().get(0);
        transport.unload(baby);
        baby.unload(transport);
        transport.setTooltipTactical("DropBaby");
        return true;
    }

}
