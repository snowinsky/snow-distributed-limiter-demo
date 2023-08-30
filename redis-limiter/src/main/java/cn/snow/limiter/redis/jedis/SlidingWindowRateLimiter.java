package cn.snow.limiter.redis.jedis;

import java.util.LinkedHashMap;
import java.util.LinkedList;
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
public class SlidingWindowRateLimiter implements RateLimiter {

    private final int maxReqCountPerFixWindowsMillis;
    private final long fixWindowMillis;
    private final Jedis jedis;


    @Override
    public synchronized boolean goThroughLimiter() {
        long currentTime = System.currentTimeMillis();
        final String apiUrl = "rollWindowJedis";
        long countRangeByScore = jedis.zcount(apiUrl, currentTime - fixWindowMillis, currentTime);
        if (countRangeByScore + 1 <= maxReqCountPerFixWindowsMillis) {
            long isZadd = jedis.zadd(apiUrl, currentTime, currentTime + "");
            if (countRangeByScore == 0 && isZadd == 1) {
                jedis.expire(apiUrl, fixWindowMillis / 1000);
            }
            return true;
        }
        return false;
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


        SlidingWindowRateLimiter a = new SlidingWindowRateLimiter(2, 1000, new JedisClientFactory().jedisClient());
        //请求每100ms发一个，一秒钟发10个，如果没有限流，10个都通过。现在限流一秒钟限制1个，那么下面应该极限也就过2个
        for (int i = 0; i < 30; i++) {
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
