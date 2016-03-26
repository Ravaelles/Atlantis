package bwapi.Latency;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public enum Enum {

	SinglePlayer(2),
	LanLow(5),
	LanMedium(7),
	LanHigh(9),
	BattlenetLow(14),
	BattlenetMedium(19);

	private int value;

	public int getValue(){
		return value;
	}

	Enum(int value){
		this.value = value;
	}

}
