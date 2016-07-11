package com.liuyun.doubao.config;

import com.liuyun.doubao.extension.SPI;

@SPI
public interface OutputConfig extends BaseConfig {

	int getBatchSize();
	
}
