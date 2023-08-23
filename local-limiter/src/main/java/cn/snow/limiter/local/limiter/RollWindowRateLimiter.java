package cn.snow.limiter.local.limiter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 滑动窗口限流
 * 优点：精确度高，可扩展性强
 * 缺点：对于突发的大流量应付不来
 * <p>
 * 滑动窗口限流其实是在固定窗口限流的基础上做的优化，把固定的时间窗口变成了一堆的小窗口。让窗口的挪动不那么生硬。从而避免被瞬时高流量击穿。
 */
@Slf4j
@SuppressWarnings("all")
public class RollWindowRateLimiter implements RateLimiter {

    @Getter
    private final int maxReqCountPerFixWindowsMillis;
    @Getter
    private final long fixWindowMillis;
    @Getter
    private final int subFixWindowMillis;
    @Getter
    private final FixLengthQueueMap<Long, AtomicInteger> subWindowCounter;

    private long previousReqTime;

    public RollWindowRateLimiter(int maxReqCountPerFixWindowsMillis, long fixWindowMillis) {
        this.maxReqCountPerFixWindowsMillis = maxReqCountPerFixWindowsMillis == 0 ? 5 : maxReqCountPerFixWindowsMillis;
        this.fixWindowMillis = fixWindowMillis == 0 ? 1000 : fixWindowMillis;
        subFixWindowMillis = 500;
        previousReqTime = System.currentTimeMillis();
        subWindowCounter = new FixLengthQueueMap<>((int) Math.ceil((double) fixWindowMillis / subFixWindowMillis));
    }


    @Override
    public synchronized boolean goThroughLimiter() {
        long currentTime = System.currentTimeMillis();
        expiredSubWindowCounter(currentTime);
        if (subWindowCounter.isEmpty() || currentTime - previousReqTime >= getSubFixWindowMillis()) {
            subWindowCounter.put(currentTime, new AtomicInteger(0));
            previousReqTime = currentTime;
        }
        if (sumTotalSubFixWindowCount() >= getMaxReqCountPerFixWindowsMillis()) {
            log.warn("over limit, reject..{}..\n", subWindowCounter);
            return false;
        }
        subWindowCounter.get(subWindowCounter.getLastKey()).incrementAndGet();
        log.info("pass the limit..{}.\n", subWindowCounter);
        return true;

    }

    private void expiredSubWindowCounter(long currentTime) {
        final long s = currentTime - getFixWindowMillis();
        Set<Long> sl = subWindowCounter.keySet().stream().filter(a-> a < s).collect(Collectors.toSet());
        sl.forEach(subWindowCounter::removeKey);
    }

    private int sumTotalSubFixWindowCount() {
        AtomicInteger count = new AtomicInteger(0);
        subWindowCounter.entrySet().forEach(a -> count.addAndGet(a.getValue().get()));
        return count.get();
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


        RollWindowRateLimiter a = new RollWindowRateLimiter(2, 1000);
        //请求每100ms发一个，一秒钟发10个，如果没有限流，10个都通过。现在限流一秒钟限制1个，那么下面应该极限也就过2个
        for (int i = 0; i < 15; i++) {
            pool.execute(() -> {
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

        public void removeKey(K key){
            keySet().removeIf(k -> k.equals(key));
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}
