package com.atid.app.rfid.util;

import com.atid.lib.dev.rfid.type.MaskActionType;
import com.atid.lib.dev.rfid.type.MaskTargetType;

public class ArrayUtil {
	
	public static String toString(MaskActionType type) {
		switch (type) {
		case Assert_Deassert: return "assert SL or inventoried ¡æ A\r\ndeassert SL or inventoried ¡æ B";
		case Assert_DoNothing: return "assert SL or inventoried ¡æ A\r\ndo nothing";
		case DoNothing_Deassert: return "do nothing\r\ndeassert SL or inventoried ¡æ B";
		case Negate_DoNothing: return "negate SL or (A ¡æ B, B ¡æ A)\r\ndo nothing";
		case Deassert_Assert: return "deassert SL or inventoried ¡æ B\r\nassert SL or inventoried ¡æ A";
		case Deassert_DoNothing: return "deassert SL or inventoried ¡æ B\r\ndo nothing";
		case DoNothing_Assert: return "do nothing\r\nassert SL or inventoried ¡æ A";
		case DoNothing_Negate: return "do nothing\r\nnegate SL or (A ¡æ B, B ¡æ A)";
		}
		return "";
	}
	
	public static String toString(MaskTargetType type) {
		switch (type) {
		case S0: return "Session 0";
		case S1: return "Session 1";
		case S2: return "Session 2";
		case S3: return "Session 3";
		case SL: return "Session Flag";
		}
		return "";
	}
}
