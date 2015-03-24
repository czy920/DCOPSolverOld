package org.cqu.core.control;

/**
 * 封装了线程的建立、启动、停止、初始化、执行、结束等控制逻辑
 * @author hz
 *
 */
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
			if(threadName==null||threadName.isEmpty())
			{
				thread=new Thread(task);
			}else
			{
				thread=new Thread(task, threadName);
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
	 * process主体
	 */
	protected abstract void runProcess();
	
	private Runnable task=new Runnable()
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			running=true;
			runProcess();
			running=false;
		}
	};
}
