package com.liuyun.doubao.config.file;

import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.AbstractInputConfig;

@Identified(name="file")
public class FileInputConfig extends AbstractInputConfig {

	private String[] path;
	private String[] exclude;
	private String delimiter = "\n";
	
	private Map<String, String> tags = Maps.newConcurrentMap();
	private String type = null;
	
	//one of ["beginning", "end"]
	@JSONField(name="start_position")
	private String startPosition = "end";

	public String[] getPath() {
		return path;
	}

	public void setPath(String[] path) {
		this.path = path;
	}

	public String[] getExclude() {
		return exclude;
	}

	public void setExclude(String[] exclude) {
		this.exclude = exclude;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(String startPosition) {
		this.startPosition = startPosition;
	}
	
}
