package com.liuyun.doubao.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liuyun.doubao.common.InitializingBean;
import com.liuyun.doubao.ctx.Context;

public abstract class StopableThread implements InitializingBean, Runnable {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static final int DEFAULT_POLL_TIMEOUT = 3;
	
	private static volatile boolean running = true;
	private volatile boolean continued = true;
	private volatile boolean stoped = false;
	
	protected Context context = null;
	
	public StopableThread(Context context){
		this.context = context;
	}
	
	@Override
	public void run(){
		try{
			while(running || continued){
				boolean result = doTask(this.context);
				if(!result){
					continued = false;
				}
			}
			stoped = true;
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			doShutdown();
		}
	}
	
	public void stop(boolean waitCompleted){
		if(waitCompleted){
			running = false;
		} else {
			running = false;
			continued = false;
		}
	}
	
	private void doShutdown(){
		logger.info(this.getClass().getSimpleName() + " is shutdown.");
	}

	public void waitForStoped() {
		while(!this.stoped){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	/**
	 * 
	 * @param context
	 * @return 任务是否继续
	 * @throws Exception
	 */
	public abstract boolean doTask(Context context) throws Exception;
}
