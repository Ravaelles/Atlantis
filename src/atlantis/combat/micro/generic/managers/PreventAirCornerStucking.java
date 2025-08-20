package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class PreventAirCornerStucking extends Manager {
    public static final int MIN_TILES_TO_CORNER = 20;

    public PreventAirCornerStucking(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.shieldWound() <= 4) return false;

        return unit.position().isInMapCorner(getMinTilesToCorner());
    }

    @Override
    public Manager handle() {
        APosition goTo = goTo();

        if (goTo != null && unit.move(goTo, Actions.MOVE_AVOID, "PreventCornerStuck")) {
//            goTo.paintCircleFilled(8, Color.Yellow);
//            unit.paintLine(goTo, Color.Yellow);
            return usedManager(this);
        }

        return null;
    }

    private APosition goTo() {
        int dtx = (unit.tx() <= 30 ? 20 : -20);
        int dty = getMinTilesToCorner() + (unit.ty() <= 30 ? -15 : 15);

        return unit.position().translateByTiles(dtx, dty).makeValidFarFromBounds(1);
    }

    private int getMinTilesToCorner() {
        return MIN_TILES_TO_CORNER;
//        return (int) (8 + unit.woundPercent() / 7.0);
    }
}
