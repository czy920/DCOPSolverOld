package com.cqu.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.cqu.core.ThreadEx;

public class ConsoleRedirectThread extends ThreadEx{
	
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
		String newLine = null;
		while (isRunning==true) {
			try {
				newLine = reader.readLine();
			} catch (IOException ioe) {
				break;
			}
			if (newLine == null) {
				break;
			} else {
				listener.newLineAvailable(newLine);
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
