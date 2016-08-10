package com.liuyun.doubao.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;

import com.google.common.base.Function;

/**
 * 
 * @Date: 2014年3月12日 下午10:07:34<br>
 * @Copyright (c) 2014 udai.com <br> * 
 * @since 1.0
 * @author coral
 */
public class ThreadUtils {
	
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 10, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1024),// bufferSize
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setName("sms-sender");
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

	public static void sleep(long ms){
		if(ms<0){
			return;
		}
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}
}
