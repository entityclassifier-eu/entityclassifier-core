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
package cz.vse.fis.keg.entityclassifier.core.conf;

import cz.vse.fis.keg.entityclassifier.core.THDController;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.DBpediaOntologyManager;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.TypeMapper;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.YagoOntologyManager;
import cz.vse.fis.keg.entityclassifier.rest.authorizator.Authorizator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan
 */
public class Settings {
    
//    private boolean initialized = false;
    
    private static Settings instance = null;
    
    public static Settings getInstance() {
        if(instance == null) {
            Properties prop = new PropertiesLoader().getProperties();
            readSettings(prop);
            instance = new Settings();
        }
        return instance;
    }
    
    public static int    MONGODB_PORT;
    public static String MONGODB_URL;
    public static String MONGODB_DATABASE_NAME;
    
    public static int    REDIS_PORT;
    public static String REDIS_URL;
    
    public static String GATE_HOME;
    public static String PLUGINS_HOME;
    
    public static String EN_ENTITY_EXTRACTION_GRAMMAR;
    public static String DE_ENTITY_EXTRACTION_GRAMMAR;
    public static String NL_ENTITY_EXTRACTION_GRAMMAR;

    public static String EN_HYPERNYM_EXTRACTION_GRAMMAR;
    public static String DE_HYPERNYM_EXTRACTION_GRAMMAR;
    public static String NL_HYPERNYM_EXTRACTION_GRAMMAR;

    public static String NL_TAGGER_BINARY;
    public static String DE_TAGGER_BINARY;
    
    public static String EN_WIKIPEDIA_LOCAL_EXPORT;
    public static String DE_WIKIPEDIA_LOCAL_EXPORT;
    public static String NL_WIKIPEDIA_LOCAL_EXPORT;

    public static String EN_WIKIPEDIA_LIVE_EXPORT;
    public static String DE_WIKIPEDIA_LIVE_EXPORT;
    public static String NL_WIKIPEDIA_LIVE_EXPORT;
    
    public static String EN_WIKIPEDIA_LOCAL_API;
    public static String DE_WIKIPEDIA_LOCAL_API;
    public static String NL_WIKIPEDIA_LOCAL_API;

    public static String EN_WIKIPEDIA_LIVE_API;
    public static String DE_WIKIPEDIA_LIVE_API;
    public static String NL_WIKIPEDIA_LIVE_API;
    
    public static String EN_LUCENE;
    public static String DE_LUCENE;
    public static String NL_LUCENE;    
    
    public static String SALIENCE_DATASET;
    
    public static String EN_DBPEDIA_DISAMBIGUATION_DATASET;
    public static String DE_DBPEDIA_DISAMBIGUATION_DATASET;
    public static String NL_DBPEDIA_DISAMBIGUATION_DATASET;
    
    public static String SEMITAGS_SPOTTING_ENDPOINT;
    public static String SEMITAGS_LINKING_ENDPOINT;

    public static int POOL_SIZE;
    public static String ADMIN_KEY;
    
    private static void readSettings(Properties prop) {
        
        Settings.POOL_SIZE = Integer.parseInt(prop.getProperty("workers.pool.size"));
        System.out.println(Settings.POOL_SIZE);
                
        Settings.GATE_HOME = prop.getProperty("gate.home");
        System.out.println(Settings.GATE_HOME);
        Settings.PLUGINS_HOME = prop.getProperty("gate.plugins");
        System.out.println(Settings.PLUGINS_HOME);

        Settings.MONGODB_URL           = prop.getProperty("mongodb.url");
        System.out.println(Settings.MONGODB_URL);
        Settings.MONGODB_PORT          = Integer.parseInt(prop.getProperty("mongodb.port"));
        System.out.println(Settings.MONGODB_PORT);
        Settings.MONGODB_DATABASE_NAME = prop.getProperty("mongodb.database");
        System.out.println(Settings.MONGODB_DATABASE_NAME);
        
        Settings.DE_TAGGER_BINARY = prop.getProperty("pos.tagger.binary.de");
        System.out.println(Settings.DE_TAGGER_BINARY);
        Settings.NL_TAGGER_BINARY = prop.getProperty("pos.tagger.binary.nl");
        System.out.println(Settings.NL_TAGGER_BINARY);

        Settings.REDIS_URL = prop.getProperty("redis.url");
        System.out.println(Settings.REDIS_URL);
        Settings.REDIS_PORT = Integer.parseInt(prop.getProperty("redis.port"));
        System.out.println(Settings.REDIS_PORT);
        
        Settings.EN_LUCENE = prop.getProperty("lucene.en.url");
        System.out.println(Settings.EN_LUCENE);
        Settings.DE_LUCENE = prop.getProperty("lucene.de.url");
        System.out.println(Settings.DE_LUCENE);
        Settings.NL_LUCENE = prop.getProperty("lucene.nl.url");
        System.out.println(Settings.NL_LUCENE);
        
        Settings.EN_ENTITY_EXTRACTION_GRAMMAR = prop.getProperty("entity.extraction.jape.grammar.en");
        System.out.println(Settings.EN_ENTITY_EXTRACTION_GRAMMAR);
        Settings.DE_ENTITY_EXTRACTION_GRAMMAR = prop.getProperty("entity.extraction.jape.grammar.de");
        System.out.println(Settings.DE_ENTITY_EXTRACTION_GRAMMAR);
        Settings.NL_ENTITY_EXTRACTION_GRAMMAR = prop.getProperty("entity.extraction.jape.grammar.nl");
        System.out.println(Settings.NL_ENTITY_EXTRACTION_GRAMMAR);

        Settings.EN_HYPERNYM_EXTRACTION_GRAMMAR = prop.getProperty("hypernym.extraction.jape.grammar.en");
        System.out.println(Settings.EN_HYPERNYM_EXTRACTION_GRAMMAR);
        Settings.DE_HYPERNYM_EXTRACTION_GRAMMAR = prop.getProperty("hypernym.extraction.jape.grammar.de");
        System.out.println(Settings.DE_HYPERNYM_EXTRACTION_GRAMMAR);
        Settings.NL_HYPERNYM_EXTRACTION_GRAMMAR = prop.getProperty("hypernym.extraction.jape.grammar.nl");
        System.out.println(Settings.NL_HYPERNYM_EXTRACTION_GRAMMAR);
                
        DBpediaOntologyManager.setDbpediaOntologyFileLocation(prop.getProperty("dataset.dbpedia.ontology.location"));
//        System.out.println(Settings);
        YagoOntologyManager.setYagoOntologyFileLocation(prop.getProperty("dataset.yago.ontology.location"));
//        System.out.println(Settings);

        Settings.EN_WIKIPEDIA_LIVE_API = prop.getProperty("wikipedia.api.live.en");
        System.out.println(Settings.EN_WIKIPEDIA_LIVE_API);
        Settings.DE_WIKIPEDIA_LIVE_API = prop.getProperty("wikipedia.api.live.de");
        System.out.println(Settings.DE_WIKIPEDIA_LIVE_API);
        Settings.NL_WIKIPEDIA_LIVE_API = prop.getProperty("wikipedia.api.live.nl");
        System.out.println(Settings.NL_WIKIPEDIA_LIVE_API);

        Settings.EN_WIKIPEDIA_LOCAL_API = prop.getProperty("wikipedia.api.local.en");
        System.out.println(Settings.EN_WIKIPEDIA_LOCAL_API);
        Settings.DE_WIKIPEDIA_LOCAL_API = prop.getProperty("wikipedia.api.local.de");
        System.out.println(Settings.DE_WIKIPEDIA_LOCAL_API);
        Settings.NL_WIKIPEDIA_LOCAL_API = prop.getProperty("wikipedia.api.local.nl");
        System.out.println(Settings.NL_WIKIPEDIA_LOCAL_API);

        Settings.EN_WIKIPEDIA_LIVE_EXPORT = prop.getProperty("wikipedia.export.api.live.en");
        System.out.println(Settings.EN_WIKIPEDIA_LIVE_EXPORT);
        Settings.DE_WIKIPEDIA_LIVE_EXPORT = prop.getProperty("wikipedia.export.api.live.de");
        System.out.println(Settings.DE_WIKIPEDIA_LIVE_EXPORT);
        Settings.NL_WIKIPEDIA_LIVE_EXPORT = prop.getProperty("wikipedia.export.api.live.nl");
        System.out.println(Settings.NL_WIKIPEDIA_LIVE_EXPORT);

        Settings.EN_WIKIPEDIA_LOCAL_EXPORT = prop.getProperty("wikipedia.export.api.local.en");
        System.out.println(Settings.EN_WIKIPEDIA_LOCAL_EXPORT);
        Settings.DE_WIKIPEDIA_LOCAL_EXPORT = prop.getProperty("wikipedia.export.api.local.de");
        System.out.println(Settings.DE_WIKIPEDIA_LOCAL_EXPORT);
        Settings.NL_WIKIPEDIA_LOCAL_EXPORT = prop.getProperty("wikipedia.export.api.local.nl");
        System.out.println(Settings.NL_WIKIPEDIA_LOCAL_EXPORT);
        
        Settings.SALIENCE_DATASET = prop.getProperty("dataset.salience.reuters128");
        System.out.println(Settings.SALIENCE_DATASET);
        
        Settings.EN_DBPEDIA_DISAMBIGUATION_DATASET = prop.getProperty("dataset.dbpedia.disambiguation.en");
        System.out.println(Settings.EN_DBPEDIA_DISAMBIGUATION_DATASET);
        Settings.DE_DBPEDIA_DISAMBIGUATION_DATASET = prop.getProperty("dataset.dbpedia.disambiguation.de");
        System.out.println(Settings.DE_DBPEDIA_DISAMBIGUATION_DATASET);
        Settings.NL_DBPEDIA_DISAMBIGUATION_DATASET = prop.getProperty("dataset.dbpedia.disambiguation.nl");
        System.out.println(Settings.NL_DBPEDIA_DISAMBIGUATION_DATASET);
        
        System.out.println(prop.getProperty("dataset.lhd.inferrred.en"));
        System.out.println(prop.getProperty("dataset.lhd.inferrred.de"));
        System.out.println(prop.getProperty("dataset.lhd.inferrred.nl"));
        
        TypeMapper.setEn_inferred_mappings(prop.getProperty("dataset.lhd.inferrred.en"));
        TypeMapper.setDe_inferred_mappings(prop.getProperty("dataset.lhd.inferrred.de"));
        TypeMapper.setNl_inferred_mappings(prop.getProperty("dataset.lhd.inferrred.nl"));

        Settings.SEMITAGS_SPOTTING_ENDPOINT = prop.getProperty("semitags.endpoint.spotting");
        System.out.println(Settings.SEMITAGS_SPOTTING_ENDPOINT);
        Settings.SEMITAGS_LINKING_ENDPOINT = prop.getProperty("semitags.endpoint.linking");
        System.out.println(Settings.SEMITAGS_LINKING_ENDPOINT);
                
        Settings.ADMIN_KEY = prop.getProperty("entityclassifier.apikey.admin");

        Authorizator.getInstance().setAdminKey(Settings.ADMIN_KEY);
        TypeMapper.getInstance().init();

        Logger.getLogger(THDController.class.getName()).log(Level.INFO, "Configuration file successfully loaded.");            
    }
}
