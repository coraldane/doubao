package com.liuyun.doubao.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Stopable;
import com.liuyun.doubao.utils.SysUtils;

public abstract class StopableThread extends Thread implements Stopable {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static final int DEFAULT_POLL_TIMEOUT = 3;
	
	protected static volatile boolean running = true;
	private volatile boolean continued = true;
	private volatile boolean stoped = false;
	
	protected Context context = null;
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	public void run(){
		try{
			while(running || continued){
				boolean result = doTask(this.context);
				if(!result){
					continued = false;
					SysUtils.sleep(1000);//if return false please sleep one second to forbid CPU 100%
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
			SysUtils.sleep(100);
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
