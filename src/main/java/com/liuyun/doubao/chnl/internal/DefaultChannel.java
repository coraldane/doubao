package com.liuyun.doubao.chnl.internal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.Lists;
import com.liuyun.doubao.chnl.Channel;
import com.liuyun.doubao.config.DoubaoConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.task.FilterTask;
import com.liuyun.doubao.task.InputTask;
import com.liuyun.doubao.task.OutputTask;
import com.liuyun.doubao.task.TaskAdapter;

public class DefaultChannel implements Channel {
	private static final ExecutorService executor = Executors.newFixedThreadPool(3);
	
	private ThreadLocal<Context> context = new ThreadLocal<Context>();
	
	private List<TaskAdapter> taskList = Lists.newArrayList();
	
	private void addTask(TaskAdapter task){
		task.init(this.context.get());
		this.taskList.add(task);
	}
	
	@Override
	public void setConfig(DoubaoConfig config){
		Context ctx = new Context(config);
		this.context.set(ctx);
	}

	@Override
	public void start() {
		TaskAdapter inputTask = new InputTask(this.context.get());
		addTask(inputTask);
		executor.submit(inputTask);
		
		TaskAdapter filterTask = new FilterTask(this.context.get());
		addTask(filterTask);
		executor.submit(filterTask);
		
		TaskAdapter outputTask = new OutputTask(this.context.get());
		addTask(outputTask);
		executor.submit(outputTask);
	}

	@Override
	public void stop() {
		for(int index=0; index < this.taskList.size(); index++){
			TaskAdapter task = this.taskList.get(index);
			task.stop(0 == index?false:true);
		}
		for(TaskAdapter task: this.taskList){
			task.destroy(this.context.get());
		}
	}

}
