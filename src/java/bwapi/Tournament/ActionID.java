package bwapi.Tournament;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public enum ActionID {

	EnableFlag(0),
	PauseGame(1),
	ResumeGame(2),
	LeaveGame(3),
	SetLocalSpeed(4),
	SetTextSize(5),
	SetLatCom(6),
	SetGUI(7),
	SetMap(8),
	SetFrameSkip(9),
	Printf(10),
	SendText(11);

	private int value;

	public int getValue(){
		return value;
	}

	ActionID(int value){
		this.value = value;
	}

}
