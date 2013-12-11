/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core.redis;

import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.apache.commons.pool.impl.GenericObjectPool;  
/**
 *
 * @author Milan Dojchinovski <http://dojchinovski.mk>
 */
public class RedisClient {
    
    private static RedisClient client = null;
    private static JedisPool jedisPool = null;    
    
    public static RedisClient getInstance() {
        if(client == null ){
            client = new RedisClient();
            configure();
        }
        return client;        
    }
    
    private static void configure(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();  
        /* The maximum active connections per Redis instance */  
        jedisPoolConfig.setMaxActive(30);  
        /* The minimum idling connections- these connections are always open and always ready */  
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setMaxIdle(5);
        /* Fail- fast behaviour Set the action to take when your pool runs out of connections  
         * default is to block the caller till a connection frees up */  
        jedisPoolConfig.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);  
        /*Tests if a connection is still alive at the time of retrieval*/  
        jedisPoolConfig.setTestOnBorrow(true);  
        /* Tests whether connections are dead during idle periods */  
        jedisPoolConfig.setTestWhileIdle(true);  
        /*Number of connections to check at each idle check*/  
        jedisPoolConfig.setNumTestsPerEvictionRun(10);  
        /* Check idling connections every */  
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(60000);  
        /*maximum time in milliseconds to wait when the exhaust action is set to block*/  
        jedisPoolConfig.setMaxWait(3000);  
        jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379);
    }

    public String getValue(String key) {
        Jedis jedis = jedisPool.getResource();
        String val = null;
        try {
            val = jedis.get(key);
        } catch (Exception ex) {
            Logger.getLogger(RedisClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jedisPool.returnResource(jedis);
            return val;
        }
    }
    
    public void setKey(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        try{            
            jedis.set(key, value);
        } catch(Exception ex) {
            Logger.getLogger(RedisClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jedisPool.returnResource(jedis);            
        }
    }    
}
