package cn.snow.limiter.redis.jedis;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.snow.limiter.redis.RateLimiter;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * 滑动窗口限流
 * 优点：精确度高，可扩展性强
 * 缺点：对于突发的大流量应付不来
 * <p>
 * 滑动窗口限流其实是在固定窗口限流的基础上做的优化，把固定的时间窗口变成了一堆的小窗口。让窗口的挪动不那么生硬。从而避免被瞬时高流量击穿。
 */
@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class SlidingWindowRateLimiterByLua implements RateLimiter {

    private final int maxReqCountPerFixWindowsMillis;
    private final long fixWindowMillis;
    private final Jedis jedis;


    @Override
    public synchronized boolean goThroughLimiter() {
        return executeByLua();
    }

    private boolean executeByLua() {
        long currentTime = System.currentTimeMillis();
        final String key = "slidingWindowJedisLua";
        List<String> keys = List.of(key);
        String zsetScore = currentTime + "";
        String slidingWindowStartScore = String.valueOf(currentTime - fixWindowMillis);
        String slidingWindowMillis = fixWindowMillis + "";
        String maxCount = maxReqCountPerFixWindowsMillis + "";
        List<String> args = List.of(zsetScore, slidingWindowStartScore, slidingWindowMillis, maxCount);
        Object ret = jedis.eval(getLuaForSlidingWindowRateLimiter(), keys, args);
        return ret != null && Integer.valueOf(ret.toString()) <= maxReqCountPerFixWindowsMillis;
    }

    private boolean executeByCommand() {
        long currentTime = System.currentTimeMillis();
        final String key = "slidingWindowJedisCommand";
        log.info("zrangeByScore={}", jedis.zrangeByScore(key, 0, currentTime - fixWindowMillis));
        jedis.zremrangeByScore(key, 0, currentTime - fixWindowMillis);
        final long cnt = jedis.zcard(key);
        if (cnt + 1 <= maxReqCountPerFixWindowsMillis) {
            long isZaddSuccess = jedis.zadd(key, currentTime, currentTime + "");
            if (cnt == 0 && isZaddSuccess == 1) {
                jedis.expire(key, fixWindowMillis);
            }
            return true;
        }
        return false;
    }

    private String getLuaForSlidingWindowRateLimiter() {
        return "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, tonumber(ARGV[2])) \n" +
                "local zsetSize = redis.call('ZCARD', KEYS[1])\n" +
                "if tonumber(zsetSize) <= tonumber(ARGV[4]) then \n" +
                "  redis.call('ZADD', KEYS[1], tonumber(ARGV[1]), ARGV[1]) \n" +
                "  redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3])) \n" +
                "end \n" +
                "return zsetSize;";
    }

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) {


        /*EvictingQueue queue = EvictingQueue.create(5);
        for (int i = 0; i < 7; i++) {
            queue.add(i);
            System.out.println(queue);
        }*/
        /*FixLengthQueueMap<Integer, String> mm = new FixLengthQueueMap<>(5);
        for (int i = 0; i < 7; i++) {
            mm.put(i, i + "");
            System.out.println(mm);
        }*/


        SlidingWindowRateLimiterByLua a = new SlidingWindowRateLimiterByLua(2, 1000, new JedisClientFactory().jedisClient());
        //请求每100ms发一个，一秒钟发10个，如果没有限流，10个都通过。现在限流一秒钟限制1个，那么下面应该极限也就过2个
        for (int i = 0; i < 60; i++) {
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

    @EqualsAndHashCode(callSuper = true)
    public static class FixLengthQueueMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 5667132395384549782L;

        private final int capacity;

        public FixLengthQueueMap(int capacity) {
            super(capacity + 1, 1.0f, true);
            this.capacity = capacity;
        }

        public K getFirstKey() {
            return new LinkedList<K>(keySet()).getFirst();
        }

        public K getLastKey() {
            return new LinkedList<K>(keySet()).getLast();
        }

        public void removeKey(K key) {
            keySet().removeIf(k -> k.equals(key));
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}
