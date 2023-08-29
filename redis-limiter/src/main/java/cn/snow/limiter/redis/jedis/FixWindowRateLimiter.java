package cn.snow.limiter.redis.jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.snow.limiter.redis.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * 固定时间窗口限流
 * 优点
 * 1. 实现简单
 * 缺点
 * 1. 因为时间窗口固定，所以没法避免数据扎堆在时间窗口夹缝处，很容易因为瞬间的高流量而击穿
 * <p>
 * 为了解决固定时间窗口数据扎堆问题，请参见滑动窗口限流RollWindowRateLimiter
 */
@Slf4j
@RequiredArgsConstructor
public class FixWindowRateLimiter implements RateLimiter {

    private final int maxReqCountPerFixWindowsMillis;
    private final long fixWindowMillis;
    private final Jedis jedis;


    @Override
    public synchronized boolean goThroughLimiter() {
        final String apiUrl = "apiUrl";
        if (jedis.exists(apiUrl)) {
            jedis.incr(apiUrl);
            return Long.parseLong(jedis.get(apiUrl)) <= maxReqCountPerFixWindowsMillis;
        } else {
            String ret = jedis.set(apiUrl, "0", new SetParams().nx().px(fixWindowMillis));
            return ret != null && ret.equals("OK");
        }
    }

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {

        FixWindowRateLimiter a = new FixWindowRateLimiter(2, 1000, new JedisClientFactory().jedisClient());
        //请求每100ms发一个，一秒钟发10个，如果没有限流，10个都通过。现在限流一秒钟限制1个，那么下面应该极限也就过2个
        for (int i = 0; i < 13; i++) {
            pool.execute(() -> {
                log.info("{}", a.goThroughLimiter());
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
