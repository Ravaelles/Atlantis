package bwapi.ShapeType;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public enum Enum {

	None(0),
	Text(1),
	Box(2),
	Triangle(3),
	Circle(4),
	Ellipse(5),
	Dot(6);

	private int value;

	public int getValue(){
		return value;
	}

	Enum(int value){
		this.value = value;
	}

}
