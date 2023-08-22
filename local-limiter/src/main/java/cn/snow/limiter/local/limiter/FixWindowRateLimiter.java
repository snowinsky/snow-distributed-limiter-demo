package cn.snow.limiter.local.limiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 固定时间窗口限流
 * 优点
 * 1. 实现简单
 * 缺点
 * 1. 因为时间窗口固定，所以没法避免数据扎堆在时间窗口夹缝处
 *
 * 为了解决固定时间窗口数据扎堆问题，请参见滑动窗口限流RollWindowRateLimiter
 */
@Slf4j
public class FixWindowRateLimiter implements RateLimiter {

    @Getter
    private final int maxReqCountPerFixWindowsMillis;
    @Getter
    private final long fixWindowMillis;

    public FixWindowRateLimiter(int maxReqCountPerFixWindowsMillis, long fixWindowMillis) {
        this.maxReqCountPerFixWindowsMillis = maxReqCountPerFixWindowsMillis == 0 ? 5 : maxReqCountPerFixWindowsMillis;
        this.fixWindowMillis = fixWindowMillis == 0 ? 1000 : fixWindowMillis;
        countPerFixWindowMillis = 0;
        previousFixWindowStartTime = 0;
    }

    private int countPerFixWindowMillis;
    private long previousFixWindowStartTime;


    @Override
    public synchronized boolean goThroughLimiter() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - previousFixWindowStartTime >= getFixWindowMillis()) {
            countPerFixWindowMillis = 0;
            previousFixWindowStartTime = currentTime;
        }
        if (countPerFixWindowMillis < getMaxReqCountPerFixWindowsMillis()) {
            countPerFixWindowMillis++;
            log.info("go through the limiter...");
            return true;
        }
        log.warn("over limit, the request was reject...");
        return false;
    }

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        FixWindowRateLimiter a = new FixWindowRateLimiter(2, 1000);
        //请求每100ms发一个，一秒钟发10个，如果没有限流，10个都通过。现在限流一秒钟限制1个，那么下面应该极限也就过2个
        for (int i = 0; i < 10; i++) {
            pool.execute(()->{
                a.goThroughLimiter();
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
