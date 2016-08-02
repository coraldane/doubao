package com.liuyun.doubao.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liuyun.doubao.config.redis.RedisConfig;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
* Jedis连接池管理类
* @Date: 2014年2月20日 下午10:00:00<br>
* @Copyright (c) 2014 Vobile <br> * 
* @since 1.0
* @author coral
*/
public class JedisService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String KEY_SPLITTER = "^";
	
	private static final int REDIS_TIMEOUT = 3000;
	
	private ShardedJedisPool pool;
	
	public JedisService(RedisConfig redisConfig) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(20);
		config.setMaxIdle(10);
		config.setTestOnBorrow(true);
		config.setMaxWaitMillis(10001L);
		
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		try {
			if(StringUtils.isNotBlank(redisConfig.getHost())){
				JedisShardInfo jsi = new JedisShardInfo(redisConfig.getHost(), redisConfig.getPort());
				jsi.setPassword(redisConfig.getPasswd());
				shards.add(jsi);
			}
			
			pool = new ShardedJedisPool(config, shards);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ShardedJedis getJedis(){
		ShardedJedis jedis = null;
		boolean enabled = true;
		while(enabled){
			try{
				jedis = pool.getResource();
				enabled = false;
			} catch(Exception e){
				logger.info("The redis connection is not successful,wait " + REDIS_TIMEOUT + "ms try again.");
				try {
					wait(REDIS_TIMEOUT);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		return jedis;
	}
	
	public void returnRes(ShardedJedis jedis){
		jedis.close();
	}
	
	public void destroy(){
		pool.destroy();
	}
	
	public long llen(String key){
		ShardedJedis jedis = this.getJedis();
		if(!jedis.exists(key)){
			return 0;
		}
		long len = jedis.llen(key);
		this.returnRes(jedis);
		return len;
	}
	
	public String hget(String key, String field){
		ShardedJedis jedis = this.getJedis();
		String retValue = jedis.hget(key, field);
		this.returnRes(jedis);
		return retValue;
	}
	
	public Long hset(String key, String field, String value){
		ShardedJedis jedis = this.getJedis();
		Long retValue = jedis.hset(key, field, value);
		this.returnRes(jedis);
		return retValue;
	}

}
