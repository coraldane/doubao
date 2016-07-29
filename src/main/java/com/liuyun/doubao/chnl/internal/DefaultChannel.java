package com.liuyun.doubao.chnl.internal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.collect.Lists;
import com.liuyun.doubao.chnl.Channel;
import com.liuyun.doubao.config.DoubaoConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.ctx.JsonEventFactory;
import com.liuyun.doubao.handler.ClosableEventHandler;
import com.liuyun.doubao.handler.FilterEventHandler;
import com.liuyun.doubao.handler.InputEventHandler;
import com.liuyun.doubao.handler.OutputEventHandler;
import com.liuyun.doubao.handler.StopableThread;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DefaultChannel implements Channel {
	// Specify the size of the ring buffer, must be power of 2.
	private static final int DEFAULT_RING_BUFFER_SIZE = 1024;

	private static final ExecutorService executor = Executors.newFixedThreadPool(3);

	private ThreadLocal<Context> context = new ThreadLocal<Context>();

	private StopableThread inputEventHandler = null;
	private List<ClosableEventHandler> handlerList = Lists.newArrayList();

	@Override
	public void setConfig(DoubaoConfig config) {
		Context ctx = new Context(config);
		this.context.set(ctx);
	}

	@Override
	public void start() {
		Context ctx = this.context.get();
		
		ClosableEventHandler filterHandler = new FilterEventHandler();
		this.addHandler(filterHandler, ctx);
		ctx.setFilterQueue(this.makeRingBuffer(filterHandler));
		
		ClosableEventHandler outputHandler = new OutputEventHandler();
		this.addHandler(outputHandler, ctx);
		ctx.setOutputQueue(this.makeRingBuffer(outputHandler));

		this.inputEventHandler = new InputEventHandler(ctx);
		this.inputEventHandler.init(this.context.get());
		executor.submit(this.inputEventHandler);
	}

	@Override
	public void stop() {
		Context ctx = this.context.get();
		this.inputEventHandler.stop(false);
		this.inputEventHandler.destroy(ctx);
		
		ctx.stop();
		for(ClosableEventHandler handler: this.handlerList){
			handler.destroy(ctx);
		}
	}
	
	private void addHandler(ClosableEventHandler handler, Context context){
		handler.init(context);
		this.handlerList.add(handler);
	}

	@SuppressWarnings("unchecked")
	private RingBuffer<JsonEvent> makeRingBuffer(EventHandler<JsonEvent> handler) {
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		JsonEventFactory factory = new JsonEventFactory();
		Disruptor<JsonEvent> disruptor = new Disruptor<JsonEvent>(factory, DEFAULT_RING_BUFFER_SIZE, threadFactory);
		disruptor.handleEventsWith(handler);
		disruptor.start();

		return disruptor.getRingBuffer();
	}

}
