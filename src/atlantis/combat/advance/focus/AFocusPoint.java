package atlantis.combat.advance.focus;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;

/**
 * POSITION - where units should concentrate around. Can be offensive or defensive. Usually around a choke.
 * <p>
 * LOOKING FROM - Units can position themselves from the wrong side of focus point.
 * This point should tell units from which side they should stand.
 */
public class AFocusPoint extends APosition {
    private HasPosition fromSide = null;
    private AChoke choke = null;
    private AUnit unit = null;
    private HasPosition position = null;
    private String name = null;

    // =========================================================

//    public AFocusPoint(HasPosition position) {
//        super(position.position());
//    }
//
//    public AFocusPoint(HasPosition position, String name) {
//        super(position.position());
//        this.name = name;
//    }

    public AFocusPoint(AUnit unit, String name) {
//        this(unit.position(), null, name);
        this(unit, null, name);
        this.unit = unit;
//        this.position = unit.position();
    }

    public AFocusPoint(AUnit unit, HasPosition fromSide, String name) {
        this(unit.position(), fromSide, name);
//        this(unit, fromSide, name);
        this.unit = unit;
        this.fromSide = fromSide;
        this.name = name;
//        this.position = unit.position();
    }

//    public AFocusPoint(HasPosition position, String name) {
//        this(position, null, name);
//    }

//    public AFocusPoint(AChoke choke, HasPosition fromSide) {
//        this(choke, fromSide, null);
//    }

    public AFocusPoint(AChoke choke, HasPosition fromSide, String name) {
        super(choke.center());
        this.choke = choke;
        this.fromSide = fromSide.position();
        this.name = name;
    }

    public AFocusPoint(HasPosition position, HasPosition fromSide, String name) {
        super(position.position());
        this.fromSide = fromSide != null ? fromSide.position() : null;
        this.name = name;
    }

    // =========================================================

    public boolean isValid() {
        if (unit != null) {
            return (!unit.isVisibleUnitOnMap() && unit instanceof AbstractFoggedUnit)
                || (unit.hp() > 0 && unit.isVisibleUnitOnMap());
        }
        else {
            APosition pos = this.position();
            if (pos == null) return false;
//            return !pos.isPositionVisible() || Select.our().inRadius(5, pos).empty();
            return !pos.isPositionVisible() || Select.our().inRadius(3.5, pos).empty();
        }
    }

    // =========================================================

    @Override
    public String toString() {
        return "Focus{" +
            "name='" + name + '\'' +
            ", unit=" + unit +
            ", pos=" + position +
//            ", from=" + fromSide +
//            ", choke=" + choke +
            '}';
    }

    // =========================================================

    public HasPosition fromSide() {
        return fromSide;
    }

    public AChoke choke() {
        return choke;
    }

    public AUnit unit() {
        return unit;
    }

    public boolean isAroundChoke() {
        return choke != null;
    }

    public boolean isUnit() {
        return unit != null;
    }

    public String name() {
        return name;
    }

    public AFocusPoint forceAroundChoke(AChoke choke) {
        this.choke = choke;
        return this;
    }

    public int chokeWidthOr(int fallback) {
        return choke != null ? choke.width() : fallback;
    }

    public boolean nameContains(String... substring) {
        if (name == null) return false;

        for (String s : substring) {
            if (name.contains(s)) return true;
        }

        return false;
    }

//    @Override
//    public APosition position() {
//        if (unit != null) {
//            return unit.position();
////            if (unit.isVisibleUnitOnMap()) return unit.position();
////            return unit.lastPosition();
//        }
//        if (choke != null) return choke.position();
//        return position != null ? position.position() : null;
//    }
//
//    @Override
//    public int x() {
//        return position().x();
//    }
//
//    @Override
//    public int y() {
//        return position().y();
//    }
}
