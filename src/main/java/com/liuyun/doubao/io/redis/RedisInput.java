package com.liuyun.doubao.io.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.config.redis.RedisInputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Input;
import com.liuyun.doubao.processor.InputEventProcessor;
import com.liuyun.doubao.service.JedisService;

import redis.clients.jedis.ShardedJedis;

public class RedisInput extends InputEventProcessor implements Input {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private RedisInputConfig inputConfig = null;
	
	private JedisService jedisService;
	
	public JedisService getJedisService() {
		return jedisService;
	}
	
	@Override
	public void init(InputConfig inputConfig, Context context){
		super.setContext(context);
		if(inputConfig instanceof RedisInputConfig){
			this.inputConfig = (RedisInputConfig)inputConfig;
			
			this.jedisService = new JedisService(this.inputConfig);
			
			this.start();
		}
	}

	@Override
	public void destroy() {
		if(null != this.jedisService){
			this.jedisService.destroy();
		}
	}

	@Override
	public boolean doTask(Context context) throws Exception {
		long rowCount = 0;
		ShardedJedis jedis = this.jedisService.getJedis();
		
		for(int index=0; index < this.inputConfig.getBatchSize(); index++){
			try{
				String text = jedis.lpop(this.inputConfig.getKey());
				if(null == text){
					break;
				}
				JSONObject json = JSON.parseObject(text);
				super.write(json);
				rowCount ++;
			} catch (Exception e){
				logger.error("read from redis error", e);
			}
		}
		
		this.jedisService.returnRes(jedis);
		return rowCount > 0;
	}

}
