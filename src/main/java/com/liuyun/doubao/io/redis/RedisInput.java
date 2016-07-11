package com.liuyun.doubao.io.redis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.config.redis.RedisInputConfig;
import com.liuyun.doubao.io.Input;
import com.liuyun.doubao.service.JedisService;

import redis.clients.jedis.ShardedJedis;

public class RedisInput implements Input {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private RedisInputConfig inputConfig = null;

	private JedisService jedisService;
	
	@Override
	public List<JSONObject> read() {
		List<JSONObject> retList = Lists.newArrayList();
		ShardedJedis jedis = this.jedisService.getJedis();
		
		for(int index=0; index < this.inputConfig.getBatchSize(); index++){
			try{
				String text = jedis.lpop(this.inputConfig.getKey());
				if(null == text){
					break;
				}
				retList.add(JSON.parseObject(text));
			} catch (Exception e){
				logger.error("read from redis error", e);
			}
		}
		
		this.jedisService.returnRes(jedis);
		return retList;
	}

	public JedisService getJedisService() {
		return jedisService;
	}
	
	@Override
	public void init(InputConfig inputConfig){
		if(inputConfig instanceof RedisInputConfig){
			this.inputConfig = (RedisInputConfig)inputConfig;
			
			this.jedisService = new JedisService(this.inputConfig);
		}
	}

	@Override
	public void destroy() {
		if(null != this.jedisService){
			this.jedisService.destroy();
		}
	}

}
