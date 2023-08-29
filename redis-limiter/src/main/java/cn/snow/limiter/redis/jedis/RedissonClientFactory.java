package cn.snow.limiter.redis.jedis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonClientFactory {

    private final RedissonClient redissonClient = initRedissonClient();

    private RedissonClient initRedissonClient() {
        Config config = new Config();
        config.useSingleServer().setDatabase(1).setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }


    public RedissonClient redissonClient() {
        return redissonClient;
    }
}
