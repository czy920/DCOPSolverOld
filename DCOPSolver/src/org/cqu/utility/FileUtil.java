package org.cqu.utility;

import java.io.FileOutputStream;

public class FileUtil {
	
	public static void writeString(String data, String file_path)
	{
		try {
			FileOutputStream out_s=new FileOutputStream(file_path);
			out_s.write(data.getBytes());
			out_s.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeStringAppend(String data, String file_path)
	{
		try {
			FileOutputStream out_s=new FileOutputStream(file_path,true);
			out_s.write(data.getBytes());
			out_s.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
