package com.cqu.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class DialogUtil {
	
	/**
	 * 过滤文件的类型放在fileTypes数组中，比如{".jpg", ".png", ".bmp"},或者{".*"}
	 * @param fileTypes
	 * @return
	 */
	public static File dialogOpenFile(final String[] fileTypes, String title, String defaultDir)
	{
		JFileChooser jfileChooser=new JFileChooser();
		jfileChooser.setDialogTitle(title);
		jfileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		if(defaultDir!=null&&defaultDir.length()>0)
		{
			jfileChooser.setCurrentDirectory(new File(defaultDir));
		}
		jfileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				String desc="";
				for(int i=0;i<fileTypes.length;i++)
				{
					desc+=fileTypes[i]+";";
				}
				return desc;
			}
			
			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				if(file.isDirectory()==true)
				{
					return true;
				}
				String fileName=file.getName().toLowerCase();
				if(fileTypes[0].equals(".*")==true)
				{
					return true;
				}
				for(int i=0;i<fileTypes.length;i++)
				{
					if(fileName.endsWith(fileTypes[i])==true)
					{
						return true;
					}
				}
				return false;
			}
		});
		if(jfileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
		{
			return jfileChooser.getSelectedFile();
		}
		return null;
	}
	
	public static File dialogOpenDir(String title, String defaultDir)
	{
		JFileChooser jfileChooser=new JFileChooser();
		jfileChooser.setDialogTitle(title);
		if(defaultDir!=null&&defaultDir.length()>0)
		{
			jfileChooser.setCurrentDirectory(new File(defaultDir));
		}
		jfileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jfileChooser.showOpenDialog(jfileChooser);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {       
            return jfileChooser.getSelectedFile();
    	}
        return null;
	}
	
	public static void dialogWaring(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public static void dialogError(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public static void dialogInformation(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
}
