# snow-distributed-limiter-demo

> 常用限流方式：

- 计数器(固定窗口)：每秒钟允许访问多少次
- 滑动窗口：一分钟内最多允许发送十个文件
- 漏桶：一秒钟只允许两个请求进入服务
- 令牌桶：一秒钟一个请求，一分钟产生60个令牌。可以一次用完，也可以一秒钟一个。

## redis基础数据类型的基本命令用法
> redis就是一个存储key value的文件，每一个database就是一个文件。文件里只能存字符串。
> 
> key/value 都是byte[] 字符串是byte[]的一个比较简单的展示
> 

### 通用命令，可以针对任何的数据类型
- copy 把一个key的value复制给另一个key
- del 删除一个或者多个key
- dump 把key的value在redis中的存储直接展示出来
- exists 判断key是否存在
- expire 设定过期时间，单位是秒
- expireat 设定具体的unix时间戳为过期时间
- expiretime 返回unix时间戳
- keys 返回所有的符合正则的key
- migrate 把一个redis中的keys搬家到另一个redis中
- move 把某个key从一个database搬到另一个database
- object encoding 返回key的encoding
- object freq 返回key的frequency
- object idletime 返回key的空闲时间
- object refcount 返回key的访问次数
- persist 删除key的过期时间
- pexpire 设置毫秒计过期时间
- pexpireat 设置unix毫秒计时间戳
- pexpiretime 返回unix毫秒计时间戳
- pttl 返回毫秒级过期时间
- randomkey 随机返回一个key
- rename 
- renamenx
- restore
- scan
- sort
- sort_ro
- rouch
- ttl
- type
- unlink
- wait
- waitaof



### string(value=string)
- append key不存在则插入，key存在就把内容拼到value的后面， 写
- desr key不存在就插入0，存在就增加1， 写
- decrby key不存在插入0，存在就增加n， 写
- get 获取value， 读
- getdel 删除key并返回对应的value
- getex 设定expire time之后返回value
- getrange 返回value的一部分
- getset 设定value为新值并返回旧值
- incr key不存在就插入0， 存在就增加1
- incrby key不存在就插入0，存在就增加n
- incrbyfloat key不存在就插入0，存在就增加m.n
- lcs 多个key对应的value求交集
- mget 批量获取value值
- mset 批量设定value值
- msetnx 批量设定final的value值
- psetex 同时设定value+expiretime(单位必须是毫秒)
- set 设定value值
- setnx 只有在key不存在的情况下才设定value， value是final的
- setex 同时设定设定value+expiretime(单位必须是秒)
- setrange 替换，用一个字符串去替换value中从某一位置开始之后的字符串
- strlen 返回value的length
- substr 返回value的其中一部分，被getrange(更容易看明白，这只是读，不是写)替代了
### hash(value=hash map<field, fieldValue>)
- hdel 删除fields和fieldValue，没field了就删除hash map
- hexists 判断field是否存在
- hget 获取fieldValue
- hgetall 返回整个hash map
- hincrby hash map中field的value增1
- hincrbyfloat hash map中field的value增m.n
- hkeys 返回hash map的keyset
- hlength 返回hash map的size
- hmget 批量返回field和fieldvalue
- hmset 批量设置field和fieldValue
- hrandfield 随机返回某些key
- hscan 返回一个游标，可以便利所有的fields
- hset 更新field的值
- hsetnx 如果field不存在，新增这个值
- hstrlen 返回field的value的长度
- hvals 返回hash map的valueList
### list(value=list<element>)
- blmove blocking list move把src list拼到des list的左边
- blmpop blocking list multiple pop，同时pop出多个list的最左边的元素
- blpop blocking list pop出一个list的最左边的元素
- brpop blocking right pop出一个list的最右边的元素
- brpoplpush blocking right pop出一个list的最右边的元素，放入另一个list的最左边，类似于一个管子对接另一个管子
- lindex list 返回某一个index对应的element
- linsert list 插入一个element到list中
- llen list 返回list的size
- lmove list 把src list拼到des list的左边
- lmpop list 从左边pop出多个element从一个list中
- lpop list 从左边pop出一个element从一个list中
- lpos list position 返回第一个匹配的element的index
- lpush list 从左边插入list，list不存在就新建
- lpushx list 从左边插入list，list不存在也不新建
- lrange list 返回list中的一部分元素
- lrem list remove 删除list中的部分元素
- lset list 插入element到某一index
- ltrim list 将trim锁定的element保留，其他的都删除
- rpop right 从右边pop出一个
- rpoplpush right 右边pop左边放入，类似于一个管子对接另一个管子
- rpush right 从右边新增一个元素
- rpushx right 从右边新增一个元素，key不存在也不新增
### set(value=set<member>)
- sadd 新增一个memeber到set中
- scard 返回set的length
- sdiff 返回第一个set中的member(这些member在其他的set中都没出现过) 差集
- sdiffstore 只是把sdiff的结果写入另一个set中
- sinter 返回多个set的交集
- sintercard 返回多个set的交集的member的个数
- sinterstore 将sinter的结果存入另一个set中
- sismember 判断一个member是否在set中
- smembers 返回所有的member
- smismember 批量判断是不是set中的member
- smove 把一个member从一个set挪到另一个set
- spop 随机pop一个member
- srandmember 获取set中随机的几个member
- srem 删除一个或者多个member
- sscan 遍历所有的member
- sunion 求多个set的并集
- sunionstore sunion的结果存入一个新set
### sorted set(value=set<member order by score> )
- bzmpop 批量pop出多个member
- bzpopmax pop出score最高的member
- bzpopmin pop出score最低的member
- zadd 加一个member到zset中，指定score
- zcard 返回zset的length
- zcount 返回zset中member的个数（score在指定区间内的）
- zdiff 差集
- zdiffstore 差集存储
- zincrby 某一个member的score加一
- zinter 交集
- zintercard 交集size
- zinterstore 交集存储
- zlexcount score相同的某一个指定range的element的数量
- zmpop 批量pop出多个member
- zmscore 批量pop出多个score的member
- zpopmax pop出score最高的member
- zpopmin pop出score最低的member
- zrandmember 随机member
- zrange 返回一定range by index
- zrangestore 返回一定range内的member
- zrangebylex 返回一定range内的lex
- zrangebyscore 返回一定range内的score
- zrank 返回排名
- zrem 删除member
- zremrangebylex 删除member by lex
- zremrangebyrank 删除 by rank
- zremrangebyscore 删除 by score
- zrevrange 倒序
- zrevrangebylex 倒序
- zrevrangebyscore 倒序
- zrevrank 倒序
- zscan 遍历
- zscore 返回score某一个member
- zunion 交集
- zunionstore 交集存储


