package com.liuyun.doubao.chnl.internal;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DefaultChannel implements Channel {
	// Specify the size of the ring buffer, must be power of 2.
	private static final int DEFAULT_RING_BUFFER_SIZE = 1024;

	private Context context = null;

	private InputProcessor inputEventHandler = null;
	private List<InitializingBean> handlerList = Lists.newArrayList();

	@Override
	public void setConfig(DoubaoConfig config) {
		this.context = new Context(config);
	}

	@Override
	public void start() {
		ClosableProcessor filterHandler = new FilterProcessor();
		this.addHandler(filterHandler, this.context);
		this.context.setFilterQueue(this.makeRingBuffer(new EventHandler[]{filterHandler}));
		
		OutputHolder outputHolder = new OutputHolder();
		this.addHandler(outputHolder, this.context);
		this.context.setOutputQueue(this.makeRingBuffer(outputHolder.getOutputEventHandlers()));
		
		this.inputEventHandler = new InputProcessor();
		this.inputEventHandler.init(this.context);
	}

	@Override
	public void stop() {
		this.inputEventHandler.stop(false);
		this.context.stop();
		
		this.inputEventHandler.destroy(this.context);
		for(InitializingBean handler: this.handlerList){
			handler.destroy(this.context);
		}
	}
	
	private void addHandler(InitializingBean handler, Context context){
		handler.init(context);
		this.handlerList.add(handler);
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
