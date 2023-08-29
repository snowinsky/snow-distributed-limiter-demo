package cn.snow.limiter.redis;

public interface RateLimiter {
    boolean goThroughLimiter();
}
