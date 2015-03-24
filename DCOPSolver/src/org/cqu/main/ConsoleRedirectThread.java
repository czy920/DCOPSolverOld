package org.cqu.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.cqu.core.control.ProcessThread;


public class ConsoleRedirectThread extends ProcessThread{
	
	private PipedInputStream pis = new PipedInputStream();
	private BufferedReader reader = new BufferedReader(new InputStreamReader(pis));
	private PipedOutputStream pos;
	private NewLineListener listener;
	
	public ConsoleRedirectThread(NewLineListener listener) {
		super("ConsoleRedirectThread");
		// TODO Auto-generated constructor stub
		this.listener=listener;
		try {
			pos = new PipedOutputStream(pis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PipedOutputStream getOut() {
		return pos;
	}

	@Override
	protected void runProcess() {
		// TODO Auto-generated method stub
		String newLine="";
		char[] buf=new char[1];
		int readoutCount=0;
		while (isRunning()==true) {
			try {
				readoutCount=reader.read(buf);
				if(readoutCount!=-1)
				{
					newLine+=buf[0];
					if(buf[0]=='\n')
					{
						listener.newLineAvailable(newLine);
						newLine="";
					}
				}
			} catch (IOException ioe) {
				;
			}
		}
		try {
			pos.close();
			pis.close();
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static interface NewLineListener
	{
		void newLineAvailable(String newLine);
	}
}
