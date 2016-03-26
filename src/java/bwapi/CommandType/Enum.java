package bwapi.CommandType;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public enum Enum {

	None(0),
	SetScreenPosition(1),
	PingMinimap(2),
	EnableFlag(3),
	Printf(4),
	SendText(5),
	PauseGame(6),
	ResumeGame(7),
	LeaveGame(8),
	RestartGame(9),
	SetLocalSpeed(10),
	SetLatCom(11),
	SetGui(12),
	SetFrameSkip(13),
	SetMap(14),
	SetAllies(15),
	SetVision(16),
	SetCommandOptimizerLevel(17);

	private int value;

	public int getValue(){
		return value;
	}

	Enum(int value){
		this.value = value;
	}

}
