package cn.snow.limiter.local.limiter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 滑动窗口限流
 * 优点：精确度高，可扩展性强
 * 缺点：对于突发的大流量应付不来
 */
@Slf4j
public class RollWindowRateLimiter implements RateLimiter {

    @Getter
    private final int maxReqCountPerFixWindowsMillis;
    @Getter
    private final long fixWindowMillis;
    @Getter
    private final int subFixWindowMillis;
    @Getter
    private final FixLengthQueueMap<Long, AtomicInteger> subWindowCounter;

    public RollWindowRateLimiter(int maxReqCountPerFixWindowsMillis, long fixWindowMillis) {
        this.maxReqCountPerFixWindowsMillis = maxReqCountPerFixWindowsMillis == 0 ? 5 : maxReqCountPerFixWindowsMillis;
        this.fixWindowMillis = fixWindowMillis == 0 ? 1000 : fixWindowMillis;
        subFixWindowMillis = 500;
        subWindowCounter = new FixLengthQueueMap<>((int) Math.ceil((double) fixWindowMillis / subFixWindowMillis));
    }


    @Override
    public synchronized boolean goThroughLimiter() {
        Long subWindowKey = getSubWindowKey(LocalDateTime.now());
        int totalSubFixWindowCount = sumTotalSubFixWindowCount(subWindowKey);
        if (totalSubFixWindowCount >= getMaxReqCountPerFixWindowsMillis()) {
            log.info("over limit, reject ...");
            return false;
        } else {
            getSubWindowCounter().get(subWindowKey).incrementAndGet();
            log.info("pass the limiter...");
            return true;
        }

    }

    private Long getSubWindowKey(LocalDateTime now) {
        return null;
    }

    private int sumTotalSubFixWindowCount(long subWindowKey) {
        AtomicInteger count = new AtomicInteger(0);
        subWindowCounter.entrySet().forEach(a -> {
            if (a != null && a.getKey() <= subWindowKey) {
                count.addAndGet(a.getValue().get());
            }
        });
        return count.get();
    }

    public static void main(String[] args) {
        FixLengthQueueMap<Integer, String> mm = new FixLengthQueueMap<>(5);
        for (int i = 0; i < 7; i++) {
            mm.put(i, i + "");
            System.out.println(mm);
        }
    }

    public static class FixLengthQueueMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 5667132395384549782L;

        private final int capacity;

        public FixLengthQueueMap(int capacity) {
            super(capacity + 1, 1.0f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}
