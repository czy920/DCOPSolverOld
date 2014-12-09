package com.cqu.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	
	public static String currentTime()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		return df.format(new Date());// new Date()为获取当前系统时间
	}
}
