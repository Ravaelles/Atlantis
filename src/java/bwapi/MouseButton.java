package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public enum MouseButton {

	M_LEFT(0),
	M_RIGHT(1),
	M_MIDDLE(2),
	M_MAX(3);

	private int value;

	public int getValue(){
		return value;
	}

	MouseButton(int value){
		this.value = value;
	}

}
