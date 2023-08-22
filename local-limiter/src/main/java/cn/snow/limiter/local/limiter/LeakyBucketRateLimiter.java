package cn.snow.limiter.local.limiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

/**
 * 漏桶限流
 * 优点：不论请求大潮多么波涛汹涌，到这里就得涓涓细流
 * 缺点：即使系统闲的蛋疼了，也得涓涓细流的走
 */
@Slf4j
public class LeakyBucketRateLimiter implements RateLimiter {

    private final int reqBufferCapacity;
    private final int reqCountPerSecond;

    private int reqBufferCurrentSize;
    private long previousReqTime;

    public LeakyBucketRateLimiter(int reqBufferCapacity, int reqCountPerSecond) {
        this.reqBufferCapacity = reqBufferCapacity;
        this.reqCountPerSecond = reqCountPerSecond;
        reqBufferCurrentSize = 0;
        previousReqTime = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean goThroughLimiter() {
        return goThroughLimiter(1L);
    }

    private boolean goThroughLimiter(Long tryToConsumeReqCount) {
        long nowMillis = System.currentTimeMillis();
        //根据漏水速率计算出过去的这段时间，应该消费掉多少请求
        long expectedReqCount = (nowMillis - previousReqTime) * reqCountPerSecond / 1000;
        if (expectedReqCount > 0) {
            //计算出当前桶中的当前数量，就是原有量减去预计漏出量
            reqBufferCurrentSize = Math.max(reqBufferCurrentSize - (int) expectedReqCount, 0);
            previousReqTime = nowMillis;
        }
        //漏桶中当前水量加上待消费的只要不超过桶容量，就可以消费，否则就拒绝
        if (reqBufferCurrentSize + tryToConsumeReqCount <= reqBufferCapacity) {
            reqBufferCurrentSize += tryToConsumeReqCount;
            log.info("go through the limiter...");
            return true;
        } else {
            log.warn("over limit, reject ....");
            return false;
        }
    }

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(10, 10);
        for (int i = 0; i < 200; i++) {
            pool.execute(() -> {
                limiter.goThroughLimiter();
            });
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();

    }
}