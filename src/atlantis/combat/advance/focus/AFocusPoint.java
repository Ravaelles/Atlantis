package atlantis.combat.advance.focus;

import atlantis.map.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;

/**
 * POSITION - where units should concentrate around. Can be offensive or defensive. Usually around a choke.
 *
 * LOOKING FROM - Units can position themselves from the wrong side of focus point.
 * This point should tell units from which side they should stand.
 */
public class AFocusPoint extends APosition {

    private APosition fromSide = null;
    private AChoke choke = null;
    private AUnit unit = null;
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
        this(unit.position(), null, name);
        this.unit = unit;
    }

    public AFocusPoint(AUnit unit, HasPosition fromSide, String name) {
        this(unit.position(), fromSide, name);
        this.unit = unit;
    }

//    public AFocusPoint(HasPosition position, String name) {
//        this(position, null, name);
//    }

    public AFocusPoint(AChoke choke, HasPosition fromSide) {
        this(choke, fromSide, null);
    }

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
            return !this.isPositionVisible() || Select.our().inRadius(3, this).empty();
        }
    }

    // =========================================================

    @Override
    public String toString() {
        return "Focus{" +
            "name='" + name + '\'' +
            ", unit=" + unit +
//            ", from=" + fromSide +
//            ", choke=" + choke +
            '}';
    }

    // =========================================================

    public APosition fromSide() {
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

    public String getName() {
        return name;
    }
}
