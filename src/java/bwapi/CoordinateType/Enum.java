package bwapi.CoordinateType;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public enum Enum {

	None(0),
	Screen(1),
	Map(2),
	Mouse(3);

	private int value;

	public int getValue(){
		return value;
	}

	Enum(int value){
		this.value = value;
	}

}
