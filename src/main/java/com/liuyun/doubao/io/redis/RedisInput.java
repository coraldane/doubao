package com.liuyun.doubao.io.redis;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.config.redis.RedisInputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Compression;
import com.liuyun.doubao.io.Input;
import com.liuyun.doubao.processor.StopableThread;
import com.liuyun.doubao.service.JedisService;

import redis.clients.jedis.ShardedJedis;

public class RedisInput extends StopableThread implements Input {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private RedisInputConfig inputConfig = null;
	
	private Compression compression = null;
	private JedisService jedisService;
	
	public JedisService getJedisService() {
		return jedisService;
	}
	
	@Override
	public void init(InputConfig inputConfig, Context context){
		this.setContext(context);
		this.compression = Context.getCompression(inputConfig);
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
	
	@Override
	public boolean doTask(Context context) throws Exception {
		ShardedJedis jedis = this.jedisService.getJedis();
		long rowCount = this.readData(jedis);
		this.jedisService.returnRes(jedis);
		return rowCount > 0;
	}
	
	private long readData(ShardedJedis jedis){
		long rowCount = 0;
		if(null == this.compression){
			for(int index=0; index < this.inputConfig.getBatch_size(); index++){
				try{
					String text = jedis.lpop(this.inputConfig.getKey());
					if(null == text){
						break;
					}
					this.context.getDataStatRepository().incrementDataRead(1, text.getBytes().length);
					
					JSONObject json = JSON.parseObject(text);
					context.readData2Queue(json);
					rowCount ++;
				} catch (Exception e){
					logger.error("read from redis error", e);
				}
			}
		} else {
			try{
				String text = jedis.lpop(this.inputConfig.getKey());
				if(StringUtils.isBlank(text)){
					return 0;
				}
				String source = this.compression.uncompress(text, "UTF-8");
				List<String> strLineList = Splitter.on("\n").splitToList(source);
				if(CollectionUtils.isEmpty(strLineList)){
					return 0;
				}
				for(String strLine: strLineList){
					JSONObject json = JSON.parseObject(strLine);
					context.readData2Queue(json);
					rowCount ++;
				}
				
				this.context.getDataStatRepository().incrementDataRead(rowCount, text.getBytes().length);
			} catch (Exception e){
				logger.error("read from redis error", e);
			}
			
		}
		return rowCount;
	}

	@Override
	public void startup(){
		this.start();
	}

}
