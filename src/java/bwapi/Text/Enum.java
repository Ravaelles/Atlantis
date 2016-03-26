package bwapi.Text;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public enum Enum {

	Previous(1),
	Default(2),
	Yellow(3),
	White(4),
	Grey(5),
	Red(6),
	Green(7),
	BrightRed(8),
	Invisible(11),
	Blue(14),
	Teal(15),
	Purple(16),
	Orange(17),
	Align_Right(18),
	Align_Center(19),
	Invisible2(20),
	Brown(21),
	PlayerWhite(22),
	PlayerYellow(23),
	DarkGreen(24),
	LightYellow(25),
	Cyan(26),
	Tan(27),
	GreyBlue(28),
	GreyGreen(29),
	GreyCyan(30);

	private int value;

	public int getValue(){
		return value;
	}

	Enum(int value){
		this.value = value;
	}

}
