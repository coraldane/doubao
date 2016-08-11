package com.liuyun.doubao.io.redis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.config.redis.RedisOutputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Output;
import com.liuyun.doubao.service.JedisService;

import redis.clients.jedis.ShardedJedis;

public class RedisOutput implements Output {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private JedisService jedisService;
	
	private Context context = null;
	private RedisOutputConfig outputConfig;
	
	@Override
	public void init(OutputConfig outputConfig, Context context) {
		this.context = context;
		if(outputConfig instanceof RedisOutputConfig){
			this.outputConfig = (RedisOutputConfig)outputConfig;
			
			this.jedisService = new JedisService(this.outputConfig);
		}
	}

	@Override
	public void destroy() {
		if(null != this.jedisService){
			this.jedisService.destroy();
		}
	}
	
	@Override
	public int write(List<JSONObject> dataArray) {
		ShardedJedis jedis = this.jedisService.getJedis();
		int success = 0;
		
		for(int index=0; index < dataArray.size(); index++){
			JSONObject data = dataArray.get(index);
			try{
				String strJson = data.toJSONString();
				jedis.rpush(this.outputConfig.getKey(), strJson);
				success ++;
				this.context.getDataStatRepository().incrementDataOutputPacked(1, strJson.getBytes().length);
			} catch (Exception e){
				logger.error("write into redis error", e);
			}
		}
		
		this.jedisService.returnRes(jedis);
		return success;
	}

	@Override
	public int writeCompressedData(String compressed) {
		ShardedJedis jedis = this.jedisService.getJedis();
		jedis.rpush(this.outputConfig.getKey(), compressed);
		this.context.getDataStatRepository().incrementDataOutputPacked(1, compressed.getBytes().length);
		this.jedisService.returnRes(jedis);
		return 0;
	}

}
