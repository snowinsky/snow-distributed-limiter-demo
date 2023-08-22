package cn.snow.limiter.local.limiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

/**
 * 令牌桶
 * 优点：
 */
@Slf4j
public class TokenBucketRateLimiter implements RateLimiter {
    /**
     * 桶容量
     */
    private final int capacity;     // 令牌桶容量
    /**
     * 令牌生成的速率
     */
    private final int ratePerSecond;
    private int currentTokenSize;
    private long previousTokenGenTime;

    /**
     * 构造函数中传入令牌桶的容量和令牌生成速率。
     *
     * @param capacity
     * @param ratePerSecond
     */
    public TokenBucketRateLimiter(int capacity, int ratePerSecond) {
        this.capacity = capacity;
        this.ratePerSecond = ratePerSecond;
        currentTokenSize = capacity;
        previousTokenGenTime = System.currentTimeMillis();
    }

    @Override
    public boolean goThroughLimiter() {
        long now = System.currentTimeMillis();
        if (now > previousTokenGenTime) {
            int generatedTokenSize = (int) ((now - previousTokenGenTime) * ratePerSecond / 1000);
            if (generatedTokenSize > 0) {
                currentTokenSize = Math.min(currentTokenSize + generatedTokenSize, capacity);
                previousTokenGenTime = now;
            }
        }

        if (currentTokenSize > 0) {
            currentTokenSize--;
            log.info("go through the limiter...");
            return true;
        } else {
            log.warn("over limit, reject...");
            return false;
        }
    }

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 10);
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
