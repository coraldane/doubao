package com.liuyun.doubao.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.liuyun.doubao.chnl.Channel;
import com.liuyun.doubao.config.DoubaoConfig;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.utils.ConfigUtils;

public class Server {

	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	private static final ExtensionLoader<Channel> loader = ExtensionLoader.getExtensionLoader(Channel.class);
	
	private static volatile boolean running = true;
	
	public static void main(String[] args) {
		try {
			if(0 == args.length){
				System.out.println("please specify config file in arguments.");
				System.exit(1);
			}
			DoubaoConfig config = ConfigUtils.getConfig(args[0]);
			logger.info("read config file success:" + JSON.toJSONString(config));
			
			final Channel channel = loader.getExtension(loader.getDefaultExtensionName());
			channel.setConfig(config);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					channel.stop();
					logger.info(channel.getClass().getSimpleName() + " stoped.");
					
					synchronized (Server.class){
						running = false;
						Server.class.notify();
					}
				}
			});
			
			channel.start();
			logger.info(channel.getClass().getSimpleName() + " started.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
            System.exit(1);
		}
		
		synchronized (Server.class) {
            while (running) {
                try {
                	Server.class.wait();
                } catch (Throwable e) {
                }
            }
        }
	}

}
