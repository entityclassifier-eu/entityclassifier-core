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
package cz.vse.fis.keg.entityclassifier.rest.authorizator;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import cz.vse.fis.keg.entityclassifier.core.mongodb.MongoDBClient;
import cz.vse.fis.keg.entityclassifier.core.redis.RedisClient;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Milan Dojchinovski <milan@dojchinovski.,mk>
 */
public class Authorizator {
    
    private static Authorizator authorizator = null;
    private DBCursor cursor = null;
    private String adminkey = null;
    
    public static Authorizator getInstance() {
        if(authorizator == null) {
            authorizator = new Authorizator();
        }
        return authorizator;
    }
    
    public boolean isAdmin(String apikey) {
        return apikey.equals(adminkey);
    }
    
    public RateBucket getConsumersRateLimits(String apikey) {
        
        RateBucket rate = new RateBucket();
        try {
            BasicDBObject queryObj = new BasicDBObject();
            queryObj.append("apikey", apikey);
            
            DBObject user = MongoDBClient.getClient()
                    .getDB(Settings.MONGODB_DATABASE_NAME)
                    .getCollection("users")
                    .findOne(queryObj);
            
            if(user != null) {
                int limit = Integer.parseInt(user.get("limit").toString());
                int interval = Integer.parseInt(user.get("interval").toString());
                return getLimits(apikey, limit, interval, rate);                
            } else {
                rate.setIsKeyValid(false);
                rate.setIsAuthorized(false);
                return rate;
            }            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Authorizator.class.getName()).log(Level.SEVERE, null, ex);
            rate.setIsKeyValid(false);
            rate.setIsAuthorized(false);
            return rate;
        }
    }
    
    public RateBucket isAuthorized(String apikey) {
        RateBucket rate = new RateBucket();
        if(apikey.equals(adminkey)) {
            int limit = 999999;
            int interval = 999999;
            return logRequest(apikey, limit, interval, rate);        
        }
        try {
            BasicDBObject queryObj = new BasicDBObject();
            queryObj.append("apikey", apikey);
            
            DBObject user = MongoDBClient
                    .getClient()
                    .getDB(Settings.MONGODB_DATABASE_NAME)
                    .getCollection("users")
                    .findOne(queryObj);
            
            if(user != null) {
                int limit = Integer.parseInt(user.get("limit").toString());
                int interval = Integer.parseInt(user.get("interval").toString());
                return logRequest(apikey, limit, interval, rate);                
            } else {
                rate.setIsKeyValid(false);
                rate.setIsAuthorized(false);
                return rate;
            }            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Authorizator.class.getName()).log(Level.SEVERE, null, ex);
            rate.setIsKeyValid(false);
            rate.setIsAuthorized(false);
            return rate;
        }
    }
    
    public RateBucket getLimits(String key, int limitRequestsPerInterval, int timeInterval, RateBucket rate) {
        
        Jedis jedis = RedisClient.getInstance().getResource();
        try {
            String numRequestsStr = jedis.get(key);
            if(numRequestsStr == null) {
                // First time
                rate.setIsAuthorized(true);
                rate.setIsKeyValid(true);
                rate.setLeftRequests(limitRequestsPerInterval);
                rate.setTimeUntilReset(timeInterval);
                rate.setIntervalLimit(limitRequestsPerInterval);
                return rate;
            } else {
                int numRequests = Integer.parseInt(numRequestsStr);
                if(numRequests >= limitRequestsPerInterval) {
                    rate.setIsKeyValid(true);
                    rate.setIsAuthorized(false);
                    rate.setLeftRequests(0);
                    rate.setTimeUntilReset(jedis.ttl(key).intValue());
                    rate.setIntervalLimit(limitRequestsPerInterval);
                    return rate;
                } else {
                    rate.setIsKeyValid(true);
                    rate.setIsAuthorized(true);
                    rate.setLeftRequests(limitRequestsPerInterval-numRequests);
                    rate.setTimeUntilReset(jedis.ttl(key).intValue());
                    rate.setIntervalLimit(limitRequestsPerInterval);
                    return rate;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(RedisClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            RedisClient.getInstance().returnResource(jedis);
        }
        rate.setIsAuthorized(false);
        return rate;
    }
    
    public RateBucket logRequest(String key, int limitRequestsPerInterval, int timeInterval, RateBucket rate) {
        
        Jedis jedis = RedisClient.getInstance().getResource();
        try {
            String numRequestsStr = jedis.get(key);
            if(numRequestsStr == null) {
                // First time
                jedis.incr(key);
                jedis.expire(key, timeInterval);
                rate.setIsAuthorized(true);
                rate.setIsKeyValid(true);
                rate.setLeftRequests(limitRequestsPerInterval-1);
                rate.setTimeUntilReset(jedis.ttl(key).intValue());
                rate.setIntervalLimit(limitRequestsPerInterval);
                return rate;
            } else {
                int numRequests = Integer.parseInt(numRequestsStr);
                if(numRequests >= limitRequestsPerInterval) {
                    rate.setIsKeyValid(true);
                    rate.setIsAuthorized(false);
                    rate.setLeftRequests(0);
                    rate.setTimeUntilReset(jedis.ttl(key).intValue());
                    rate.setIntervalLimit(limitRequestsPerInterval);
                    return rate;
                } else {
                    numRequests ++;
                    jedis.incr(key);
                    rate.setIsKeyValid(true);
                    rate.setIsAuthorized(true);
                    rate.setLeftRequests(limitRequestsPerInterval-numRequests);
                    rate.setTimeUntilReset(jedis.ttl(key).intValue());
                    rate.setIntervalLimit(limitRequestsPerInterval);
                    return rate;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(RedisClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            RedisClient.getInstance().returnResource(jedis);
        }
        rate.setIsAuthorized(false);
        return rate;
    }
    
    public ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList();
        try {
            DBCursor usersCur = MongoDBClient
                    .getClient()
                    .getDB(Settings.MONGODB_DATABASE_NAME)
                    .getCollection("users")
                    .find();
            while(usersCur.hasNext()) {
                DBObject userObj = usersCur.next();
                User user = new User();
                user.setLimit(Integer.parseInt(userObj.get("limit").toString()));
                user.setInterval(Integer.parseInt(userObj.get("interval").toString()));
                user.setName(userObj.get("requester").toString());
                user.setApikey(userObj.get("apikey").toString());
                users.add(user);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Authorizator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return users;
    }

    /**
     * @return the adminKey
     */
    public String getAdminKey() {
        return adminkey;
    }

    /**
     * @param adminKey the adminKey to set
     */
    public void setAdminKey(String adminkey) {
        this.adminkey = adminkey;
    }
}
