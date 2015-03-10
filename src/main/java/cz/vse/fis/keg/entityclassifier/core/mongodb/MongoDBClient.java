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
package cz.vse.fis.keg.entityclassifier.core.mongodb;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import java.net.UnknownHostException;

/**
 * @author Milan Dojchinovski
 * <milan (at) dojchinovski (dot) mk>
 * Twitter: @m1ci
 * www: http://dojchinovski.mk
 */
public class MongoDBClient {
        
    private static MongoClient   mongoClient = null;
    private static DB            db          = null;

    public static DB getDBInstance() throws UnknownHostException{
        if(db == null){
            init();
            db = mongoClient.getDB( Settings.MONGODB_DATABASE_NAME );
        }
        return db;
    }
    
    public static MongoClient getClient() throws UnknownHostException{
        if(db == null){
            init();
            db = mongoClient.getDB( Settings.MONGODB_DATABASE_NAME );
        }
        return mongoClient;
    }

    public static void init() throws UnknownHostException {
//        new Settings().init();
        Settings.getInstance();
        mongoClient = new MongoClient( Settings.MONGODB_URL , Settings.MONGODB_PORT );
        System.out.println("mongoDB was sucessfully configured.");
    }
}
