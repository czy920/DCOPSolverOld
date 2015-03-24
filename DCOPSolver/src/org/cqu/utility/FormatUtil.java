package org.cqu.utility;

import java.text.DecimalFormat;

public class FormatUtil {
	
	public static String format(double value, String formatString)
	{
		return new DecimalFormat(formatString).format(value);
	}
	
	public static String formatSize(int size)
	{
		if(size<1024)
		{
			return size+"B";
		}else if(size<1024*1024)
		{
			return FormatUtil.format(size*1.0/1024, "#.0")+"KB";
		}else if(size<1024*1024*1024)
		{
			return FormatUtil.format(size*1.0/(1024*1024), "#.0")+"MB"; 
		}else
		{
			return FormatUtil.format(size*1.0/(1024*1024*1024), "#.0")+"GB";
		}
	}
}
