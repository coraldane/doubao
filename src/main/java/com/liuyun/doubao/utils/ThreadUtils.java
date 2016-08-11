package com.liuyun.doubao.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;

import com.google.common.base.Function;

/**
 * 
* @Description: 
* @version: v1.0.0
* @author: coral
* @date: Aug 11, 2016 11:03:25 AM
* @copyright liutian
* Modification History:
* Date         Author          Version            Description
*---------------------------------------------------------*
* Aug 11, 2016      coral          v1.0.0
 */
public class ThreadUtils {
	
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 10, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1024),// bufferSize
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setName("thread-executor");
					return thread;
				}
			}, new DiscardOldestPolicy());// 旧的自动放弃
	
	public static <F, T> void newThread(T arg, Function<? super T, F> function){
		executor.execute(new InnerThread<F, T>(arg, function));
	}
	
	static class InnerThread<F, T> extends Thread{
		final T argValue;
		final Function<? super T, ? extends F> function;
		public InnerThread(T arg, Function<? super T, ? extends F> function){
			this.argValue = arg;
			this.function = function;
		}
		
		@Override
		public void run(){
			this.function.apply(this.argValue);
		}
	}

}
