package com.cqu.core;

public abstract class ThreadEx{
	
	private Thread thread;
	private String threadName;
	protected volatile boolean isRunning=false;
	
	public ThreadEx(String threadName) {
		// TODO Auto-generated constructor stub
		this.threadName=threadName;
	}
	
	public void start()
	{
		thread=new Thread(task, threadName);
		thread.start();
	}
	
	public void stopRunning()
	{
		isRunning=false;
		thread.interrupt();
	}
	
	public boolean isRunning()
	{
		return this.isRunning;
	}
	
	protected abstract void runProcess();
	
	private Runnable task=new Runnable()
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isRunning=true;
	        
			runProcess();
		}
		
	};
}
