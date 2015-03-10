/*
 * #%L
 * Entityclassifier.eu NER CORE v3.9
 * %%
 * Copyright (C) 2015 Knowledge Engineering Group (KEG) and Web Intelligence Research Group (WIRG) - Milan Dojchinovski (milan.dojchinovski@fit.cvut.cz)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package cz.vse.fis.keg.entityclassifier.core.redis;

import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author Milan Dojchinovski <http://dojchinovski.mk>
 */
public class RedisClient {
    
    private static RedisClient client = null;
    private static JedisPool jedisPool = null;    
    
    public static RedisClient getInstance() {
        if(client == null ) {
            client = new RedisClient();
            Settings.getInstance();
            configure();
        }
        return client;        
    }
    
    private static void configure(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();  
        /* The maximum active connections per Redis instance */  

        jedisPoolConfig.setMaxTotal(30);//        jedisPoolConfig.setMaxActive(30);
        /* The minimum idling connections- these connections are always open and always ready */  
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setMaxIdle(5);
        /* Fail- fast behaviour Set the action to take when your pool runs out of connections  
         * default is to block the caller till a connection frees up */  
        jedisPoolConfig.setBlockWhenExhausted(true);//setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);  
        /*Tests if a connection is still alive at the time of retrieval*/  
        jedisPoolConfig.setTestOnBorrow(true);  
        /* Tests whether connections are dead during idle periods */  
        jedisPoolConfig.setTestWhileIdle(true);  
        /*Number of connections to check at each idle check*/  
        jedisPoolConfig.setNumTestsPerEvictionRun(10);  
        /* Check idling connections every */  
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(60000);  
        /*maximum time in milliseconds to wait when the exhaust action is set to block*/  
        jedisPoolConfig.setMaxWaitMillis(3000);
//        jedisPoolConfig.setMaxWait(3000);
//        jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379);
        jedisPool = new JedisPool(jedisPoolConfig, Settings.REDIS_URL, Settings.REDIS_PORT);
        System.out.println("Redis was sucessfully configured.");
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
    
    public Jedis getResource() {
        RedisClient.getInstance();
        return jedisPool.getResource();
    }
    
    public void returnResource(Jedis res) {
        jedisPool.returnResource(res);
    }
    
    public void runPattern(String key) {
        RedisClient.getInstance();
        Jedis jedis = jedisPool.getResource();
        try {

            String numRequestsStr = jedis.get(key);
            if(numRequestsStr == null) {
                jedis.incr(key);
                jedis.expire(key, 10);
            } else {
                int numRequests = Integer.parseInt(numRequestsStr);
                if(numRequests >= 4) {
                } else {
                    numRequests ++;
                    jedis.incr(key);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RedisClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }
    
    public static void main(String[] args) {
        RedisClient.getInstance().runPattern("mykey");
    }
}
