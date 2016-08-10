package com.liuyun.doubao.input;

import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Stopable;
import com.liuyun.doubao.utils.SysUtils;

public abstract class AbstractStopableDataReader implements InputDataReader, Stopable {
	
	protected volatile boolean ready = true;
	protected volatile boolean waitForReading = true;
	protected volatile boolean readyForStop = true;
	
	protected Context context = null;
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	public void stop(boolean waitCompleted) {
		this.ready = false;
		if(false == waitCompleted){
			this.waitForReading = false;
		}
		
		while(!this.readyForStop){
			SysUtils.sleep(100);
		}
	}

	@Override
	public void notifyForRead() {
		this.ready = true;
	}

	public void setReadyForStop(boolean readyForStop) {
		this.readyForStop = readyForStop;
	}

}
