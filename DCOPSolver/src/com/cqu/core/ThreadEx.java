package com.cqu.core;

public abstract class ThreadEx{
	
	private Thread thread;
	protected boolean isRunning=false;
	
	public ThreadEx() {
		// TODO Auto-generated constructor stub
		thread=new Thread(task);
	}
	
	public void start()
	{
		this.thread.start();
	}
	
	public synchronized void stopRunning()
	{
		isRunning=false;
		thread.interrupt();
	}
	
	public synchronized boolean isRunning()
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
