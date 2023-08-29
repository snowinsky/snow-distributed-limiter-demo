package cn.snow.limiter.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisClientFactory {

    private final JedisPool JEDIS_POOL = getPoolByConfig();

    private JedisPool getPoolByConfig(){
        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(50);
        jedisPoolConfig.setMaxIdle(10);
        return new JedisPool(jedisPoolConfig, "127.0.0.1", 6379);
    }

    public Jedis jedisClient(){
        return JEDIS_POOL.getResource();
    }
}
