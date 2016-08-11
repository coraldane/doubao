package com.liuyun.doubao.stat;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据流量统计服务
* @Description: 
* @version: v1.0.0
* @author: coral
* @date: Aug 11, 2016 2:08:57 PM
* @copyright liutian
* Modification History:
* Date         Author          Version            Description
*---------------------------------------------------------*
* Aug 11, 2016      coral          v1.0.0
 */
public class DataStatisticsRepository implements Runnable {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Lock lock = new ReentrantLock();
	
	private AtomicLong readDataCount = new AtomicLong();
	private AtomicLong readDataBytes = new AtomicLong();
	private AtomicLong outputDataCountOrigin = new AtomicLong();
	private AtomicLong outputDataBytesOrigin = new AtomicLong();
	private AtomicLong outputDataCountPacked = new AtomicLong();
	private AtomicLong outputDataBytesPacked = new AtomicLong();
	
	public void incrementDataRead(long rows, long bytes){
		this.readDataCount.getAndSet(this.readDataCount.get() + rows);
		this.readDataBytes.getAndSet(this.readDataBytes.get() + bytes);
	}
	
	public void incrementDataOutputOrigin(long rows, long bytes){
		this.outputDataCountOrigin.getAndSet(this.outputDataCountOrigin.get() + rows);
		this.outputDataBytesOrigin.getAndSet(this.outputDataBytesOrigin.get() + bytes);
	}
	
	public void incrementDataOutputPacked(long rows, long bytes){
		this.outputDataCountPacked.getAndSet(this.outputDataCountPacked.get() + rows);
		this.outputDataBytesPacked.getAndSet(this.outputDataBytesPacked.get() + bytes);
	}
	
	@Override
	public void run(){
		lock.lock();
		logger.info("read_count:" + this.readDataCount + ",read_bytes:" + this.readDataBytes 
				+ ",output_count_origin:" + this.outputDataCountOrigin + ",output_bytes_origin:" + this.outputDataBytesOrigin 
				+ ",output_count_packed:" + this.outputDataCountPacked + ",output_bytes_packed:" + this.outputDataBytesPacked);
		this.readDataCount.set(0);
		this.readDataBytes.set(0);
		this.outputDataCountOrigin.set(0);
		this.outputDataBytesOrigin.set(0);
		this.outputDataCountPacked.set(0);
		this.outputDataBytesPacked.set(0);
		lock.unlock();
	}
	
}
