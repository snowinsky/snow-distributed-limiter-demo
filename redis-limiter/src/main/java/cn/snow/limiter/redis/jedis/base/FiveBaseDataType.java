package cn.snow.limiter.redis.jedis.base;

import java.util.List;
import java.util.Map;

import cn.snow.limiter.redis.jedis.JedisClientFactory;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@SuppressWarnings("all")
@Slf4j
public class FiveBaseDataType {

    private static final Jedis jedis = new JedisClientFactory().jedisClient();

    /**
     *
     */
    public void testString() {
        log.info("string 一个一个的设置value");
        String setStringRet = jedis.set("key1", "value" + System.currentTimeMillis());
        //只有返回OK才表示设置key value成功
        log.info("string set result={}", setStringRet);
        String v = jedis.get("key1");
        log.info("string get result={}", v);
        log.info("\n");
        log.info("string 批量的设置key value");
        String mapSetRet = jedis.mset("key11", "value11_" + System.currentTimeMillis(), "key12", "value12_" + System.currentTimeMillis());
        log.info("map set return={}", mapSetRet);
        List<String> l = jedis.mget("key11", "key12");
        log.info("map get result={}", l);
    }

    /**
     *
     */
    public void testHash() {
        log.info("hash set return={}", jedis.hmset("person1", Map.of("id", "1", "name", "张三", "age", "11")));
        log.info("hash get by keyAndFields return={}", jedis.hmget("person1", "id", "name"));
    }

    /**
     *
     */
    public void testList() {
        //返回的是list的length
        log.info("list set return={}", jedis.lpush("province", "hebei"));
        log.info("list set return={}", jedis.lpush("province", "beijing"));
        log.info("list set return={}", jedis.rpush("province", "tianjin"));
        log.info("list get return={}", jedis.lindex("province", 0));
        log.info("list get return={}", jedis.lindex("province", 1));
        log.info("list get return={}", jedis.lindex("province", 2));
        log.info("list get return={}", jedis.lindex("province", 3));
    }

    /**
     *
     */
    public void testSet() {

    }

    /**
     *
     */
    public void testZSet() {

    }

    public static void main(String[] args) {
        FiveBaseDataType five = new FiveBaseDataType();
        five.testString();
        five.testHash();
        five.testList();
        five.testSet();
        five.testZSet();
    }
}
