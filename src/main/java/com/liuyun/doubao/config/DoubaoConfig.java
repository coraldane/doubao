package com.liuyun.doubao.config;

import java.util.List;

import com.google.common.collect.Lists;

public class DoubaoConfig {

	private InputConfig input;
	private List<OutputConfig> outputs = Lists.newArrayList();
	private List<List<FilterConfig>> filters = Lists.newArrayList();
	
	public InputConfig getInput() {
		return input;
	}
	public void setInput(InputConfig input) {
		this.input = input;
	}
	public List<OutputConfig> getOutputs() {
		return outputs;
	}
	public void setOutputs(List<OutputConfig> outputs) {
		this.outputs = outputs;
	}
	public List<List<FilterConfig>> getFilters() {
		return filters;
	}
	public void setFilters(List<List<FilterConfig>> filters) {
		this.filters = filters;
	}
	
}
