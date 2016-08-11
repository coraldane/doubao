package com.liuyun.doubao.processor;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.io.Compression;
import com.liuyun.doubao.io.Output;
import com.liuyun.doubao.utils.StringUtils;
import com.liuyun.doubao.utils.SysUtils;
import com.lmax.disruptor.EventHandler;

public class OutputProcessor implements EventHandler<JsonEvent>, Runnable {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final long FLUSH_DATA_INTERVAL = 1000;
	
	private Output output = null;
	private OutputConfig outputConfig = null;
	
	private Compression compression = null;
	private int batchSize = 100;
	private long lastWriteTime = System.currentTimeMillis();
	
	private List<JSONObject> dataBuffer = Lists.newArrayList();
	
	public OutputProcessor(Output output, OutputConfig outputConfig){
		this.output = output;
		this.outputConfig = outputConfig;
		this.batchSize = outputConfig.getBatch_size();
		
		this.compression = Context.getCompression(this.outputConfig);
	}
	
	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}

	@Override
	public void onEvent(JsonEvent event, long sequence, boolean endOfBatch) throws Exception {
		JSONObject json = event.get();
		if(null == json || json.isEmpty()){
			return;
		}
		
		while(this.dataBuffer.size() >= this.batchSize){
			SysUtils.sleep(100);
		}
		this.dataBuffer.add(json);
	}
	
	@Override
	public void run(){
		while(true){
			if(this.dataBuffer.size() >= this.batchSize || lastWriteTime + FLUSH_DATA_INTERVAL < System.currentTimeMillis()){
				if(CollectionUtils.isNotEmpty(this.dataBuffer)){
					this.flushData(this.dataBuffer);
					this.dataBuffer.clear();
				}
				
				lastWriteTime = System.currentTimeMillis();
			} else {
				SysUtils.sleep(100);
			}
		}
	}
	
	private void flushData(List<JSONObject> dataList){
		if(null == this.compression){
			this.output.write(dataList);
			return;
		}
		
		String source = null;
		try {
			source = StringUtils.toReflectString(dataList);
			String compressed = this.compression.compress(source);
			this.output.writeCompressedData(compressed);
		} catch(Exception e){
			logger.error("compress data error, source: " + source, e);
		}
	}
	
}