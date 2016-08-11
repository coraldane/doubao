package com.liuyun.doubao.chnl.internal;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.liuyun.doubao.chnl.Channel;
import com.liuyun.doubao.common.InitializingBean;
import com.liuyun.doubao.config.DoubaoConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.ctx.JsonEventFactory;
import com.liuyun.doubao.processor.ClosableProcessor;
import com.liuyun.doubao.processor.FilterProcessor;
import com.liuyun.doubao.processor.InputProcessor;
import com.liuyun.doubao.processor.OutputHolder;
import com.liuyun.doubao.stat.DataStatisticsRepository;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DefaultChannel implements Channel {
	// Specify the size of the ring buffer, must be power of 2.
	private static final int DEFAULT_RING_BUFFER_SIZE = 1024;
	
	private static final int DEFAULT_DATA_STAT_INTERVAL = 60;

	private Context context = null;

	private InputProcessor inputProcessor = null;
	private List<InitializingBean> processorList = Lists.newArrayList();
	
	private DataStatisticsRepository dataStatRepository = new DataStatisticsRepository();
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	@Override
	public void setConfig(DoubaoConfig config) {
		this.context = new Context(config, dataStatRepository);
	}

	@Override
	public void start() {
		this.executorService.scheduleWithFixedDelay(this.dataStatRepository, 10, DEFAULT_DATA_STAT_INTERVAL, TimeUnit.SECONDS);
		
		ClosableProcessor filterProcessor = new FilterProcessor();
		this.addHandler(filterProcessor, this.context);
		this.context.setFilterQueue(this.makeRingBuffer(new EventHandler[]{filterProcessor}));
		
		OutputHolder outputHolder = new OutputHolder();
		this.addHandler(outputHolder, this.context);
		this.context.setOutputQueue(this.makeRingBuffer(outputHolder.getOutputEventHandlers()));
		
		this.inputProcessor = new InputProcessor();
		this.inputProcessor.init(this.context);
		this.inputProcessor.start();
	}
	
	@Override
	public void stop() {
		this.inputProcessor.stop(false);
		this.context.stop();
		this.executorService.shutdown();
		
		this.inputProcessor.destroy(this.context);
		for(InitializingBean processor: this.processorList){
			processor.destroy(this.context);
		}
	}
	
	private void addHandler(InitializingBean handler, Context context){
		handler.init(context);
		this.processorList.add(handler);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RingBuffer<JsonEvent> makeRingBuffer(EventHandler[] handlers) {
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		JsonEventFactory factory = new JsonEventFactory();
		Disruptor<JsonEvent> disruptor = new Disruptor<JsonEvent>(factory, DEFAULT_RING_BUFFER_SIZE, threadFactory);
		disruptor.handleEventsWith(handlers);
		disruptor.start();

		return disruptor.getRingBuffer();
	}

}
