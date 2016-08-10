package com.liuyun.doubao.input;

import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Closable;
import com.liuyun.doubao.io.Stopable;
import com.liuyun.doubao.utils.SysUtils;

public abstract class AbstractStopableDataReader implements InputDataReader, Closable, Stopable {
	
	protected volatile boolean ready = true;
	protected volatile boolean stopImmediately = false;
	protected volatile boolean readyForStop = true;
	
	protected Context context = null;
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	public void stop(boolean waitCompleted) {
		this.ready = false;
		if(waitCompleted){
			while(!this.readyForStop){
				SysUtils.sleep(100);
			}
		} else {
			this.stopImmediately = true;
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
