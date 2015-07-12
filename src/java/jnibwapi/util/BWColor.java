package jnibwapi.util;

public enum BWColor {
	Red(111), Blue(165), Teal(159), Purple(164), Orange(179), Brown(19), White(255), Yellow(135), Green(117), Cyan(128), Black(
			0), Grey(74);

	private int id;

	private BWColor(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	// =========================================================
	// ===== Start of ATLANTIS CODE ============================
	// =========================================================

	/**
	 * Returns string that if prepended to Atlantis.getBwapi().drawText() method, will change the color of the text.
	 */
	public static String getColorString(BWColor color) {
		switch (color) {
		case Cyan:
			return "\u0002";
		case Yellow:
			return "\u0003";
		case White:
			return "\u0004";
		case Grey:
			return "\u0005";
		case Red:
			return "\u0006";
		case Green:
			return "\u0007";
		case Blue:
			return "\u000E";
		case Purple:
			return "\u0010";
		case Orange:
			return "\u0011";
		case Brown:
			return "\u0015";
			// case :
			// return "\u0018";
			// case :
			// return "\u0019";
			// case :
			// return "\u001A";
			// case :
			// return "\u001B";
			// case :
			// return "\u001C";
			// case :
			// return "\u001D";
			// case :
			// return "\u001E";
		case Teal:
			return "\u001F";
		default:
			return "COLOR_ERROR";
		}
	}

}
