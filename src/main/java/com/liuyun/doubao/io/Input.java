package com.liuyun.doubao.io;

import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.extension.SPI;

@SPI
public interface Input extends Closable, Stopable {
	
	void init(InputConfig inputConfig, Context context);
	
	void startup();
}
