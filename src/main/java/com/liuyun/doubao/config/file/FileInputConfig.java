package com.liuyun.doubao.config.file;

import java.util.Set;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Sets;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.AbstractInputConfig;
import com.liuyun.doubao.config.CodecConfig;
import com.liuyun.doubao.extension.ExtensionLoader;

@Identified(name="file")
public class FileInputConfig extends AbstractInputConfig {
	
	private static ExtensionLoader<CodecConfig> codecLoader = ExtensionLoader.getExtensionLoader(CodecConfig.class);

	private String[] path;
	private String[] exclude;
	private boolean recursive = false;
	
	private Set<String> tags = Sets.newHashSet();
	private String type = null;
	
	//one of ["beginning", "end"]
	@JSONField(name="start_position")
	private String startPosition = "end";
	
	private CodecConfig codec = codecLoader.getDefaultExtension();

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
	
	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
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

	public CodecConfig getCodec() {
		return codec;
	}

	public void setCodec(String codec) {
		this.codec = codecLoader.getExtension(codec);
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

}
