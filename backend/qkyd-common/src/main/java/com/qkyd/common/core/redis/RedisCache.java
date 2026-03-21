package com.qkyd.common.core.redis;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * spring redis 宸ュ叿绫?
 *
 * @author qkyd
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缂撳瓨鍩烘湰鐨勫璞★紝Integer銆丼tring銆佸疄浣撶被绛?
     *
     * @param key 缂撳瓨鐨勯敭鍊?
     * @param value 缂撳瓨鐨勫€?
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缂撳瓨鍩烘湰鐨勫璞★紝Integer銆丼tring銆佸疄浣撶被绛?
     *
     * @param key 缂撳瓨鐨勯敭鍊?
     * @param value 缂撳瓨鐨勫€?
     * @param timeout 鏃堕棿
     * @param timeUnit 鏃堕棿棰楃矑搴?
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 璁剧疆鏈夋晥鏃堕棿
     *
     * @param key Redis閿?
     * @param timeout 瓒呮椂鏃堕棿
     * @return true=璁剧疆鎴愬姛锛沠alse=璁剧疆澶辫触
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 璁剧疆鏈夋晥鏃堕棿
     *
     * @param key Redis閿?
     * @param timeout 瓒呮椂鏃堕棿
     * @param unit 鏃堕棿鍗曚綅
     * @return true=璁剧疆鎴愬姛锛沠alse=璁剧疆澶辫触
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 鑾峰彇鏈夋晥鏃堕棿
     *
     * @param key Redis閿?
     * @return 鏈夋晥鏃堕棿
     */
    public long getExpire(final String key)
    {
        return redisTemplate.getExpire(key);
    }

    /**
     * 鍒ゆ柇 key鏄惁瀛樺湪
     *
     * @param key 閿?
     * @return true 瀛樺湪 false涓嶅瓨鍦?
     */
    public Boolean hasKey(String key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     * 鑾峰緱缂撳瓨鐨勫熀鏈璞°€?
     *
     * @param key 缂撳瓨閿€?
     * @return 缂撳瓨閿€煎搴旂殑鏁版嵁
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 鍒犻櫎鍗曚釜瀵硅薄
     *
     * @param key
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * 鍒犻櫎闆嗗悎瀵硅薄
     *
     * @param collection 澶氫釜瀵硅薄
     * @return
     */
    public boolean deleteObject(final Collection collection)
    {
        return redisTemplate.delete(collection) > 0;
    }

    /**
     * 缂撳瓨List鏁版嵁
     *
     * @param key 缂撳瓨鐨勯敭鍊?
     * @param dataList 寰呯紦瀛樼殑List鏁版嵁
     * @return 缂撳瓨鐨勫璞?
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 鑾峰緱缂撳瓨鐨刲ist瀵硅薄
     *
     * @param key 缂撳瓨鐨勯敭鍊?
     * @return 缂撳瓨閿€煎搴旂殑鏁版嵁
     */
    public <T> List<T> getCacheList(final String key)
    {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缂撳瓨Set
     *
     * @param key 缂撳瓨閿€?
     * @param dataSet 缂撳瓨鐨勬暟鎹?
     * @return 缂撳瓨鏁版嵁鐨勫璞?
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 鑾峰緱缂撳瓨鐨剆et
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缂撳瓨Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 鑾峰緱缂撳瓨鐨凪ap
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 寰€Hash涓瓨鍏ユ暟鎹?
     *
     * @param key Redis閿?
     * @param hKey Hash閿?
     * @param value 鍊?
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 鑾峰彇Hash涓殑鏁版嵁
     *
     * @param key Redis閿?
     * @param hKey Hash閿?
     * @return Hash涓殑瀵硅薄
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 鑾峰彇澶氫釜Hash涓殑鏁版嵁
     *
     * @param key Redis閿?
     * @param hKeys Hash閿泦鍚?
     * @return Hash瀵硅薄闆嗗悎
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 鍒犻櫎Hash涓殑鏌愭潯鏁版嵁
     *
     * @param key Redis閿?
     * @param hKey Hash閿?
     * @return 鏄惁鎴愬姛
     */
    public boolean deleteCacheMapValue(final String key, final String hKey)
    {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 鑾峰緱缂撳瓨鐨勫熀鏈璞″垪琛?
     *
     * @param pattern 瀛楃涓插墠缂€
     * @return 瀵硅薄鍒楄〃
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }
}


