package com.cqu.util;

import java.io.File;
import java.io.FileFilter;

/**
 * 用于对生成的DCOP问题xml文件重命名为规范化方式
 * 如01,02,03....10,11,12,.....
 * @author CQU
 *
 */
public class FormatFileNames {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dirPath="E:\\hz\\DCOP\\Matlab_DCOPSolver\\problems\\3_5-14_40";
		File[] files=new File(dirPath).listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if(pathname.getName().endsWith(".xml")==true)
				{
					return true;
				}
				return false;
			}
		});
		
		for(File f : files)
		{
			String fileName=f.getName();
			int index=fileName.indexOf('_');
			String part1=fileName.substring(0, index+1);
			int index2=fileName.indexOf('_', index+1);
			String part2=fileName.substring(index+1, index2);
			String part3=fileName.substring(index2);
			if(part2.length()==1)
			{
				f.renameTo(new File(dirPath+"\\"+part1+"0"+part2+part3));
			}
		}
	}

}
