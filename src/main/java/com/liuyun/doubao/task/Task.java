package com.liuyun.doubao.task;

import com.liuyun.doubao.ctx.Context;

public interface Task extends Runnable {

	void init(Context context);
	void destroy(Context context);
	
	/**
	 * 
	 * @param context
	 * @return 任务是否继续
	 * @throws Exception
	 */
	boolean doTask(Context context) throws Exception;
}
