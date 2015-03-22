package com.cqu.core;

public abstract class ProcessThread{
	
	private Thread thread;
	private String threadName;
	private volatile boolean running=false;
	
	public ProcessThread() {
		// TODO Auto-generated constructor stub
		this.threadName="";
	}
	
	public ProcessThread(String threadName) {
		// TODO Auto-generated constructor stub
		this.threadName=threadName;
	}
	
	/**
	 * 启动线程，只有一次启动机会，再次调用无效
	 */
	public void startProcess()
	{
		if(thread==null)
		{
			if(threadName.isEmpty()==false)
			{
				thread=new Thread(task, threadName);
			}else
			{
				thread=new Thread(task);
			}
			thread.start();
		}
	}
	
	/**
	 * 停止线程
	 */
	public void stopRunning()
	{
		if(thread!=null)
		{
			running=false;
			thread.interrupt();
		}
	}
	
	public boolean isRunning()
	{
		return this.running;
	}
	
	/**
	 * 初始化
	 */
	protected void initializeProcess(){}
	/**
	 * process主体
	 */
	protected abstract void runProcess();
	/**
	 * 结束
	 */
	protected void finalizeProcess(){}
	
	private Runnable task=new Runnable()
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			running=true;
	        initializeProcess();
			runProcess();
			finalizeProcess();
			running=false;
		}
	};
}
