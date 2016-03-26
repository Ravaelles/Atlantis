package bwapi;

import java.lang.String;
import java.lang.System;

/**
 *
 */
public class Utils {

    public static final byte Previous = 0x01;
    public static final byte Cyan = 0x02;
    public static final byte Yellow = 0x03;
    public static final byte White = 0x04;
    public static final byte Grey = 0x05;
    public static final byte Red = 0x06;
    public static final byte Green = 0x07;
    public static final byte Red_P1 = 0x08;
    public static final byte Tab = 0x09;
    public static final byte Newline = 0x0A;
    public static final byte Invisible_no_override = 0x0B;
    public static final byte Remove_beyond = 0x0C;
    public static final byte Clear_formatting = 0x0D;
    public static final byte Blue = 0x0E;
    public static final byte Teal = 0x0F;
    public static final byte Purple = 0x10;
    public static final byte Orange = 0x11;
    public static final byte Right_Align = 0x12;
    public static final byte Center_Align = 0x13;
    public static final byte Invisible = 0x14;
    public static final byte Brown = 0x15;
    public static final byte White_p7 = 0x16;
    public static final byte Yellow_p8 = 0x17;
    public static final byte Green_p9 = 0x18;
    public static final byte Brighter_Yellow = 0x19;
    public static final byte Cyan_default = 0x1A;
    public static final byte Pinkish = 0x1B;
    public static final byte Dark_Cyan = 0x1C;
    public static final byte Greygreen = 0x1D;
    public static final byte Bluegrey = 0x1E;
    public static final byte Turquoise = 0x1F;

    public static String formatText(String text, byte format){
        byte[] textData = text.getBytes();
        int textDataLength = text.length();

        byte[] newTextData = new byte[textDataLength + 1];
        newTextData[0] = format;
        System.arraycopy(textData, 0, newTextData, 1, textDataLength);
        return new String(newTextData);
    }
}