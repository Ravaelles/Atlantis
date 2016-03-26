package bwapi;

import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;

public class PositionOrUnit {

    private Unit unit;

    private Position position;

    public PositionOrUnit(Unit unit){
        if(unit == null){
            throw new IllegalArgumentException("PositionOrUnit must not reference null!");
        };
        this.unit = unit;
    }

    public PositionOrUnit(Position position){
        if(position == null){
            throw new IllegalArgumentException("PositionOrUnit must not reference null!");
        };
        this.position = position;
    }

    public Unit getUnit(){
        return unit;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isUnit(){
        return unit != null;
    }

    public boolean isPosition(){
        return position != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionOrUnit)) return false;

        PositionOrUnit that = (PositionOrUnit) o;

        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) return false;

        return true;
    }
}