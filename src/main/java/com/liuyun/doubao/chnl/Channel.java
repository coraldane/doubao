package com.liuyun.doubao.chnl;

import com.liuyun.doubao.config.DoubaoConfig;
import com.liuyun.doubao.extension.SPI;

@SPI("default")
public interface Channel {
	
	void setConfig(DoubaoConfig config);
    
    /**
     * start.
     */
    void start();
    
    /**
     * stop.
     */
    void stop();

}