package com.liuyun.doubao.config.codec;

import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.CodecConfig;

@Identified(name="plain")
public class PlainCodecConfig implements CodecConfig {

	private String charset = "UTF-8";

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
}
