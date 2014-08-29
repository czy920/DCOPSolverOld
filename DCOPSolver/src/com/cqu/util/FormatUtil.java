package com.cqu.util;

import java.text.DecimalFormat;

public class FormatUtil {
	
	public static String format(double value, String formatString)
	{
		return new DecimalFormat(formatString).format(value);
	}
}
