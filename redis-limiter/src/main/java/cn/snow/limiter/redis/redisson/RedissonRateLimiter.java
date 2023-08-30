package cn.snow.limiter.redis.redisson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedissonRateLimiter {

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {

        RedissonClient rc = new RedissonClientFactory().redissonClient();

        RRateLimiter rateLimiter = rc.getRateLimiter("apiUri");
        rateLimiter.trySetRate(RateType.OVERALL, 2, 200, RateIntervalUnit.SECONDS);


        //请求每100ms发一个，一秒钟发10个，如果没有限流，10个都通过。现在限流一秒钟限制1个，那么下面应该极限也就过2个
        for (int i = 0; i < 15; i++) {
            pool.execute(() -> {
                log.info("{}", rateLimiter.tryAcquire());
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
    }
}
