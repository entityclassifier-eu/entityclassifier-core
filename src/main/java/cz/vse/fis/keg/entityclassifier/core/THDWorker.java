/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vse.fis.keg.entityclassifier.core;

import cz.vse.fis.keg.entityclassifier.core.conf.Settings;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import cz.vse.fis.keg.entityclassifier.core.entitylinking.LinkedEntity;
import cz.vse.fis.keg.entityclassifier.core.entitylinking.LuceneSearch;
import cz.vse.fis.keg.entityclassifier.core.mongodb.MongoDBClient;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.DBpediaMapping;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.DBpediaOntologyManager;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.DBpediaOntologyMapper;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.OntoRecord;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.TypeMapper;
import cz.vse.fis.keg.entityclassifier.core.ontologymapper.YagoOntologyManager;
import cz.vse.fis.keg.entityclassifier.core.redis.RedisClient;
import cz.vse.fis.keg.entityclassifier.core.vao.Article;
import cz.vse.fis.keg.entityclassifier.core.vao.Confidence;
import cz.vse.fis.keg.entityclassifier.core.vao.Entity;
import cz.vse.fis.keg.entityclassifier.core.vao.Hypernym;
import cz.vse.fis.keg.entityclassifier.core.vao.Type;
import cz.vse.fis.keg.entityclassifier.core.entitylinking.WikipediaSearch;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Node;
import gate.ProcessingResource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.InvalidOffsetException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan
 */
public class THDWorker {
    
    private SerialAnalyserController enEntityExtractionPipeline = null;
    private SerialAnalyserController deEntityExtractionPipeline = null;    
    private SerialAnalyserController nlEntityExtractionPipeline = null;
    
    private SerialAnalyserController hypernymExtractionPipelineEN = null;
    private SerialAnalyserController hypernymExtractionPipelineDE = null;
    private SerialAnalyserController hypernymExtractionPipelineNL = null;
    
    public THDWorker() {
        assamblePipelines();
    }
    
    public ArrayList<Entity> processTextAPI_MT(String query, String lang, String entity_type, String knowledge_base, String[] provenance, boolean priorityEntityLinking, String typesFilter, String linkingMethod) throws ResourceInstantiationException, ExecutionException, UnknownHostException {
        
        ArrayList<Entity> resultEntities = new ArrayList<Entity>();        
        ArrayList<Entity> extractedEntities = new ArrayList<Entity>();

        extractedEntities = extractEntityCandidates(query, lang, entity_type);
        System.out.println("Extracted candidates: " + extractedEntities.size());
        RedisClient redis = RedisClient.getInstance();
        for(Entity e : extractedEntities) {
//            System.out.println("Extracted Entity:" + e.getUnderlyingString());
            Entity foundEntity = new Entity();;
            String entityString = e.getUnderlyingString().trim();

            LinkedEntity linkedEntity = null;
            boolean continueLinking = true;
            String longestEntityPage = null;
            String[] words = null;

            while(continueLinking) {
                                
                if(knowledge_base.equals("linkedHypernymsDataset")) {
                    
                    String linkedEntityStr = redis.getValue(entityString + lang + "linkedHypernymsDataset"+linkingMethod);

                    if(linkedEntityStr == null) {

//                        System.out.println("not found in cache");
//                        entity_title = WikipediaSearch.getInstance().findWikipediaArticle(entityString, lang, "local");
                        if(linkingMethod.equals("LuceneSearch")) {
                            linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(entityString, lang, "local");
//                            System.out.println("================================");
//                            System.out.println(linkedEntity.getPageTitle());
//                            System.out.println(linkedEntity.getConfidence());
//                            System.out.println("================================");
                        } else if(linkingMethod.equals("WikipediaSearch")){
                            linkedEntity = WikipediaSearch.getInstance().findWikipediaArticle(entityString, lang, "local");                            
//                            System.out.println("================================");
//                            System.out.println(linkedEntity.getPageTitle());
//                            System.out.println(linkedEntity.getConfidence());
//                            System.out.println("================================");
                        } else {
                            System.out.println("PROBLEM");
                        }
                        if(linkedEntity != null) {
//                        System.out.println(entity_title);
                            redis.setKey(entityString + lang + "linkedHypernymsDataset"+linkingMethod, linkedEntity.toString());
                        }
                        
                    } else {
                        // Linked entity found in cache.
                        linkedEntity = new LinkedEntity();
                        linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                        linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));
//                        System.out.println("found in cache");
                    }
                }else if(knowledge_base.equals("live")) {
                    
                    String linkedEntityStr = redis.getValue(entityString + lang + "live");
                    
                    if(linkedEntityStr == null) {
                        
//                        System.out.println("not found in cache");                        
                        linkedEntity = WikipediaSearch.getInstance().findWikipediaArticle(entityString, lang, "live");
                        if(linkedEntity != null) {
                            redis.setKey(entityString+lang + "live"+linkingMethod, linkedEntity.toString());
                        }
                    } else {
                        // Linked entity found in cache.
                        linkedEntity = new LinkedEntity();
                        linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                        linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));
//                        System.out.println("found in cache");
                    }
                    
                } else if(knowledge_base.equals("local")){
                    String linkedEntityStr = redis.getValue(entityString + lang + "local"+linkingMethod);

                    if(linkedEntityStr == null) {
                        
//                        System.out.println("not found in cache");
                        if(linkingMethod.equals("LuceneSearch")) {
                            linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(entityString, lang, "local");
                        } else if(linkingMethod.equals("WikipediaSearch")) {
                            linkedEntity = WikipediaSearch.getInstance().findWikipediaArticle(entityString, lang, "local");                        
                        } else {
                            System.out.println("PROBLEM");
                        }
                        if(linkedEntity != null) {
                            redis.setKey(entityString + lang + "local"+linkingMethod, linkedEntity.toString());
                        }                        
                    }else {
                        // Linked entity found in cache.
                        linkedEntity = new LinkedEntity();
                        linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                        linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));

//                        System.out.println("found in cache");
                    }
                }
                    
                // ENTITY NOT MAPPED TO DBpedia
                if ( linkedEntity == null ) {

                        words = entityString.split("\\s+");
                        if(words.length > 1) {
                            entityString = entityString.split("\\s+", 2)[1];
//                            entityString = entityString.split(" ", 2)[1];
                            // nothing, try to link again with shorter text
                        } else {
                        // ENTITY LINKED or CANNOT be LINKED
                        if(priorityEntityLinking) {
                            // priority linked
                            foundEntity = new Entity(e.getUnderlyingString(), e.getStartOffset(), e.getEndOffset(), e.getEntityType());                            
                            resultEntities.add(foundEntity);
                                
                        } else {
                            // no priority linking
                            foundEntity = new Entity();
                            foundEntity.setStartOffset(e.getStartOffset());
                            foundEntity.setEndOffset(e.getEndOffset());
                            foundEntity.setEntityType(e.getEntityType());
                                    
                            if(longestEntityPage != null) {
                                
                                foundEntity.setUnderlyingString(e.getUnderlyingString());
                                ArrayList<Type> entityTypes = new ArrayList<Type>();
                                
                                Type t = new Type();
                                t.setEntityLabel(longestEntityPage);
                                switch(lang) {
                                    case "en":
                                        t.setEntityURI("http://dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                        break;
                                    
                                    case "de":
                                        t.setEntityURI("http://de.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                        break;
                                    
                                    case "nl":
                                        t.setEntityURI("http://nl.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                        break;
                                }
                                t.setProvenance("thd");
                                entityTypes.add(t);
                                
                                foundEntity.setTypes(entityTypes);
                            } else{
                                foundEntity.setUnderlyingString(e.getUnderlyingString());
                            }
                            resultEntities.add(foundEntity);
                        }
                        continueLinking = false;
                    }
                } else {
                    
//                    System.out.println("entity title mapped to DBpedia");
//                    System.out.println(linkedEntity.getPageTitle());
//                    System.out.println(linkedEntity.getConfidence());
                    // entity mapped, checking types
                    HashSet<Hypernym> hypernymsList = null;
                    if (knowledge_base.equals("live")) {
                        hypernymsList = extractEntityTypes(linkedEntity.getPageTitle(), lang, "live", provenance);                    
                    } else if (knowledge_base.equals("local")) {                    
                        hypernymsList = extractEntityTypes(linkedEntity.getPageTitle(), lang, "local", provenance);                    
                    } else if (knowledge_base.equals("linkedHypernymsDataset")) {
                        hypernymsList = extractEntityTypes(linkedEntity.getPageTitle(), lang, "linkedHypernymsDataset", provenance);
                    }
                        
                    if (hypernymsList.size() > 0) {
                        // TYPES FOUND
                        // found types, ending linking
                        foundEntity = new Entity();
                        foundEntity.setUnderlyingString(e.getUnderlyingString());
                        foundEntity.setStartOffset(e.getStartOffset());
                        foundEntity.setEndOffset(e.getEndOffset());
                        foundEntity.setEntityType(e.getEntityType());
                        
                        ArrayList<Type> entityTypes = new ArrayList<Type>();
                        
                        for(Hypernym h : hypernymsList) {
                            
                            Type type = new Type();
                            type.setEntityLabel(h.getEntity());
                            type.setEntityURI(h.getEntityURL());
                            type.setTypeLabel(h.getType());
                            type.setTypeURI(h.getTypeURL());                            

                            type.setProvenance(h.getOrigin());
                            
                            Confidence classificationConf = new Confidence();
                            classificationConf.setValue(Double.parseDouble(h.getAccuracy()));
                            classificationConf.setType("classification");
                            type.setClassificationConfidence(classificationConf);
                            
                            Confidence linkingConf = new Confidence();
                            linkingConf.setType("linking");

                            DecimalFormat df = new DecimalFormat("0.000");
                            double fConfidence = -1.0;
                            fConfidence = Double.parseDouble(df.format(linkedEntity.getConfidence()));
                            
                            type.setLinkingConfidence(linkingConf);
                            linkingConf.setValue(fConfidence);
                            
                            entityTypes.add(type);
//                            System.out.println("Yes");
                            
                        }
                        
                        foundEntity.setTypes(entityTypes);
                        resultEntities.add(foundEntity);
                        
                        continueLinking = false;
                    } else {
                            // NO TYPES
                            // if priority, then end, otherwise continue
                            if(priorityEntityLinking) {
                                // PRIORITY ON
                                // priority linking
                                foundEntity = new Entity();
                                foundEntity.setUnderlyingString(e.getUnderlyingString());
                                foundEntity.setStartOffset(e.getStartOffset());
                                foundEntity.setEndOffset(e.getEndOffset());
                                foundEntity.setEntityType(e.getEntityType());
                                ArrayList<Type> entityTypes = new ArrayList<Type>();
                                Type t = new Type();
                                t.setProvenance("thd");
                                switch(lang){
                                    case "en":
                                        t.setEntityURI("http://dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                        break;
                                    case "de":
                                        t.setEntityURI("http://de.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                        break;
                                    case "nl":
                                        t.setEntityURI("http://nl.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                        break;
                                }
                                t.setEntityLabel(linkedEntity.getPageTitle());
                                
                                Confidence linkingConf = new Confidence();
                                linkingConf.setType("linking");
                                
                                DecimalFormat df = new DecimalFormat("0.000");
                                double fConfidence = -1.0;
                                fConfidence = Double.parseDouble(df.format(linkedEntity.getConfidence()));
                                
                                linkingConf.setValue(fConfidence);
                                t.setLinkingConfidence(linkingConf);
                                
                                entityTypes.add(t);
                                foundEntity.setTypes(entityTypes);                               
                                resultEntities.add(foundEntity);
                                
                                continueLinking = false;
                            } else {
                                // PRIORITY OFF, NO TYPES found
                                // no priority linking continue to search
                                words = entityString.split("\\s+");
                                if(words.length > 1) {
                                    // PRIORITY OFF, MORE SEARCHING
                                    entityString = entityString.split("\\s+", 2)[1];
//                                    entityString = entityString.split(" ", 2)[1];
                                    if(longestEntityPage == null){
                                        longestEntityPage = linkedEntity.getPageTitle();
                                    }
                                } else {
                                    // PRIORITY OFF, NO MORE SEARCH
                                    foundEntity = new Entity();
                                    foundEntity.setStartOffset(e.getStartOffset());
                                    foundEntity.setEndOffset(e.getEndOffset());
                                    foundEntity.setEntityType(e.getEntityType());
                                    ArrayList<Type> entityTypes = new ArrayList<Type>();
                                    Type t = new Type();
                                    t.setProvenance("thd");

                                    if(longestEntityPage != null) {
                                        foundEntity.setUnderlyingString(e.getUnderlyingString());
                                        switch(lang){
                                            case "en":
                                                t.setEntityURI("http://dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                                entityTypes.add(t);
                                                break;

                                            case "de":
                                                t.setEntityURI("http://de.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                                entityTypes.add(t);
                                                break;

                                            case "nl":
                                                t.setEntityURI("http://nl.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                                entityTypes.add(t);
                                                break;
                                        }
                                        
                                        Confidence linkingConf = new Confidence();
                                        linkingConf.setType("linking");
                                        
                                        DecimalFormat df = new DecimalFormat("0.000");
                                        double fConfidence = -1.0;
                                        fConfidence = Double.parseDouble(df.format(linkedEntity.getConfidence()));
                                        
                                        linkingConf.setValue(fConfidence);
                                        t.setLinkingConfidence(linkingConf);
                                        
                                        t.setEntityLabel(longestEntityPage);
                                    } else {
                                        foundEntity.setUnderlyingString(e.getUnderlyingString());
                                        switch(lang){
                                            case "en":
                                                t.setEntityURI("http://dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                                entityTypes.add(t);
                                                break;

                                            case "de":
                                                t.setEntityURI("http://de.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                                entityTypes.add(t);
                                                break;

                                            case "nl":
                                                t.setEntityURI("http://nl.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                                entityTypes.add(t);
                                                break;
                                        }
                                        Confidence linkingConf = new Confidence();
                                        linkingConf.setType("linking");
                                        
                                        DecimalFormat df = new DecimalFormat("0.000");
                                        double fConfidence = -1.0;
                                        fConfidence = Double.parseDouble(df.format(linkedEntity.getConfidence()));
                                        
                                        linkingConf.setValue(fConfidence);
                                        t.setLinkingConfidence(linkingConf);

                                        t.setEntityLabel(linkedEntity.getPageTitle());
                                    }
                                    foundEntity.setTypes(entityTypes);
                                    resultEntities.add(foundEntity);                                    
                                    continueLinking = false;
                                }
                            }
                        }
                    // Align the DE/NL enity URIs with EN URI
//                    switch(lang){
//                        case "de":
//                            // Spawning one more entity with just another localized URI
////                            HashSet<Hypernym> resHypListEnde = new HashSet();
//
//                            Entity replEntityDE = new Entity();
//                            replEntityDE.setStartOffset(e.getStartOffset());
//                            replEntityDE.setEndOffset(e.getEndOffset());
//                            replEntityDE.setEntityType(e.getEntityType());
//                            replEntityDE.setUnderlyingString(foundEntity.getUnderlyingString());
//                            ArrayList<Type> newArrayTypes = new ArrayList();
//                            
//                            boolean addEntity = false;
//                            
//                            for(Type t : foundEntity.getTypes()) {
//                                
//                                if(t.getEntityURI().startsWith("http://de.dbpedia.org/resource/")
//                                        && (t.getProvenance().equals("thd")
//                                        || t.getProvenance().equals("thd-derived")
//                                        )){
//                                    BasicDBObject queryObj = new BasicDBObject();
//                                    queryObj.append("de_uri",t.getEntityURI());
//                                    BasicDBObject projObj = new BasicDBObject();
//                                    projObj.append("en_uri", 1);
//                                    
//                                    DBObject resObj = MongoDBClient.getDBInstance().getCollection("interlanguage_links").findOne(queryObj, projObj);
//                                    
//                                    if(resObj != null) {
//                                        
//                                        addEntity = true;
//                                        String enInterLangLink = resObj.get("en_uri").toString();
//                                        Type newType = new Type();
//                                        newType.setConfidence(t.getConfidence());
//                                        newType.setEntityLabel(t.getEntityLabel());
//                                        newType.setEntityURI(enInterLangLink);
//                                        newType.setProvenance(t.getProvenance());
//                                        newType.setTypeLabel(t.getTypeLabel());
//                                        newType.setTypeURI(t.getTypeURI());
//                                        
//                                        replEntityDE.setTypes(foundEntity.getTypes());
//                                        resultEntities.add(replEntityDE);
//                                        
//                                    }
//                                }
//                            }
////                            resHypList.addAll(resHypListEnde);
//                            break;
//                        
//                        } 
                    // end - entity mapped
//                    switch(lang){
//                        case "de":
//                            // Spawning one more entity with just another localized URI
//                            Entity replEntityDE = new Entity();
//                            replEntityDE.setStartOffset(e.getStartOffset());
//                            replEntityDE.setEndOffset(e.getEndOffset());
//                            replEntityDE.setEntityType(e.getEntityType());
//                            replEntityDE.setUnderlyingString(foundEntity.getUnderlyingString());
//                            replEntityDE.setTypes(foundEntity.getTypes());
//                            resultEntities.add(replEntityDE);
//
//                            break;
//                        case "nl":
//                            // Spawning one more entity with just another localized URI
//                            Entity replEntityNL = new Entity();
//                            replEntityNL.setStartOffset(e.getStartOffset());
//                            replEntityNL.setEndOffset(e.getEndOffset());
//                            replEntityNL.setEntityType(e.getEntityType());
//                            replEntityNL.setUnderlyingString(foundEntity.getUnderlyingString());
//                            replEntityNL.setTypes(foundEntity.getTypes());
//                            resultEntities.add(replEntityNL);
//                            
//                            break;
//                    }
                }
            }
        }

        // filtering out types
        if(typesFilter.equals("dbo")) {
            
            ArrayList<Entity> dboEntityList = new ArrayList();
            for(Entity e  : resultEntities){
                for(Type t : e.getTypes()){
                    if(t.getTypeURI().contains("ontology")) {
                        dboEntityList.add(e);
                    }
                }
            }
            resultEntities = dboEntityList;
            
        } else if(typesFilter.equals("dbinstance")){
            
            ArrayList<Entity> dbinstanceEntityList = new ArrayList();
            for(Entity e  : resultEntities){
                for(Type t : e.getTypes()){
                    if(t.getTypeURI().contains("resource")) {
                        dbinstanceEntityList.add(e);
                    }                
                }
            }
            resultEntities = dbinstanceEntityList;
        
        } else if(typesFilter.equals("all")){
            return resultEntities;
        }
        
        return resultEntities;
    }
    
    public ArrayList<Hypernym> processText_MT(String query, String lang, String entity_type, String knowledge_base, String[] provenance, boolean priorityEntityLinking, String typesFilter) throws ResourceInstantiationException, ExecutionException, UnknownHostException {
        try {
        ArrayList<Entity> extractedEntities = new ArrayList<Entity>();

        extractedEntities = extractEntityCandidates(query, lang, entity_type);
        System.out.println("Extracted candidates: " + extractedEntities.size());
        HashSet<Hypernym> resHypList = new HashSet();
        RedisClient redis = RedisClient.getInstance();
        for(Entity e : extractedEntities){
//            System.out.println("processing one candidate" + e.getUnderlyingString());
            String entityString = e.getUnderlyingString().trim();
//            System.out.println("check 1");
                LinkedEntity linkedEntity = null;
                boolean continueLinking = true;
                String longestEntityPage = null;
                String[] words = null;
                
                while(continueLinking) {
                    
                    if(knowledge_base.equals("linkedHypernymsDataset")){
                        String linkedEntityStr = redis.getValue(entityString + lang + "linkedHypernymsDataset");
                        if(linkedEntityStr == null) {
//                            System.out.println("not found in cache");
                            linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(entityString, lang, "local");
                            if(linkedEntity != null){
                                redis.setKey(entityString + lang + "linkedHypernymsDataset", linkedEntity.toString());
                            }
                        } else {
                            linkedEntity = new LinkedEntity();
                            linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                            linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));
//                            System.out.println("found in cache: " + entity_title);
                        }
                    }else if(knowledge_base.equals("live")) {
                        String linkedEntityStr = redis.getValue(entityString + lang + "live");
                        if(linkedEntityStr == null) {
//                            System.out.println("not found in cache");
                            linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(entityString, lang, "live");
                            if(linkedEntityStr != null) {                                
                                redis.setKey(entityString + lang + "local", linkedEntity.toString());
                            }
                        }
                    } else if(knowledge_base.equals("local")) {
                        String linkedEntityStr = redis.getValue(entityString + lang + "local");
                        if(linkedEntityStr == null) {
//                            System.out.println("not found in cache");
                            linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(entityString, lang, "local");
                            if(linkedEntityStr != null){
                                redis.setKey(entityString + lang + "local", linkedEntity.toString());
                            }
                        } else {
                            linkedEntity = new LinkedEntity();
                            linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                            linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));
//                            System.out.println("found in cache");
                        }
                    }

                    // ENTITY NOT MAPPED TO DBpedia
                    if ( linkedEntity == null ) {
//                        System.out.println("this shit: " + entityString);
                        words = entityString.split("\\s+");
                        if(words.length > 1) {
                            entityString = entityString.split("\\s+", 2)[1];
//                            entityString = entityString.split(" ", 2)[1];
                            // nothing, try to link again with shorter text
                        } else {
                            // ENTITY LINKED or CANNOT be LINKED
                            if(priorityEntityLinking) {
                                // priority linked
                                Hypernym h = new Hypernym();
                                h.setAccuracy("-1");
                                h.setBounds("-1");
                                h.setStartOffset(e.getStartOffset());
                                h.setEndOffset(e.getEndOffset());
                                h.setOrigin("thd");
                                h.setType("");
                                h.setTypeURL("");
                                h.setEntityURL("");
                                h.setEntity(e.getUnderlyingString());
                                h.setUnderlyingEntityText(e.getUnderlyingString());
                                resHypList.add(h);
                                
                            } else {
                                // no priority linking
                                Hypernym h = new Hypernym();
                                h.setAccuracy("-1");
                                h.setBounds("-1");
                                h.setStartOffset(e.getStartOffset());
                                h.setEndOffset(e.getEndOffset());
                                h.setOrigin("thd");
                                h.setType("");
                                h.setTypeURL("");
                                h.setUnderlyingEntityText(e.getUnderlyingString());
                                
                                if(longestEntityPage != null){
                                    h.setEntity(longestEntityPage);
                                    switch(lang){
                                        case "en":
                                            h.setEntityURL("http://dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                            break;

                                        case "de":
                                            h.setEntityURL("http://de.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                            break;

                                        case "nl":
                                            h.setEntityURL("http://nl.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                            break;
                                    }
                                }else{
                                    h.setEntity("");
                                    h.setEntityURL("");                                
                                }
                                resHypList.add(h);                            
                            }
                            continueLinking = false;
                        }
                    } else {
//                        System.out.println("Entity mapped to DBpedia");
                        // entity mapped, checking types
                        HashSet<Hypernym> hypernymsList = null;
                    
                        if (knowledge_base.equals("live")) {
                            hypernymsList = extractEntityTypes(linkedEntity.getPageTitle(), lang, "live", provenance);                    
                        } else if (knowledge_base.equals("local")) {                    
                            hypernymsList = extractEntityTypes(linkedEntity.getPageTitle(), lang, "local", provenance);                    
                        } else if (knowledge_base.equals("linkedHypernymsDataset")) {
                            hypernymsList = extractEntityTypes(linkedEntity.getPageTitle(), lang, "linkedHypernymsDataset", provenance);
                        }
                        
                        if (hypernymsList.size() > 0) {
                            // TYPES FOUND
                            // found types, ending linking
                            continueLinking = false;
                            for(Hypernym h : hypernymsList) {
                                h.setUnderlyingEntityText(e.getUnderlyingString());
                                h.setStartOffset(e.getStartOffset());
                                h.setEndOffset(e.getEndOffset());
                                resHypList.add(h);
                            }
                        } else {
                            // NO TYPES
                            // if priority, then end, otherwise continue
                            if(priorityEntityLinking) {
                                // PRIORITY ON
                                // priority linking
                                Hypernym h = new Hypernym();
                                h.setAccuracy("-1");
                                h.setBounds("-1");
                                h.setStartOffset(e.getStartOffset());
                                h.setEndOffset(e.getEndOffset());
                                h.setOrigin("thd");
                                h.setType("");
                                h.setTypeURL("");
                                h.setEntity(linkedEntity.getPageTitle());
                                h.setUnderlyingEntityText(e.getUnderlyingString());
                                
                                switch(lang) {
                                        case "en":
                                            h.setEntityURL("http://dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                            break;

                                        case "de":
                                            h.setEntityURL("http://de.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                            break;

                                        case "nl":
                                            h.setEntityURL("http://nl.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                            break;
                                    }
                                resHypList.add(h);
                                continueLinking = false;
                            } else {
                                // PRIORITY OFF, NO TYPES found
                                // no priority linking continue to search
                                words = entityString.split("\\s+");
                                if(words.length > 1) {
                                    // PRIORITY OFF, MORE SEARCHING
                                    entityString = entityString.split("\\s+", 2)[1];
//                                    entityString = entityString.split(" ", 2)[1];
                                    if(longestEntityPage == null){
                                        longestEntityPage = linkedEntity.getPageTitle();
                                    }
                                } else {
                                    // PRIORITY OFF, NO MORE SEARCH
                                    Hypernym h = new Hypernym();
                                    h.setAccuracy("-1");
                                    h.setBounds("-1");
                                    h.setStartOffset(e.getStartOffset());
                                    h.setEndOffset(e.getEndOffset());
                                    h.setOrigin("thd");
                                    h.setType("");
                                    h.setTypeURL("");                                    
                                    h.setUnderlyingEntityText(e.getUnderlyingString());
                                    if(longestEntityPage != null) {
                                        h.setEntity(longestEntityPage);
                                        switch(lang){
                                            case "en":
                                                h.setEntityURL("http://dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                                break;

                                            case "de":
                                                h.setEntityURL("http://de.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                                break;

                                            case "nl":
                                                h.setEntityURL("http://nl.dbpedia.org/resource/"+longestEntityPage.replace(" ", "_"));
                                                break;
                                        }
                                    }else{
                                        h.setEntity(linkedEntity.getPageTitle());
                                        switch(lang){
                                            case "en":
                                                h.setEntityURL("http://dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                                break;

                                            case "de":
                                                h.setEntityURL("http://de.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                                break;

                                            case "nl":
                                                h.setEntityURL("http://nl.dbpedia.org/resource/"+linkedEntity.getPageTitle().replace(" ", "_"));
                                                break;
                                        }
                                    }
                                    resHypList.add(h);
                                    continueLinking = false;
                                }
                            }
                        }
                    }

                    // Align the DE/NL enity URIs with EN URI
                    switch(lang){
                        case "de":
                            // Spawning one more entity with just another localized URI
                            HashSet<Hypernym> resHypListEnde = new HashSet();
                            for(Hypernym h : resHypList) {
                                if(h.getEntityURL().startsWith("http://de.dbpedia.org/resource/")
                                        && (h.getOrigin().equals("thd")
                                        || h.getOrigin().equals("thd-derived")
                                        )){
                                    
                                    BasicDBObject queryObj = new BasicDBObject();
                                    queryObj.append("de_uri",h.getEntityURL());
                                    BasicDBObject projObj = new BasicDBObject();
                                    projObj.append("en_uri", 1);
                                    
//                                    System.out.println("link: " + h.getEntityURL());
                                    DBObject resObj = MongoDBClient.getDBInstance().getCollection("interlanguage_links").findOne(queryObj, projObj);
                                    
                                    if(resObj != null) {
                                        
                                        if(resObj.get("en_uri") != null) {
                                            String enInterLangLink = resObj.get("en_uri").toString();
                                            Hypernym h2 = new Hypernym();
                                            h2.setType(h.getType());
                                            h2.setTypeURL(h.getTypeURL());
                                            h2.setEntity(enInterLangLink.split("/")[enInterLangLink.split("/").length-1].replace("_", " "));
                                            h2.setEntityURL(enInterLangLink);
                                            h2.setAccuracy(h.getAccuracy());
                                            h2.setBounds(h.getBounds());
                                            h2.setStartOffset(h.getStartOffset());
                                            h2.setEndOffset(h.getEndOffset());
                                            h2.setOrigin("thd");
                                            h2.setUnderlyingEntityText(h.getUnderlyingEntityText());
                                            resHypListEnde.add(h2);
                                        }
                                        
                                    }
                                }
                            }
                            resHypList.addAll(resHypListEnde);
                            break;
                            
                        case "nl":
                            
                            HashSet<Hypernym> resHypListEnNl = new HashSet();
                            
                            // Spawning one more entity with just another localized URI
                            for(Hypernym h : resHypList){
                                if(h.getEntityURL().startsWith("http://nl.dbpedia.org/resource/")
                                        && (h.getOrigin().equals("thd")
                                        || h.getOrigin().equals("thd-derived")
                                        )) {
                                    
                                    BasicDBObject queryObj = new BasicDBObject();
                                    queryObj.append("nl_uri", h.getEntityURL());
                                    BasicDBObject projObj = new BasicDBObject();
                                    projObj.append("en_uri", 1);
                                    
                                    DBObject resObj = MongoDBClient.getDBInstance().getCollection("interlanguage_links").findOne(queryObj, projObj);

                                    if(resObj != null) {
                                        
                                        if(resObj.get("en_uri") != null) {
                                            
                                            String enInterLangLink = resObj.get("en_uri").toString();

                                            Hypernym h2 = new Hypernym();
                                            h2.setType(h.getType());
                                            h2.setTypeURL(h.getTypeURL());
                                            h2.setEntity(enInterLangLink.split("/")[enInterLangLink.split("/").length-1].replace("_", " "));
                                            h2.setEntityURL(enInterLangLink);
                                            h2.setAccuracy(h.getAccuracy());
                                            h2.setBounds(h.getBounds());
                                            h2.setStartOffset(h.getStartOffset());
                                            h2.setEndOffset(h.getEndOffset());
                                            h2.setOrigin("thd");
                                            h2.setUnderlyingEntityText(h.getUnderlyingEntityText());

                                            resHypListEnNl.add(h2);
                                        }
                                    }
                                }
                            }
                            resHypList.addAll(resHypListEnNl);

                            break;
                        }                    
                }
        }
        
        // filtering out types
        if(typesFilter.equals("dbo")) {
            
            HashSet dboHypList = new HashSet();
            for(Hypernym h  : resHypList){
                if(h.getTypeURL().contains("ontology")) {
                    dboHypList.add(h);
                }                
            }
            resHypList = dboHypList;
            
        } else if(typesFilter.equals("dbinstance")){
            
            HashSet dbinstanceHypList = new HashSet();
            for(Hypernym h  : resHypList){
                if(h.getTypeURL().contains("resource")) {
                    dbinstanceHypList.add(h);
                }                
            }
            resHypList = dbinstanceHypList;
        
        } else if(typesFilter.equals("all")){
            return new ArrayList<Hypernym>(resHypList);
        }
        
        return new ArrayList<Hypernym>(resHypList);
        
        } catch(Exception ex){
            Logger.getLogger(THDWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public ArrayList<Entity> extractEntityCandidates(String query, String lang, String entity_type) throws ResourceInstantiationException, ExecutionException {
        
        ArrayList<Entity> candidates = null;
        switch (lang) {
            case "en":
                candidates = extractEntityCandidatesEN(query, lang, entity_type);
                break;
            case "de":
                candidates = extractEntityCandidatesDE(query, lang, entity_type);
                break;
            case "nl":
                candidates = extractEntityCandidatesNL(query, lang, entity_type);
                break;
        }

        return candidates;
    }  
    public ArrayList<Entity> extractEntityCandidatesEN(String query, String lang, String entity_type) throws ResourceInstantiationException, ExecutionException{
        Document doc = Factory.newDocument(query);
        doc.setName("Query_Document");
        Corpus corpus = Factory.newCorpus("");
        corpus.add(doc);
        
        enEntityExtractionPipeline.setCorpus(corpus);
        enEntityExtractionPipeline.execute();
            
        Document[] docs = (Document[]) corpus.toArray(new Document[corpus.size()]);
        //System.out.println("EN Entity Extraction pipeline finished");
        ArrayList<Entity> candidates = new ArrayList<Entity>();
            
        Document d = docs[0];
        AnnotationSet as_all = d.getAnnotations();
            
            //System.out.println("Found AS: "+ as_all.size());
            if(entity_type.equals("all") || entity_type.equals("ne")){
                
                AnnotationSet as_named_entity = as_all.get("ne");
                Iterator anot = as_named_entity.iterator();

                while(anot.hasNext()){
                    try {
                        Annotation isaAnnot = (gate.Annotation) anot.next();
                        Node annStart = isaAnnot.getStartNode();
                        Node annEnd = isaAnnot.getEndNode();
                        ////System.out.println("Start offset"+ annStart.getOffset());
                        ////System.out.println("End offset"+ annEnd.getOffset());
                        String content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();
//                        System.out.println("Candidate: " + content);
                        candidates.add(new Entity(content, annStart.getOffset(), annEnd.getOffset(), "named entity"));
                    } catch (InvalidOffsetException ex) {
                        Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(entity_type.equals("all") || entity_type.equals("ce")) {

//                System.out.println("trying to extract common");
                AnnotationSet as_common_nouns = as_all.get("e");
                Iterator cn_anot = as_common_nouns.iterator();

                while(cn_anot.hasNext()){
                    try {
                        Annotation isaAnnot = (gate.Annotation) cn_anot.next();
                        Node annStart = isaAnnot.getStartNode();
                        Node annEnd = isaAnnot.getEndNode();
//                        System.out.println("Start offset"+ annStart.getOffset());
//                        System.out.println("End offset"+ annEnd.getOffset());
                        String content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();
//                        System.out.println("Candidate: " + content);
                        candidates.add(new Entity(content, annStart.getOffset(), annEnd.getOffset(), "common entity"));
                    } catch (InvalidOffsetException ex) {
                        Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        Factory.deleteResource(doc);
        Factory.deleteResource(corpus);
        return candidates;
    }
    public ArrayList<Entity> extractEntityCandidatesDE(String query, String lang, String entity_type) throws ResourceInstantiationException, ExecutionException {
        
//        System.out.println("opa");
        Document doc = Factory.newDocument(query);
            doc.setName("Query_Document");
            Corpus corpus = Factory.newCorpus("");
            corpus.add(doc);
            
            deEntityExtractionPipeline.setCorpus(corpus);        
            deEntityExtractionPipeline.execute();
            
            Document[] docs = (Document[]) corpus.toArray(new Document[corpus.size()]);
            //System.out.println("DE Entity Extraction pipeline finished");
            ArrayList<Entity> candidates = new ArrayList<Entity>();
            
            Document d = docs[0];
            AnnotationSet as_all = d.getAnnotations();
            if(entity_type.equals("all") || entity_type.equals("ne")){
//                System.out.println("Found AS: "+ as_all.size());
                AnnotationSet as_entity = as_all.get("ne");

                Iterator anot = as_entity.iterator();
                while(anot.hasNext()){
                    try {
                        Annotation isaAnnot = (gate.Annotation) anot.next();
                        Node annStart = isaAnnot.getStartNode();
                        Node annEnd = isaAnnot.getEndNode();
                        AnnotationSet as_token = as_all.get("Token",annStart.getOffset(), annEnd.getOffset() );
                        String content = "";
//                        System.out.println("size: " + as_token.size());
                        
                        if (as_token.size()>1 ) {
                            content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();                    
    //                        System.out.println("lemma: " + tok.getFeatures().get("lemma"));
                        }else{
                            Iterator as_token_iter = as_token.iterator();
                            
                            Annotation tok = (gate.Annotation)as_token_iter.next();
                            String lemma = tok.getFeatures().get("lemma").toString();
                                
                            if(!lemma.equals("<unknown>")) {
                                content = lemma;                                
                            } else {
                                content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();                                
                            }
                        }
                        candidates.add(new Entity(d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString(), annStart.getOffset(), annEnd.getOffset(), "named entity"));
                    } catch (InvalidOffsetException ex) {
                        Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(entity_type.equals("all") || entity_type.equals("ce")){
                //System.out.println("Chosen all entities");
                AnnotationSet as_common_entity = as_all.get("e");
                Iterator anot_e = as_common_entity.iterator();
                
                while(anot_e.hasNext()){
                    try {
                        Annotation isaAnnot = (gate.Annotation) anot_e.next();
                        Node annStart = isaAnnot.getStartNode();
                        Node annEnd = isaAnnot.getEndNode();
                        ////System.out.println("Start offset"+ annStart.getOffset());
                        ////System.out.println("End offset"+ annEnd.getOffset());
                        AnnotationSet as_token = as_all.get("Token",annStart.getOffset(), annEnd.getOffset() );
                        
                        String content = "";
//                        System.out.println("size: " + as_token.size());
                        
                        if (as_token.size()>1 ) {
                            content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();                    
    //                        System.out.println("lemma: " + tok.getFeatures().get("lemma"));
                        }else{
                            Iterator as_token_iter = as_token.iterator();
                            
                            Annotation tok = (gate.Annotation)as_token_iter.next();
                            String lemma = tok.getFeatures().get("lemma").toString();
                                
                            if(!lemma.equals("<unknown>")) {
                                content = lemma;                                
                            } else {
                                content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();                                
                            }
                        }
                        //System.out.println("Candidate: " + content);
                        candidates.add(new Entity(d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString(), annStart.getOffset(), annEnd.getOffset(), "common entity"));
                    } catch (InvalidOffsetException ex) {
                        //System.out.println("Problem in the loop 2");
                       Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            //System.out.println("Number of extracted candidates: "+candidates.size());
            Factory.deleteResource(doc);
            Factory.deleteResource(corpus);
            return candidates;

    }    
    public ArrayList<Entity> extractEntityCandidatesNL(String query, String lang, String entity_type) throws ResourceInstantiationException, ExecutionException{
        
            Document doc = Factory.newDocument(query);
            doc.setName("Query_Document");
            Corpus corpus = Factory.newCorpus("");
            corpus.add(doc);
            
            nlEntityExtractionPipeline.setCorpus(corpus);        
            nlEntityExtractionPipeline.execute();
            
            Document[] docs = (Document[]) corpus.toArray(new Document[corpus.size()]);
            //System.out.println("NL Entity Extraction pipeline finished");
            ArrayList<Entity> candidates = new ArrayList<Entity>();
            
            Document d = docs[0];
            AnnotationSet as_all = d.getAnnotations();
            
            //System.out.println("Found AS: "+ as_all.size());
            if(entity_type.equals("all") || entity_type.equals("ne")) {
                
                AnnotationSet as_entity = as_all.get("ne");
                Iterator anot = as_entity.iterator();
                while(anot.hasNext()){
                    try {
                        Annotation isaAnnot = (gate.Annotation) anot.next();
                        Node annStart = isaAnnot.getStartNode();
                        Node annEnd = isaAnnot.getEndNode();
                        ////System.out.println("Start offset"+ annStart.getOffset());
                        ////System.out.println("End offset"+ annEnd.getOffset());
                        String content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();
                        //System.out.println("Candidate: " + content);
                        candidates.add(new Entity(content, annStart.getOffset(), annEnd.getOffset(), "named entity"));
                    } catch (InvalidOffsetException ex) {
                        //System.out.println("Problem in the loop 1");
                        Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(entity_type.equals("all") || entity_type.equals("ce")){
                AnnotationSet as_common_entity = as_all.get("e");
                Iterator anot_e = as_common_entity.iterator();
                while(anot_e.hasNext()){
                    try {
                        Annotation isaAnnot = (gate.Annotation) anot_e.next();
                        Node annStart = isaAnnot.getStartNode();
                        Node annEnd = isaAnnot.getEndNode();
                        ////System.out.println("Start offset"+ annStart.getOffset());
                        ////System.out.println("End offset"+ annEnd.getOffset());
                        String content = d.getContent().getContent(annStart.getOffset(), annEnd.getOffset()).toString();
                        //System.out.println("Candidate: " + content);
                        candidates.add(new Entity(content, annStart.getOffset(), annEnd.getOffset(), "common entity"));
                    } catch (InvalidOffsetException ex) {
                        //System.out.println("Problem in the loop 2");
                       Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            //System.out.println("Number of extracted candidates: "+candidates.size());
            Factory.deleteResource(doc);
            Factory.deleteResource(corpus);
            return candidates;

    }

    private void assamblePipelines() {        
        assambleEntityExtractionPipelineEN();
        assambleEntityExtractionPipelineDE();
        assambleEntityExtractionPipelineNL();
        assambleHypernymExtractionPipelineEN();
        assambleHypernymExtractionPipelineDE();
        assambleHypernymExtractionPipelineNL();
    }
    private void assambleEntityExtractionPipelineEN() {
        
        try {
            //System.out.println("Initialization of English Entity extraction pipeline...");
//            System.out.println("Grammar loc: " + Settings.EN_ENTITY_EXTRACTION_GRAMMAR);
            URL url = THDController.getInstance().getClass().getResource(Settings.EN_ENTITY_EXTRACTION_GRAMMAR);
            File japeOrigFile = new File(url.getFile());
            java.net.URI japeURI = japeOrigFile.toURI();
            FeatureMap transducerFeatureMap = Factory.newFeatureMap();

            try {
                transducerFeatureMap.put("grammarURL", japeURI.toURL());
                transducerFeatureMap.put("encoding", "UTF-8");
            } catch (MalformedURLException e) {
                //System.out.println("Malformed URL of JAPE grammar");
                //System.out.println(e.toString());
            }

            FeatureMap tokenizerFeatureMap = Factory.newFeatureMap();        
            ProcessingResource tokenizerPR = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", tokenizerFeatureMap);

            FeatureMap sentenceSplitterFeatureMap = Factory.newFeatureMap();
            ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter", sentenceSplitterFeatureMap);

            FeatureMap posTaggerFeatureMap = Factory.newFeatureMap();
            ProcessingResource posTaggerPR = (ProcessingResource) Factory.createResource("gate.creole.POSTagger", posTaggerFeatureMap);

            ProcessingResource japeCandidatesPR = (ProcessingResource) Factory.createResource("gate.creole.Transducer", transducerFeatureMap);

            enEntityExtractionPipeline = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController");

            enEntityExtractionPipeline.add(tokenizerPR);
            enEntityExtractionPipeline.add(sentenceSplitterPR);
            enEntityExtractionPipeline.add(posTaggerPR);
            enEntityExtractionPipeline.add(japeCandidatesPR);
        } catch (ResourceInstantiationException ex) {;
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void assambleEntityExtractionPipelineDE(){
        try {
                    
            //System.out.println("Initializing DE pipeline");

            FeatureMap resetFeatureMap = Factory.newFeatureMap();
            ProcessingResource resetPR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", resetFeatureMap);
                    
            FeatureMap tokenizerFeatureMap = Factory.newFeatureMap();
            ProcessingResource tokenizerPR = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", tokenizerFeatureMap);
                    
            FeatureMap taggerFeatureMap = Factory.newFeatureMap();
            taggerFeatureMap.put("debug", "false");
            taggerFeatureMap.put("encoding", "UTF-8");
            taggerFeatureMap.put("failOnUnmappableCharacter", false);
            taggerFeatureMap.put("featureMapping", "lemma=3;category=2;string=1");
            taggerFeatureMap.put("inputAnnotationType", "Token");
            taggerFeatureMap.put("inputTemplate", "${string}");
            taggerFeatureMap.put("outputAnnotationType", "Token");
            taggerFeatureMap.put("regex", "(.+)	(.+)	(.+)");
            taggerFeatureMap.put("taggerBinary", Settings.DE_TAGGER_BINARY);
            taggerFeatureMap.put("updateAnnotations", true);
                    
            ProcessingResource genTag = (ProcessingResource) Factory.createResource("gate.taggerframework.GenericTagger",taggerFeatureMap);
                    
            //JAPE Entity Extraction grammar
            URL url =  THDController.getInstance().getClass().getResource(Settings.DE_ENTITY_EXTRACTION_GRAMMAR);
            
            //System.out.println(url);
            File japeOrigFile = new File(url.getFile());
            java.net.URI japeURI = japeOrigFile.toURI();
            FeatureMap transducerFeatureMap = Factory.newFeatureMap();

            try {
                transducerFeatureMap.put("grammarURL", japeURI.toURL());
                transducerFeatureMap.put("encoding", "UTF-8");
            } catch (MalformedURLException e) {
                //System.out.println("Malformed URL of JAPE grammar");
                //System.out.println(e.toString());
            }
                    
            ProcessingResource japeCandidatesPR = (ProcessingResource) Factory.createResource("gate.creole.Transducer", transducerFeatureMap);

            deEntityExtractionPipeline = (SerialAnalyserController) Factory.createResource("gate.creole.RealtimeCorpusController");
            deEntityExtractionPipeline.add(resetPR);
            deEntityExtractionPipeline.add(tokenizerPR);
            deEntityExtractionPipeline.add(genTag);                    
            deEntityExtractionPipeline.add(japeCandidatesPR);
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    private void assambleEntityExtractionPipelineNL(){        
        try {
        
            //System.out.println("First time running NL pipeline");
            //System.out.println(nlEntityExtractionGrammar);

            FeatureMap resetFeatureMap = Factory.newFeatureMap();
            ProcessingResource resetPR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", resetFeatureMap);
                    
            FeatureMap tokenizerFeatureMap = Factory.newFeatureMap();
            ProcessingResource tokenizerPR = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", tokenizerFeatureMap);
                    
            FeatureMap taggerFeatureMap = Factory.newFeatureMap();
            taggerFeatureMap.put("debug", "false");
            taggerFeatureMap.put("encoding", "UTF-8");
            taggerFeatureMap.put("failOnUnmappableCharacter", false);
//            taggerFeatureMap.put("failOnMissingInputAnnotations", false);
            taggerFeatureMap.put("featureMapping", "lemma=3;category=2;string=1");
            taggerFeatureMap.put("inputAnnotationType", "Token");
            taggerFeatureMap.put("inputTemplate", "${string}");
            taggerFeatureMap.put("outputAnnotationType", "Token");
            taggerFeatureMap.put("regex", "(.+)	(.+)	(.+)");
            taggerFeatureMap.put("taggerBinary", Settings.NL_TAGGER_BINARY);
            taggerFeatureMap.put("updateAnnotations", true);
                    
            ProcessingResource genTag = (ProcessingResource) Factory.createResource("gate.taggerframework.GenericTagger",taggerFeatureMap);
                    
            //JAPE Entity Extraction grammar
            URL url =  THDController.getInstance().getClass().getResource(Settings.NL_ENTITY_EXTRACTION_GRAMMAR);
            File japeOrigFile = new File(url.getFile());
            java.net.URI japeURI = japeOrigFile.toURI();
            FeatureMap transducerFeatureMap = Factory.newFeatureMap();

            try {
                transducerFeatureMap.put("grammarURL", japeURI.toURL());
                transducerFeatureMap.put("encoding", "UTF-8");
            } catch (MalformedURLException e) {
                //System.out.println("Malformed URL of JAPE grammar");
                //System.out.println(e.toString());
            }
                    
            ProcessingResource japeCandidatesPR = (ProcessingResource) Factory.createResource("gate.creole.Transducer", transducerFeatureMap);

            nlEntityExtractionPipeline = (SerialAnalyserController) Factory.createResource("gate.creole.RealtimeCorpusController");
            nlEntityExtractionPipeline.add(resetPR);
            nlEntityExtractionPipeline.add(tokenizerPR);
            nlEntityExtractionPipeline.add(genTag);                    
            nlEntityExtractionPipeline.add(japeCandidatesPR);
            
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }    
    private void assambleHypernymExtractionPipelineEN(){
        
        try {
            
            hypernymExtractionPipelineEN = (SerialAnalyserController) Factory.createResource("gate.creole.RealtimeCorpusController");
            
            FeatureMap resetFeatureMap = Factory.newFeatureMap();
            ProcessingResource resetPR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", resetFeatureMap);
            hypernymExtractionPipelineEN.add(resetPR);
        
            FeatureMap tokenizerFeatureMap = Factory.newFeatureMap();
            ProcessingResource tokenizerPR = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", tokenizerFeatureMap);
            hypernymExtractionPipelineEN.add(tokenizerPR);
                
            FeatureMap sentenceSplitterFeatureMap = Factory.newFeatureMap();
            ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter", sentenceSplitterFeatureMap);
            //ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.RegexSentenceSplitter", sentenceSplitterFeatureMap);
            hypernymExtractionPipelineEN.add(sentenceSplitterPR);

            FeatureMap posTaggerFeatureMap = Factory.newFeatureMap();
            ProcessingResource posTaggerPR = (ProcessingResource) Factory.createResource("gate.creole.POSTagger", posTaggerFeatureMap);                
            hypernymExtractionPipelineEN.add(posTaggerPR);

            URL url = THDController.getInstance().getClass().getResource(Settings.EN_HYPERNYM_EXTRACTION_GRAMMAR);
            File japeOrigFile = new File(url.getFile());            
            java.net.URI japeURI = japeOrigFile.toURI();
            
            FeatureMap transducerFeatureMap = Factory.newFeatureMap();
            try {
                transducerFeatureMap.put("grammarURL", japeURI.toURL());
                transducerFeatureMap.put("encoding", "UTF-8");
            } catch (MalformedURLException e) {
                // TODO
            }
            ProcessingResource japeCandidatesPR = (ProcessingResource) Factory.createResource("gate.creole.Transducer", transducerFeatureMap);

            hypernymExtractionPipelineEN.add(japeCandidatesPR);
            
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    private void assambleHypernymExtractionPipelineDE(){
        
        try {
            
            hypernymExtractionPipelineDE = (SerialAnalyserController) Factory.createResource("gate.creole.RealtimeCorpusController");
            
            FeatureMap resetFeatureMap = Factory.newFeatureMap();
            ProcessingResource resetPR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", resetFeatureMap);
            hypernymExtractionPipelineDE.add(resetPR);
        
            FeatureMap tokenizerFeatureMap = Factory.newFeatureMap();
            ProcessingResource tokenizerPR = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", tokenizerFeatureMap);
            hypernymExtractionPipelineDE.add(tokenizerPR);
                
            FeatureMap sentenceSplitterFeatureMap = Factory.newFeatureMap();
            ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter", sentenceSplitterFeatureMap);
            //ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.RegexSentenceSplitter", sentenceSplitterFeatureMap);
            hypernymExtractionPipelineDE.add(sentenceSplitterPR);

            FeatureMap taggerFeatureMap = Factory.newFeatureMap();
            taggerFeatureMap.put("debug", "false");
            taggerFeatureMap.put("encoding", "UTF-8");
            taggerFeatureMap.put("failOnUnmappableCharacter", "false");
            taggerFeatureMap.put("featureMapping", "lemma=3;category=2;string=1");
            taggerFeatureMap.put("inputAnnotationType", "Token");
            taggerFeatureMap.put("inputTemplate", "${string}");
            taggerFeatureMap.put("outputAnnotationType", "Token");
            taggerFeatureMap.put("regex", "(.+)	(.+)	(.+)");
            taggerFeatureMap.put("taggerBinary", Settings.DE_TAGGER_BINARY);
            taggerFeatureMap.put("updateAnnotations", false);
                    
            ProcessingResource genTag = (ProcessingResource) Factory.createResource("gate.taggerframework.GenericTagger",taggerFeatureMap);
            hypernymExtractionPipelineDE.add(genTag);

            URL url = THDController.getInstance().getClass().getResource(Settings.DE_HYPERNYM_EXTRACTION_GRAMMAR);
            File japeOrigFile = new File(url.getFile());            
            java.net.URI japeURI = japeOrigFile.toURI();
            
            FeatureMap transducerFeatureMap = Factory.newFeatureMap();
            try {
                transducerFeatureMap.put("grammarURL", japeURI.toURL());
                transducerFeatureMap.put("encoding", "UTF-8");
            } catch (MalformedURLException e) {
                //System.out.println("Malformed URL of JAPE grammar");
                System.out.println("PROBLEM 1 " + e.toString());
            }
            
            ProcessingResource japeCandidatesPR = (ProcessingResource) Factory.createResource("gate.creole.Transducer", transducerFeatureMap);
            hypernymExtractionPipelineDE.add(japeCandidatesPR);
            
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void assambleHypernymExtractionPipelineNL(){
        
        try {
            
            hypernymExtractionPipelineNL = (SerialAnalyserController) Factory.createResource("gate.creole.RealtimeCorpusController");
            
            FeatureMap resetFeatureMap = Factory.newFeatureMap();
            ProcessingResource resetPR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", resetFeatureMap);
            hypernymExtractionPipelineNL.add(resetPR);
        
            FeatureMap tokenizerFeatureMap = Factory.newFeatureMap();
            ProcessingResource tokenizerPR = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", tokenizerFeatureMap);
            hypernymExtractionPipelineNL.add(tokenizerPR);
                
            FeatureMap sentenceSplitterFeatureMap = Factory.newFeatureMap();
            ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter", sentenceSplitterFeatureMap);
            //ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.RegexSentenceSplitter", sentenceSplitterFeatureMap);
            hypernymExtractionPipelineNL.add(sentenceSplitterPR);

            FeatureMap taggerFeatureMap = Factory.newFeatureMap();
            taggerFeatureMap.put("debug", "false");
            taggerFeatureMap.put("encoding", "UTF-8");
            taggerFeatureMap.put("failOnUnmappableCharacter", "false");
            taggerFeatureMap.put("featureMapping", "lemma=3;category=2;string=1");
            taggerFeatureMap.put("inputAnnotationType", "Token");
            taggerFeatureMap.put("inputTemplate", "${string}");
            taggerFeatureMap.put("outputAnnotationType", "Token");
            taggerFeatureMap.put("regex", "(.+)	(.+)	(.+)");
            taggerFeatureMap.put("taggerBinary", Settings.NL_TAGGER_BINARY);
            taggerFeatureMap.put("updateAnnotations", false);
                    
            ProcessingResource genTag = (ProcessingResource) Factory.createResource("gate.taggerframework.GenericTagger",taggerFeatureMap);
            hypernymExtractionPipelineNL.add(genTag);            

//            URL url = new URL("file:/Users/Milan/Documents/research/repositories/linked-tv/code/thd-v04/THD-lib-v04/src/resources/nl_hearst_v2.jape");
            URL url = THDController.getInstance().getClass().getResource(Settings.NL_HYPERNYM_EXTRACTION_GRAMMAR);
            File japeOrigFile = new File(url.getFile());            
            java.net.URI japeURI = japeOrigFile.toURI();
            
            FeatureMap transducerFeatureMap = Factory.newFeatureMap();
            try {
                transducerFeatureMap.put("grammarURL", japeURI.toURL());
                transducerFeatureMap.put("encoding", "UTF-8");
            } catch (MalformedURLException e) {
                //System.out.println("Malformed URL of JAPE grammar");
            }
            ProcessingResource japeCandidatesPR = (ProcessingResource) Factory.createResource("gate.creole.Transducer", transducerFeatureMap);

            hypernymExtractionPipelineNL.add(japeCandidatesPR);
            
        } catch (Exception ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashSet<Hypernym> extractEntityTypes(String entity, String lang, String kb, String[] provenance) throws UnknownHostException{
//        System.out.println("entity: " + entity);
        HashSet<Hypernym> hypernymsList = new HashSet();
        HashSet<Hypernym> thdHypernyms = new HashSet();
        HashSet<Hypernym> dbpediaHypernyms = new HashSet();
        HashSet<Hypernym> yagoHypernyms = new HashSet();
        
        switch (lang) {
            case "en":
                for(String prov : provenance) {
                    
                    if(prov.equals("thd")) {
                        
                        if(kb.equals("linkedHypernymsDataset")) {
                            thdHypernyms = extractEntityTypesLHD(entity, lang);
                        } else {
                            thdHypernyms = extractEntityTypesEN(entity, kb);
                        }
                        
                    } else if(prov.equals("dbpedia")) {
                        dbpediaHypernyms = getDBpediaHypernyms(entity, lang);
                    } else if(prov.equals("yago")) {
                        yagoHypernyms = getYAGOHypernyms(entity, lang);
                    }
                }
                if(dbpediaHypernyms != null){
                    hypernymsList.addAll(dbpediaHypernyms);
                }

                if(yagoHypernyms != null){
                    hypernymsList.addAll(yagoHypernyms);
                }

                if(thdHypernyms != null) {
                    hypernymsList.addAll(thdHypernyms);
                }
                return hypernymsList;
                
            case "de":
                
                for(String prov : provenance) {
                    if(prov.equals("thd")) {
                        
                        if(kb.equals("linkedHypernymsDataset")){
                            thdHypernyms = extractEntityTypesLHD(entity, lang);
                        } else {
                            thdHypernyms = extractEntityTypesDE(entity, kb);
                        }
                        
                    } else if(prov.equals("dbpedia")){
                        HashSet hs = new HashSet();
                        HashSet tmp1 = getDBpediaHypernyms(entity, lang);
                        HashSet tmp2 = getDBpediaHypernyms(entity, "en");
                        hs.addAll(tmp1);
                        hs.addAll(tmp2);
                        dbpediaHypernyms.addAll(hs);
                    } else if(prov.equals("yago")) {
                        HashSet hs = new HashSet();
                        HashSet tmp = getYAGOHypernyms(entity, lang);
                        hs.addAll(tmp);
                        
                        String s = getInterlanguageLink(entity, lang, "en");                        
                        if(s != null) {
                            HashSet tmp2 = getYAGOHypernyms(s, "en");
                            hs.addAll(tmp2);
                            tmp.addAll(tmp2);                            
                        }
                        yagoHypernyms.addAll(hs);
                    }
                }
                if(dbpediaHypernyms != null){
                    hypernymsList.addAll(dbpediaHypernyms);
                }

                if(yagoHypernyms != null){
                    hypernymsList.addAll(yagoHypernyms);
                }

                if(thdHypernyms != null) {
                    hypernymsList.addAll(thdHypernyms);
                }
                return hypernymsList;
                
            case "nl":                
                
                for(String prov : provenance){
                    if(prov.equals("thd")){
                        if(kb.equals("linkedHypernymsDataset")){
                            thdHypernyms = extractEntityTypesLHD(entity, lang);
                        }else{
                            thdHypernyms = extractEntityTypesNL(entity, kb);
                        }
                    } else if(prov.equals("dbpedia")) {
                        HashSet hs = new HashSet();
                        HashSet tmp1 = getDBpediaHypernyms(entity, lang);
                        HashSet tmp2 = getDBpediaHypernyms(entity, "en");
                        hs.addAll(tmp1);
                        hs.addAll(tmp2);

                        dbpediaHypernyms.addAll(hs);
                        
                    } else if(prov.equals("yago")){
                        HashSet hs = new HashSet();
                        yagoHypernyms = getYAGOHypernyms(entity, lang);
                        String s = getInterlanguageLink(entity, lang, "en");
                        if(s != null) {
                            HashSet tmp = getYAGOHypernyms(s, "en");
                            hs.addAll(tmp);
                        }
                        
                        yagoHypernyms.addAll(hs);                        
                    }
                }
                if(dbpediaHypernyms != null){
                    hypernymsList.addAll(dbpediaHypernyms);
                }

                if(yagoHypernyms != null){
                    hypernymsList.addAll(yagoHypernyms);
                }

                if(thdHypernyms != null) {
                    hypernymsList.addAll(thdHypernyms);
                }
                return hypernymsList;
        }
        return hypernymsList;
    }
    public HashSet extractEntityTypesEN(String entity, String kb) {
                
        HashSet hypernymsList = new HashSet();
        try {
            URL url = null;
            String path = "";
            if(kb.equals("local")){
                path = Settings.EN_WIKIPEDIA_LOCAL_EXPORT+entity.replace(" ", "_");
            }else if(kb.equals("live")){
                path = Settings.EN_WIKIPEDIA_LIVE_EXPORT+entity.replace(" ", "_");            
            }
//            System.out.println(path);
            StringBuffer buffer = new StringBuffer();
            url = new URL(path);
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            Reader isr = new InputStreamReader(is,"UTF-8");
            Reader in = new BufferedReader(isr);
            int ch;
            
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            in.close();
            Article article = new Article(buffer.toString(),"en");
            Document document = Factory.newDocument(article.getFirstSection());

            Corpus corpus = Factory.newCorpus("");
            corpus.add(document);
            hypernymExtractionPipelineEN.setCorpus(corpus);
            hypernymExtractionPipelineEN.execute();
            
            Iterator corpus_iter = corpus.iterator();
            
            while (corpus_iter.hasNext()) {
                Document doc = (Document)corpus_iter.next();
                AnnotationSet as_all = doc.getAnnotations();
                AnnotationSet as_hearst = as_all.get("h");
                Iterator ann_iter = as_hearst.iterator();
                //System.out.println(as_hearst.size());
                
                while (ann_iter.hasNext()) {
                    
                    Annotation isaAnnot = (gate.Annotation) ann_iter.next();
                    Node isaStart = isaAnnot.getStartNode();                               
                    Node isaEnd = isaAnnot.getEndNode();
                    String hypernym = doc.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
                    //System.out.println("HYPERNYM " + hypernym);
                    
                    Hypernym hypObj = new Hypernym();
                    hypObj.setEntity(entity);
                    hypObj.setEntityURL("http://dbpedia.org/resource/" + entity.replace(" ", "_"));
                    hypObj.setType(hypernym.substring(0, 1).toUpperCase() + hypernym.substring(1));
                    hypObj.setOrigin("thd");
                    hypObj.setAccuracy("0.85");
                    hypObj.setBounds("+- 2.5%");
                    RedisClient redis = RedisClient.getInstance();
                    
                    LinkedEntity linkedEntity = null;
                    
                    String linkedEntityStr = null;
                    linkedEntityStr = redis.getValue(hypernym + "en" + kb);
                    
                    if( linkedEntityStr == null) {
//                        System.out.println("other: hypernym page not in cache");
                        linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(hypernym, "en", kb);
                        if(linkedEntity != null) {
                            redis.setKey(hypernym + "en" + kb, linkedEntity.toString());
                        }                        
                    } else {
                        linkedEntity = new LinkedEntity();
                        linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                        linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));
//                        System.out.println("other: hypernym page in cache");
                    }
                    if(linkedEntity != null){
                        hypObj.setTypeURL("http://dbpedia.org/resource/" + linkedEntity.getPageTitle().replace(" ", "_"));                    
                    } else {
                        hypObj.setTypeURL("");                        
                    }
                    hypernymsList.add(hypObj);
                }
            }
            
        } catch (InvalidOffsetException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hypernymsList;
    }
    
    public HashSet extractEntityTypesDE(String entity, String kb) {
//        System.out.println("TEST: "  + kb);
        HashSet hypernymsList = new HashSet();
        try {
            URL url = null;
            String path = "";
            if(kb.equals("local")){
                path = Settings.DE_WIKIPEDIA_LOCAL_EXPORT + URLEncoder.encode(entity.replace(" ", "_"), "UTF-8");
            }else if(kb.equals("live")){
                path = Settings.DE_WIKIPEDIA_LIVE_EXPORT + URLEncoder.encode(entity.replace(" ", "_"), "UTF-8");           
            }
//            System.out.println(path);

            StringBuffer buffer = new StringBuffer();
            url = new URL(path);
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            Reader isr = new InputStreamReader(is,"UTF-8");
            Reader in = new BufferedReader(isr);
            int ch;
            
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            
            in.close();
            Article article = new Article(buffer.toString(),"en");
            Document document = Factory.newDocument(article.getFirstSection());
            Corpus corpus = Factory.newCorpus("");
            corpus.add(document);
                        
            hypernymExtractionPipelineDE.setCorpus(corpus);
            hypernymExtractionPipelineDE.execute();
            
            Iterator corpus_iter = corpus.iterator();
            
            while (corpus_iter.hasNext()) {
                Document doc = (Document)corpus_iter.next();
                AnnotationSet as_all = doc.getAnnotations();
                AnnotationSet as_hearst = as_all.get("h");
                Iterator ann_iter = as_hearst.iterator();

                while (ann_iter.hasNext()) {
                    Annotation isaAnnot = (gate.Annotation) ann_iter.next();
                    Node isaStart = isaAnnot.getStartNode();                               
                    Node isaEnd = isaAnnot.getEndNode();
                    String hypernym = doc.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
                    //System.out.println("HYPERNYM " + hypernym);
                    
                    Hypernym hypObj = new Hypernym();
                    hypObj.setEntity(entity);
                    hypObj.setEntityURL("http://de.dbpedia.org/resource/" + entity.replace(" ", "_"));
                    hypObj.setType(hypernym.substring(0, 1).toUpperCase() + hypernym.substring(1));
                    RedisClient redis = RedisClient.getInstance();
                    
                    LinkedEntity linkedEntity = null;
                    
                    String linkedEntityStr = null;
                    linkedEntityStr = redis.getValue(hypernym + "de" + kb);
                    if(linkedEntityStr == null) {
//                        System.out.println("other: hypernym page not in cache");
                        linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(hypernym, "de", kb);
                        if(linkedEntity != null) {
                            redis.setKey(hypernym + "de" + kb, linkedEntity.toString());
                        }
                    } else {
                        linkedEntity = new LinkedEntity();
                        linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                        linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));
//                        System.out.println("other: hypernym page in cache");                    
                    }
                    if(linkedEntity != null){
                        hypObj.setTypeURL("http://de.dbpedia.org/resource/" + linkedEntity.getPageTitle().replace(" ", "_"));                    
                    } else {
                        hypObj.setTypeURL("");                        
                    }
                    hypObj.setOrigin("thd");
                    hypObj.setAccuracy("0.77");
                    hypObj.setBounds("+- 2.5%");
                   hypernymsList.add(hypObj);
//                    return hypObj;
                }
            }
            
        } catch (InvalidOffsetException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hypernymsList;
    }
    public HashSet extractEntityTypesNL(String entity, String kb) {
        HashSet hypernymsList = new HashSet();
        try {
            URL url = null;
            String path = "";
            if(kb.equals("local")){
                path = Settings.NL_WIKIPEDIA_LOCAL_EXPORT + URLEncoder.encode(entity.replace(" ", "_"), "UTF-8");
            }else if(kb.equals("live")){
                path = Settings.NL_WIKIPEDIA_LIVE_EXPORT + URLEncoder.encode(entity.replace(" ", "_"), "UTF-8");          
            }
//            System.out.println("path: " + path);
            StringBuffer buffer = new StringBuffer();
            url = new URL(path);
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            Reader isr = new InputStreamReader(is,"UTF-8");
            Reader in = new BufferedReader(isr);
            int ch;
            
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            in.close();
            Article article = new Article(buffer.toString(),"nl");

            Document document = Factory.newDocument(article.getFirstSection());
            Corpus corpus = Factory.newCorpus("");
            corpus.add(document);
            hypernymExtractionPipelineNL.setCorpus(corpus);
            hypernymExtractionPipelineNL.execute();
            
            Iterator corpus_iter = corpus.iterator();
            
            while (corpus_iter.hasNext()) {
                Document doc = (Document)corpus_iter.next();
                AnnotationSet as_all = doc.getAnnotations();
                AnnotationSet as_hearst = as_all.get("h");
                Iterator ann_iter = as_hearst.iterator();
//                System.out.println(as_hearst.size());
                while (ann_iter.hasNext()) {
                    Annotation isaAnnot = (gate.Annotation) ann_iter.next();
                    Node isaStart = isaAnnot.getStartNode();                               
                    Node isaEnd = isaAnnot.getEndNode();
                    String hypernym = doc.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
//                    System.out.println("HYPERNYM " + hypernym);
                    
                    Hypernym hypObj = new Hypernym();
                    hypObj.setEntity(entity);
                    hypObj.setEntityURL("http://nl.dbpedia.org/resource/" + entity.replace(" ", "_"));
                    hypObj.setType(hypernym.substring(0, 1).toUpperCase() + hypernym.substring(1));
                    RedisClient redis = RedisClient.getInstance();
                    
                    LinkedEntity linkedEntity = null;
                    
                    String linkedEntityStr = null;
                    linkedEntityStr = redis.getValue(hypernym + "nl" + kb);
                    
                    if(linkedEntityStr == null) {
//                        System.out.println("other: hypernym page not in cache");
                        linkedEntity = LuceneSearch.getInstance().findWikipediaArticle(hypernym, "nl", kb);
                        if(linkedEntity != null) {
                            redis.setKey(hypernym + "nl" + kb, linkedEntity.toString());
                        }
                    } else {
                        linkedEntity = new LinkedEntity();
                        linkedEntity.setPageTitle(linkedEntityStr.split("\\+")[0]);
                        linkedEntity.setConfidence(Double.parseDouble(linkedEntityStr.split("\\+")[1]));
//                        System.out.println("other: hypernym page in cache");                    
                    }
                    if(linkedEntity != null){
                        hypObj.setTypeURL("http://nl.dbpedia.org/resource/" + linkedEntity.getPageTitle().replace(" ", "_"));                    
                    } else {
                        hypObj.setTypeURL("");                        
                    }
                    hypObj.setOrigin("thd");
                    hypObj.setAccuracy("0.89");
                    hypObj.setBounds("+- 2%");
                    hypernymsList.add(hypObj);
                }
            }
            
        } catch (InvalidOffsetException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(THDInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hypernymsList;
    }
    
    public HashSet<Hypernym> extractEntityTypesLHD(String entityTitle, String lang) throws UnknownHostException {
        
        HashSet<Hypernym> thdHypernyms = null;
        thdHypernyms = getTHDHypernymsLHDv2(entityTitle, lang);
        
        if(lang.contains("de") || lang.contains("nl")) {
            String s = getInterlanguageLink(entityTitle, lang, "en");
            if(s != null) {
                
                HashSet tmp = getTHDHypernymsLHDv2(s, "en");
//                System.out.println("passed 1 " + tmp.size());
                if(tmp != null) {
//                    Hypernym h = new Hypernym();
//                    h.setEntity("Netherlands");
//                    h.setEntityURL("http://dbpedia.org/resource/Netherlands");
//                    h.setType("Country");
//                    h.setTypeURL("http://dbpedia.org/resource/Country");
//                    h.setAccuracy("14");
//                    h.setBounds("+- 1");
//                    h.setOrigin("thd");
//                    h.setStartOffset(20L);
//                    h.setEndOffset(31L);
//                    h.setUnderlyingEntityText("some shit");
//                    thdHypernyms.add(h);
//                    System.out.println("try 1");
                    try {
                        thdHypernyms.addAll(tmp);
                    } catch(Exception ex) {
                        Logger.getLogger(THDWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
//                    System.out.println("null for " + entityTitle);
                }            
            }
        }
        
        return thdHypernyms;
    }    
    public String getInterlanguageLink(String entityTitle, String langOrig, String langTo) {
        
        try {
            BasicDBObject queryObj = new BasicDBObject();
            switch (langOrig) {
                case "en":
                    queryObj.append("en_uri", "http://dbpedia.org/resource/"+entityTitle.replace(" ", "_"));
                    break;
                    
                case "de":
                    queryObj.append("de_uri", "http://de.dbpedia.org/resource/"+entityTitle.replace(" ", "_"));
                    break;

                case "nl":
                    queryObj.append("nl_uri", "http://nl.dbpedia.org/resource/"+entityTitle.replace(" ", "_"));
                    break;
                    
            }
            BasicDBObject projObj = new BasicDBObject();
            switch (langTo) {
                case "en":
                    projObj.append("en_uri", 1);
                    break;
                    
                case "de":
                    projObj.append("de_uri", 1);
                    break;

                case "nl":
                    projObj.append("nl_uri", 1);
                    break;
            }

            DBObject resObj = MongoDBClient.getDBInstance().getCollection("interlanguage_links").findOne(queryObj, projObj);
            String otherEntityTitle = null;
            if(resObj != null) {
                switch (langTo) {
                    case "en":
                        Object temp = resObj.get("en_uri");
                        if(temp!=null){
                            otherEntityTitle = resObj.get("en_uri").toString();
                        }
                        break;

                    case "de":
                        otherEntityTitle = resObj.get("de_uri").toString();
                        break;

                    case "nl":
                        otherEntityTitle = resObj.get("nl_uri").toString();
                        break;
                }
            }
            if(otherEntityTitle != null){
                otherEntityTitle = otherEntityTitle.substring( otherEntityTitle.lastIndexOf('/')+1, otherEntityTitle.length()).replace("_", " ");
            }
            return otherEntityTitle;
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(THDWorker.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
        
    public HashSet getTHDHypernymsLHDv2(String entityTitle, String lang) throws UnknownHostException {
        
        HashSet hypernymsList = new HashSet();
        try {
            BasicDBObject queryObj = new BasicDBObject();
//            System.out.println("Searching hypernym for entity: " + entityTitle);
            queryObj.append("types", new BasicDBObject().append("$elemMatch", new BasicDBObject().append("origin", "thd")));
            
            switch (lang) {
                case "en":
                    String encodedURIEn = "http://dbpedia.org/resource/"+URLEncoder.encode(URLEncoder.encode(entityTitle.replace(" ", "_"), "UTF-8"), "UTF-8");
                    String notEncodedURIEn = "http://dbpedia.org/resource/"+entityTitle.replace(" ", "_");
                    queryObj.append("uri", encodedURIEn);
                    
                    DBObject resObj = MongoDBClient.getDBInstance().getCollection("en_entities_thd_lhd10").findOne(queryObj);
                    
                    if(resObj != null) {
                        
                        BasicDBList e = (BasicDBList)resObj.get("types");
                        
                        for(int i = 0; i < e.size(); i++) {
                            
                            DBObject type = (DBObject)e.get(i);
                            
                            String mappingStr = type.get("mapping").toString();
                                                        
                            // hypernym is mapped to dbpedia ontology
                            // creating hierarchy from the dbpedia ontology
                            if(mappingStr.equals("dbOnto")) {
                                
                                Hypernym hypernym = new Hypernym();
                                hypernym.setEntity(entityTitle);
                                hypernym.setEntityURL(notEncodedURIEn);
                                hypernym.setType(type.get("label").toString());
                                hypernym.setTypeURL(type.get("uri").toString());
                                hypernym.setOrigin(type.get("origin").toString());
                                hypernym.setAccuracy(type.get("accuracy").toString());
                                hypernym.setBounds(type.get("bounds").toString());
                                hypernymsList.add(hypernym);
                                
                                OntoRecord initRecord = new OntoRecord();
                                initRecord.setUri(type.get("uri").toString());
                                
                                while(initRecord != null){

                                    initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                    if(initRecord == null) {
                                        return hypernymsList;
                                    } else {
                                        Hypernym hypernymDerived = new Hypernym();
                                        hypernymDerived.setEntity(entityTitle);
                                        hypernymDerived.setEntityURL(notEncodedURIEn);
                                        hypernymDerived.setType(initRecord.getLabel());
                                        hypernymDerived.setTypeURL(initRecord.getUri());
                                        hypernymDerived.setOrigin("thd-derived");
                                        hypernymDerived.setAccuracy(type.get("accuracy").toString());
                                        hypernymDerived.setBounds(type.get("bounds").toString());
                                        hypernymsList.add(hypernymDerived);
                                    }
                                }                            
                            }
                            // the type is DBpedia instance, doesn't matter, add it it to the types list
                            else {
                                Hypernym hypernymInst = new Hypernym();
                                hypernymInst.setEntity(entityTitle);
                                hypernymInst.setEntityURL(notEncodedURIEn);
                                hypernymInst.setType(type.get("label").toString());
                                hypernymInst.setTypeURL(type.get("uri").toString());
                                hypernymInst.setOrigin(type.get("origin").toString());
                                hypernymInst.setAccuracy(type.get("accuracy").toString());
                                hypernymInst.setBounds(type.get("bounds").toString());
                                hypernymsList.add(hypernymInst);
                                
                                // try to map the DBpedia instance type to a DBpedia Ontology type
                                String mappedType = TypeMapper.getInstance().getTypeMapping(lang, type.get("uri").toString());
                                
                                if(mappedType != null) {
                                    
                                    Hypernym hypernym2 = new Hypernym();
                                    hypernym2.setEntity(entityTitle);
                                    hypernym2.setEntityURL(notEncodedURIEn);
                                    hypernym2.setType( mappedType.split("/")[mappedType.split("/").length-1] );
                                    hypernym2.setTypeURL(mappedType);
                                    hypernym2.setOrigin(type.get("origin").toString());
                                    hypernym2.setAccuracy(type.get("accuracy").toString());
                                    hypernym2.setBounds(type.get("bounds").toString());
                                    hypernymsList.add(hypernym2);
                                    
                                    OntoRecord initRecord = new OntoRecord();
                                    initRecord.setUri(mappedType);
                                    
                                    while(initRecord != null){
                                        
                                        initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                                        
                                        if(initRecord == null) {
                                            return hypernymsList;
                                        } else {
                                            Hypernym hypernymDerived = new Hypernym();
                                            hypernymDerived.setEntity(entityTitle);
                                            hypernymDerived.setEntityURL(notEncodedURIEn);
                                            hypernymDerived.setType(initRecord.getLabel());
                                            hypernymDerived.setTypeURL(initRecord.getUri());
                                            hypernymDerived.setOrigin("thd-derived");
                                            hypernymDerived.setAccuracy(type.get("accuracy").toString());
                                            hypernymDerived.setBounds(type.get("bounds").toString());
                                            hypernymsList.add(hypernymDerived);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return hypernymsList;
                    
                case "de":
                    
                    String encodedURIDe = "http://de.dbpedia.org/resource/"+URLEncoder.encode(entityTitle.replace(" ", "_"), "UTF-8");
                    String notEncodedURIDe = "http://de.dbpedia.org/resource/"+entityTitle.replace(" ", "_");
                    queryObj.append("uri", encodedURIDe);
                    
                    resObj = MongoDBClient.getDBInstance().getCollection("de_entities_thd_lhd10").findOne(queryObj);
                    
                    if(resObj != null){
                        
//                    hypernymsList.addAll(YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_"), "de", "thd-derived"));
                        
                        BasicDBList typesList = (BasicDBList)resObj.get("types");
                        
                        for(int i = 0; i < typesList.size(); i++) {
                            
                            DBObject type = (DBObject)typesList.get(i);
                            
                            String mappingStr = type.get("mapping").toString();
                            // hypernym is mapped to dbpedia ontology
                            // creating hierarchy from the dbpedia ontology
                            if(mappingStr.equals("dbOnto")){
                                
                                Hypernym hypernym = new Hypernym();
                                hypernym.setEntity(entityTitle);
                                hypernym.setEntityURL(notEncodedURIDe);
                                hypernym.setType(type.get("label").toString());
                                hypernym.setTypeURL(type.get("uri").toString());
                                hypernym.setAccuracy(type.get("accuracy").toString());
                                hypernym.setBounds(type.get("bounds").toString());
                                hypernym.setOrigin(type.get("origin").toString());
                                hypernymsList.add(hypernym);
                                
                                OntoRecord initRecord = new OntoRecord();
                                initRecord.setUri(type.get("uri").toString());
                                
                                while(initRecord != null){

                                    initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                                    
                                    if(initRecord != null){
                                        
                                        Hypernym hypernymDerived = new Hypernym();
                                        hypernymDerived.setEntity(entityTitle);
                                        hypernymDerived.setEntityURL(notEncodedURIDe);
                                        hypernymDerived.setType(initRecord.getLabel());
                                        hypernymDerived.setTypeURL(initRecord.getUri());
                                        hypernymDerived.setOrigin("thd-derived");
                                        hypernymsList.add(hypernymDerived);
                                    }
                                }                            
                            }
                            // type is DBpedia instance, doesn't matter, add it to the types list
                            else {
                                Hypernym hypernymInst = new Hypernym();
                                hypernymInst.setEntity(entityTitle);
                                hypernymInst.setEntityURL(notEncodedURIDe);
                                hypernymInst.setType(type.get("label").toString());
                                hypernymInst.setTypeURL(type.get("uri").toString());
                                hypernymInst.setOrigin(type.get("origin").toString());
                                hypernymInst.setAccuracy(type.get("accuracy").toString());
                                hypernymInst.setBounds(type.get("bounds").toString());
                                hypernymsList.add(hypernymInst);
                                
                                // try to map the DBpedia instance type to a DBpedia Ontology type
                                String mappedType = TypeMapper.getInstance().getTypeMapping("de", type.get("uri").toString());
                                
                                if(mappedType != null) {
                                    Hypernym hypernym2 = new Hypernym();
                                    hypernym2.setEntity(entityTitle);
                                    hypernym2.setEntityURL(notEncodedURIDe);
                                    hypernym2.setType( mappedType.split("/")[mappedType.split("/").length-1] );
                                    hypernym2.setTypeURL(mappedType);
                                    hypernym2.setOrigin(type.get("origin").toString());
                                    hypernym2.setAccuracy(type.get("accuracy").toString());
                                    hypernym2.setBounds(type.get("bounds").toString());
                                    hypernymsList.add(hypernym2);
                                    
                                    OntoRecord initRecord = new OntoRecord();
                                    initRecord.setUri(mappedType);
                                    
                                    while(initRecord != null){
                                        
                                        initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                                        
                                        if(initRecord == null) {
                                            return hypernymsList;
                                        } else {
                                            Hypernym hypernymDerived = new Hypernym();
                                            hypernymDerived.setEntity(entityTitle);
                                            hypernymDerived.setEntityURL(notEncodedURIDe);
                                            hypernymDerived.setType(initRecord.getLabel());
                                            hypernymDerived.setTypeURL(initRecord.getUri());
                                            hypernymDerived.setOrigin("thd-derived");
                                            hypernymDerived.setAccuracy(type.get("accuracy").toString());
                                            hypernymDerived.setBounds(type.get("bounds").toString());
                                            hypernymsList.add(hypernymDerived);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return hypernymsList;
                    
                case "nl":
                    
                    String encodedURINl = "http://nl.dbpedia.org/resource/"+URLEncoder.encode(entityTitle.replace(" ", "_"), "UTF-8");
                    String notEncodedURINl = "http://nl.dbpedia.org/resource/"+entityTitle.replace(" ", "_");
                    queryObj.append("uri", encodedURINl);                    
                    
                    resObj = MongoDBClient.getDBInstance().getCollection("nl_entities_thd_lhd10").findOne(queryObj);
                    
                    if(resObj != null){
                        
//                        hypernymsList.addAll(YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_"), "en", "thd-derived"));
                        
                        BasicDBList typesList = (BasicDBList)resObj.get("types");
                        
                        for(int i = 0; i < typesList.size(); i++) {
                            
                            DBObject type = (DBObject)typesList.get(i);
                            
                            String typeURI = type.get("mapping").toString();
                            // hypernym is mapped to dbpedia ontology
                            // creating hierarchy from the dbpedia ontology
                            if(typeURI.equals("dbOnto")) {
                                
                                //System.out.println("dbOnto");
                                Hypernym hypernym = new Hypernym();
                                hypernym.setEntity(entityTitle);
                                hypernym.setEntityURL(notEncodedURINl);
                                hypernym.setType(type.get("label").toString());
                                hypernym.setTypeURL(type.get("uri").toString());
                                hypernym.setAccuracy(type.get("accuracy").toString());
                                hypernym.setBounds(type.get("bounds").toString());
                                hypernym.setOrigin(type.get("origin").toString());
                                hypernymsList.add(hypernym);
                                
                                OntoRecord initRecord = new OntoRecord();
                                initRecord.setUri(type.get("uri").toString());
                                
                                while(initRecord != null){

                                    initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                                    
                                    if(initRecord != null){
                                        
                                        Hypernym hypernymDerived = new Hypernym();
                                        hypernymDerived.setEntity(entityTitle);
                                        hypernymDerived.setEntityURL(notEncodedURINl);
                                        hypernymDerived.setType(initRecord.getLabel());
                                        hypernymDerived.setTypeURL(initRecord.getUri());
                                        hypernymDerived.setOrigin("thd-derived");
                                        hypernymsList.add(hypernymDerived);
                                    }
                                }
                            }
                            // type is DBpedia instance, doesn't matter, add it to the types list
                            else {
                                Hypernym hypernym = new Hypernym();
                                hypernym.setEntity(entityTitle);
                                hypernym.setEntityURL(notEncodedURINl);
                                hypernym.setType(type.get("label").toString());
                                hypernym.setTypeURL(type.get("uri").toString());
                                hypernym.setOrigin(type.get("origin").toString());
                                hypernym.setAccuracy(type.get("accuracy").toString());
                                hypernym.setBounds(type.get("bounds").toString());
                                hypernymsList.add(hypernym);
                                
                                // try to map the DBpedia instance type to a DBpedia Ontology type
                                String mappedType = TypeMapper.getInstance().getTypeMapping("nl", type.get("uri").toString());
                                
                                if(mappedType != null) {
                                    
                                    Hypernym hypernym2 = new Hypernym();
                                    hypernym2.setEntity(entityTitle);
                                    hypernym2.setEntityURL(notEncodedURINl);
                                    hypernym2.setType( mappedType.split("/")[mappedType.split("/").length-1] );
                                    hypernym2.setTypeURL(mappedType);
                                    hypernym2.setOrigin(type.get("origin").toString());
                                    hypernym2.setAccuracy(type.get("accuracy").toString());
                                    hypernym2.setBounds(type.get("bounds").toString());
                                    hypernymsList.add(hypernym2);
                                    
                                    OntoRecord initRecord = new OntoRecord();
                                    initRecord.setUri(mappedType);
                                    
                                    while(initRecord != null){
                                        
                                        initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                                        
                                        if(initRecord == null) {
                                            return hypernymsList;
                                        } else {
                                            Hypernym hypernymDerived = new Hypernym();
                                            hypernymDerived.setEntity(entityTitle);
                                            hypernymDerived.setEntityURL(notEncodedURINl);
                                            hypernymDerived.setType(initRecord.getLabel());
                                            hypernymDerived.setTypeURL(initRecord.getUri());
                                            hypernymDerived.setOrigin("thd-derived");
                                            hypernymDerived.setAccuracy(type.get("accuracy").toString());
                                            hypernymDerived.setBounds(type.get("bounds").toString());
                                            hypernymsList.add(hypernymDerived);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return hypernymsList;
            }
            return hypernymsList;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(THDWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hypernymsList;
    }    
    public ArrayList<Hypernym> getTHDHypernymsLHD(String entityTitle, String lang) throws UnknownHostException {

        ArrayList<Hypernym> hypernymsList = new ArrayList<Hypernym>();
        BasicDBObject queryObj = new BasicDBObject();
        //System.out.println("Searching hypernym for entity: " + entityTitle);
        queryObj.append("label", entityTitle);
        queryObj.append("types", new BasicDBObject().append("$elemMatch", new BasicDBObject().append("origin", "thd")));
        
        switch (lang) {
            case "en":
                
                DBObject resObj = MongoDBClient.getDBInstance().getCollection("en_entities_thd").findOne(queryObj);                
                if(resObj != null) {
                    
                    BasicDBList e = (BasicDBList)resObj.get("types");
                    DBObject type = (DBObject)e.get(0);
                    
                    String mappingStr = type.get("mapping").toString();
                    // hypernym is mapped to dbpedia ontology
                    // creating hierarchy from the dbpedia ontology
                    if(mappingStr.equals("dbOnto")) {
                        
                        Hypernym hypernym = new Hypernym();
                        hypernym.setEntity(entityTitle);
                        hypernym.setEntityURL(resObj.get("uri").toString());
                        hypernym.setType(type.get("label").toString());
                        hypernym.setTypeURL(type.get("uri").toString());
                        hypernym.setOrigin(type.get("origin").toString());
                        hypernym.setAccuracy(type.get("accuracy").toString());
                        hypernym.setBounds(type.get("bounds").toString());
                        hypernymsList.add(hypernym);

                        OntoRecord initRecord = new OntoRecord();
                        initRecord.setUri(type.get("uri").toString());
                        
                        while(initRecord != null){
                            
                            initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                            if(initRecord == null){
                                return hypernymsList;
                            } else {
                                Hypernym hypernymDerived = new Hypernym();
                                hypernymDerived.setEntity(entityTitle);
                                hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                hypernymDerived.setType(initRecord.getLabel());
                                hypernymDerived.setTypeURL(initRecord.getUri());
                                hypernymDerived.setOrigin("thd-derived");
                                hypernymDerived.setAccuracy(type.get("accuracy").toString());
                                hypernymDerived.setBounds(type.get("bounds").toString());
                                hypernymsList.add(hypernymDerived);
//                                System.out.println("hyp added, size: " + hypernymsList.size());
                            }
                        }
                    }
                    
                    // hypernym is not mapped to dbpedia ontology
                    // searching for superclass mapped to dbpedia ontology
                    // if found, for the superclass is created hierarchy
                    else if(mappingStr.equals("dbRes")){
                        
                        // try to find mapping
                        String mappedRes = DBpediaOntologyMapper.getInstance().mapEnResource(type.get("uri").toString());
                        
                        if(mappedRes != null) {
                            
                            Hypernym hypernym2 = new Hypernym();
                            hypernym2.setEntity(entityTitle);
                            hypernym2.setEntityURL(resObj.get("uri").toString());
                            hypernym2.setType(type.get("label").toString());
                            hypernym2.setTypeURL(mappedRes);
                            hypernym2.setOrigin(type.get("origin").toString());
                            hypernym2.setAccuracy(type.get("accuracy").toString());
                            hypernym2.setBounds(type.get("bounds").toString());
                            hypernymsList.add(hypernym2);
                            
                            // try to derive some more types
                            OntoRecord initRecord = new OntoRecord();
                            initRecord.setUri(mappedRes);

                            while(initRecord != null){

                                initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                                if(initRecord == null){
                                    return hypernymsList;
                                } else {
                                    Hypernym hypernymDerived = new Hypernym();
                                    hypernymDerived.setEntity(entityTitle);
                                    hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                    hypernymDerived.setType(initRecord.getLabel());
                                    hypernymDerived.setTypeURL(initRecord.getUri());
                                    hypernymDerived.setOrigin("thd-derived");
                                    hypernymDerived.setAccuracy(type.get("accuracy").toString());
                                    hypernymDerived.setBounds(type.get("bounds").toString());
                                    hypernymsList.add(hypernymDerived);
                                }
                            }
                        } else {
                            String initialUri = type.get("uri").toString();

                            boolean continueSearching = true;

                            while(continueSearching) {
                                // try to find dbonto for dbres
                                DBpediaMapping mapping = getSubclassConfirmed(initialUri);

                                // if not found, try to find dbres for the dbres
                                if(mapping == null) {

                                    initialUri = getSuperclass(initialUri, "en");

                                    if(initialUri == null){
                                        //System.out.println("No superclass found, finishing searching");
                                        continueSearching = false;

                                    } else {
                                        // superClass uri found
                                        // check if uri is dbOnto - create hierarchy
                                        // check if uri is dbRes - continue

                                        if(initialUri.contains("/resource/")){
                                            //System.out.println("Found superclass is dbres");
                                            // do nothing
                                            // continue to search
                                        } else if(initialUri.contains("/ontology/")) {
                                            //System.out.println("Found superclass is dbonto, finishing searching and creating hierarchy");
                                            // create hierarchy
                                            continueSearching = false;

                                            OntoRecord initRecord = new OntoRecord();
                                            initRecord.setUri(initialUri);

                                            while(initRecord != null){

                                                initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                                if(initRecord != null){

                                                    Hypernym hypernymDerived = new Hypernym();
                                                    hypernymDerived.setEntity(entityTitle);
                                                    hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                                    hypernymDerived.setType(initRecord.getLabel());
                                                    hypernymDerived.setTypeURL(initRecord.getUri());
                                                    hypernymDerived.setOrigin("thd-derived");
                                                    hypernymDerived.setAccuracy(type.get("accuracy").toString());
                                                    hypernymDerived.setBounds(type.get("bounds").toString());
                                                    hypernymsList.add(hypernymDerived);

                                                }
                                            }
                                        } else {
                                            // some other uri
                                            continueSearching = false;
                                        }
                                    }
                                }
                                // if found, then create hierarchy
                                else {
                                    continueSearching = false;
                                    // creating hierarchy
                                    OntoRecord initRecord = new OntoRecord();
                                    initRecord.setUri(initialUri);

                                    while(initRecord != null){

                                        initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                        if(initRecord != null){

                                            Hypernym hypernymDerived = new Hypernym();
                                            hypernymDerived.setEntity(entityTitle);
                                            hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                            hypernymDerived.setType(initRecord.getLabel());
                                            hypernymDerived.setTypeURL(initRecord.getUri());
                                            hypernymDerived.setOrigin("thd-derived");
                                            hypernymsList.add(hypernymDerived);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return hypernymsList;
                    
                } else {
//                    System.out.println("hypernym not found for " + entityTitle);
                    return hypernymsList;
                }
            case "de":
                
                resObj = MongoDBClient.getDBInstance().getCollection("de_entities_thd").findOne(queryObj);                
                
                if(resObj != null){
                    
                    hypernymsList.addAll(YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_"), "de", "thd-derived"));

                    BasicDBList typesList = (BasicDBList)resObj.get("types");
                    DBObject firstType = (DBObject)typesList.get(0);
                    
                    String mappingStr = firstType.get("mapping").toString();
                    // hypernym is mapped to dbpedia ontology
                    // creating hierarchy from the dbpedia ontology
                    if(mappingStr.equals("dbOnto")){
                        
                        Hypernym hypernym = new Hypernym();
                        hypernym.setEntity(entityTitle);
                        hypernym.setEntityURL(resObj.get("uri").toString());
                        hypernym.setType(firstType.get("label").toString());
                        hypernym.setTypeURL(firstType.get("uri").toString());
                        hypernym.setAccuracy(firstType.get("accuracy").toString());
                        hypernym.setBounds(firstType.get("bounds").toString());
                        hypernym.setOrigin(firstType.get("origin").toString());
                        hypernymsList.add(hypernym);

                        OntoRecord initRecord = new OntoRecord();
                        initRecord.setUri(firstType.get("uri").toString());
                        
                        while(initRecord != null){
                            
                            initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                            
                            if(initRecord != null){
                                
                                Hypernym hypernymDerived = new Hypernym();
                                hypernymDerived.setEntity(entityTitle);
                                hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                hypernymDerived.setType(initRecord.getLabel());
                                hypernymDerived.setTypeURL(initRecord.getUri());
                                hypernymDerived.setOrigin("thd-derived");
                                hypernymsList.add(hypernymDerived);                                
                            }
                        }
                    }
                    // hypernym is not mapped to dbpedia ontology
                    // searching for superclass mapped to dbpedia ontology
                    // if found, for the superclass is created hierarchy
                    else if(mappingStr.equals("dbRes")) {
                        
                        String mappedRes = DBpediaOntologyMapper.getInstance().mapDeResource(firstType.get("uri").toString());
                        
                        if(mappedRes != null) {
                            
                            Hypernym hypernym2 = new Hypernym();
                            hypernym2.setEntity(entityTitle);
                            hypernym2.setEntityURL(resObj.get("uri").toString());
                            hypernym2.setType(firstType.get("label").toString());
                            hypernym2.setTypeURL(mappedRes);
                            hypernym2.setOrigin(firstType.get("origin").toString());
                            hypernym2.setAccuracy(firstType.get("accuracy").toString());
                            hypernym2.setBounds(firstType.get("bounds").toString());
                            hypernymsList.add(hypernym2);
                            
                            OntoRecord initRecord = new OntoRecord();
                            initRecord.setUri(firstType.get("uri").toString());

                            while(initRecord != null){

                                initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                if(initRecord != null){

                                    Hypernym hypernymDerived = new Hypernym();
                                    hypernymDerived.setEntity(entityTitle);
                                    hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                    hypernymDerived.setType(initRecord.getLabel());
                                    hypernymDerived.setTypeURL(initRecord.getUri());
                                    hypernymDerived.setOrigin("thd-derived");
                                    hypernymsList.add(hypernymDerived);                                
                                }
                            }
                            // try to derive some more types
                        } else {
                            String initialUri = firstType.get("uri").toString();

                            boolean continueSearching = true;

                            while(continueSearching){
                                // try to find dbonto for dbres
                                DBpediaMapping mapping = getSubclassConfirmed(initialUri);

                                // if not found, try to find dbres for the dbres
                                if(mapping == null){

                                    initialUri = getSuperclass(initialUri, "de");

                                    if(initialUri == null){
                                        //System.out.println("No superclass found, finishing searching");
                                        continueSearching = false;

                                    } else {
                                        //System.out.println("Superclass found");
                                        // superClass uri found
                                        // check if uri is dbOnto - create hierarchy
                                        // check if uri is dbRes - continue

                                        if(initialUri.contains("/resource/")){
                                            //System.out.println("Found superclass is dbres");
                                            // do nothing
                                            // continue to search
                                        } else if(initialUri.contains("/ontology/")) {
                                            //System.out.println("Found superclass is dbonto, finishing searching and creating hierarchy");
                                            // create hierarchy
                                            continueSearching = false;

                                            OntoRecord initRecord = new OntoRecord();
                                            initRecord.setUri(initialUri);

                                            while(initRecord != null){

                                                initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                                if(initRecord != null){

                                                    Hypernym hypernymDerived = new Hypernym();
                                                    hypernymDerived.setEntity(entityTitle);
                                                    hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                                    hypernymDerived.setType(initRecord.getLabel());
                                                    hypernymDerived.setTypeURL(initRecord.getUri());
                                                    hypernymDerived.setOrigin("thd-derived");
                                                    hypernymsList.add(hypernymDerived);

                                                }
                                            }
                                        } else {
                                            // some other uri
                                            continueSearching = false;
                                        }
                                    }
                                }
                                // if found, then create hierarchy
                                else {
                                    continueSearching = false;
                                    // creating hierarchy
                                    OntoRecord initRecord = new OntoRecord();
                                    initRecord.setUri(initialUri);

                                    while(initRecord != null){

                                        initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                        if(initRecord != null){

                                            Hypernym hypernymDerived = new Hypernym();
                                            hypernymDerived.setEntity(entityTitle);
                                            hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                            hypernymDerived.setType(initRecord.getLabel());
                                            hypernymDerived.setTypeURL(initRecord.getUri());
                                            hypernymDerived.setOrigin("thd-derived");
                                            hypernymsList.add(hypernymDerived);

                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    return hypernymsList;
                    
                } else {
                    //System.out.println("hypernym not found for " + entityTitle);
                    return hypernymsList;
                }
                
            case "nl":
                
                resObj = MongoDBClient.getDBInstance().getCollection("nl_entities_thd").findOne(queryObj);                
                        //System.out.println(resObj);

                if(resObj != null){
                    
                    //System.out.println("before " + hypernymsList.size());
                    hypernymsList.addAll(YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_"), "en", "thd-derived"));
                    //System.out.println("after " + hypernymsList.size());

                    //System.out.println(resObj);
                    BasicDBList e = (BasicDBList)resObj.get("types");
                    DBObject type = (DBObject)e.get(0);
                    //System.out.println("HYPENYM: " + type.get("label"));
                    
                    BasicDBList typesList = (BasicDBList)resObj.get("types");
                    DBObject firstType = (DBObject)typesList.get(0);
                    
                    String typeURI = firstType.get("mapping").toString();
                    // hypernym is mapped to dbpedia ontology
                    // creating hierarchy from the dbpedia ontology
                    if(typeURI.equals("dbOnto")){
                        
                        Hypernym hypernym = new Hypernym();
                        hypernym.setEntity(entityTitle);
                        hypernym.setEntityURL(resObj.get("uri").toString());
                        hypernym.setType(type.get("label").toString());
                        hypernym.setTypeURL(type.get("uri").toString());
                        hypernym.setAccuracy(type.get("accuracy").toString());
                        hypernym.setBounds(type.get("bounds").toString());
                        hypernym.setOrigin(type.get("origin").toString());
                        hypernymsList.add(hypernym);
                    
                        OntoRecord initRecord = new OntoRecord();
                        initRecord.setUri(firstType.get("uri").toString());
                        
                        while(initRecord != null){
                            
                            initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);
                            
                            if(initRecord != null){
                                
                                Hypernym hypernymDerived = new Hypernym();
                                hypernymDerived.setEntity(entityTitle);
                                hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                hypernymDerived.setType(initRecord.getLabel());
                                hypernymDerived.setTypeURL(initRecord.getUri());
                                hypernymDerived.setOrigin("thd-derived");
                                hypernymsList.add(hypernymDerived);                                
                            }
                        }
                    }
                    // hypernym is not mapped to dbpedia ontology
                    // searching for superclass mapped to dbpedia ontology
                    // if found, for the superclass is created hierarchy
                    else if(typeURI.equals("dbRes")) {

                        String mappedRes = DBpediaOntologyMapper.getInstance().mapNlResource(type.get("uri").toString());
                        //System.out.println("dbRes");
                        if(mappedRes != null) {
                            Hypernym hypernym = new Hypernym();
                            hypernym.setEntity(entityTitle);
                            hypernym.setEntityURL(resObj.get("uri").toString());
                            hypernym.setType(type.get("label").toString());
                            hypernym.setTypeURL(mappedRes);
                            hypernym.setAccuracy(type.get("accuracy").toString());
                            hypernym.setBounds(type.get("bounds").toString());
                            hypernym.setOrigin(type.get("origin").toString());
                            hypernymsList.add(hypernym);

                            OntoRecord initRecord = new OntoRecord();
                            initRecord.setUri(mappedRes.toString());

                            while(initRecord != null){

                                initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                if(initRecord != null){

                                    Hypernym hypernymDerived = new Hypernym();
                                    hypernymDerived.setEntity(entityTitle);
                                    hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                    hypernymDerived.setType(initRecord.getLabel());
                                    hypernymDerived.setTypeURL(initRecord.getUri());
                                    hypernymDerived.setOrigin("thd-derived");
                                    hypernymsList.add(hypernymDerived);                                
                                }
                            }
                        } else {
                            
                            String initialUri = firstType.get("uri").toString();

                            boolean continueSearching = true;

                            while(continueSearching){
                                // try to find dbonto for dbres
                                DBpediaMapping mapping = getSubclassConfirmed(initialUri);

                                // if not found, try to find dbres for the dbres
                                if(mapping == null){

                                    initialUri = getSuperclass(initialUri, "nl");

                                    if(initialUri == null){
                                        //System.out.println("No superclass found, finishing searching");
                                        continueSearching = false;

                                    } else {

                                        //System.out.println("Superclass found");
                                        // superClass uri found
                                        // check if uri is dbOnto - create hierarchy
                                        // check if uri is dbRes - continue

                                        if(initialUri.contains("/resource/")){
                                            //System.out.println("Found superclass is dbres");
                                            // do nothing
                                            // continue to search
                                        } else if(initialUri.contains("/ontology/")) {
                                            //System.out.println("Found superclass is dbonto, finishing searching and creating hierarchy");
                                            // create hierarchy
                                            continueSearching = false;

                                            OntoRecord initRecord = new OntoRecord();
                                            initRecord.setUri(initialUri);

                                            while(initRecord != null){

                                                initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                                if(initRecord != null){

                                                    Hypernym hypernymDerived = new Hypernym();
                                                    hypernymDerived.setEntity(entityTitle);
                                                    hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                                    hypernymDerived.setType(initRecord.getLabel());
                                                    hypernymDerived.setTypeURL(initRecord.getUri());
                                                    hypernymDerived.setOrigin("thd-derived");
                                                    hypernymsList.add(hypernymDerived);

                                                }
                                            }
                                        } else {
                                            // some other uri
                                            continueSearching = false;
                                        }
                                    }
                                }
                                // if found, then create hierarchy
                                else {
                                    continueSearching = false;
                                    // creating hierarchy
                                    OntoRecord initRecord = new OntoRecord();
                                    initRecord.setUri(initialUri);

                                    while(initRecord != null){

                                        initRecord = DBpediaOntologyManager.getInstance().getSubclass(initRecord.getUri(), lang);

                                        if(initRecord != null){

                                            Hypernym hypernymDerived = new Hypernym();
                                            hypernymDerived.setEntity(entityTitle);
                                            hypernymDerived.setEntityURL(resObj.get("uri").toString());
                                            hypernymDerived.setType(initRecord.getLabel());
                                            hypernymDerived.setTypeURL(initRecord.getUri());
                                            hypernymDerived.setOrigin("thd-derived");
                                            hypernymsList.add(hypernymDerived);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    return hypernymsList;
                    
                } else {
                    //System.out.println("hypernym not found for " + entityTitle);
                    return hypernymsList;
                }
        }
        return null;
    }    
    public HashSet getDBpediaHypernyms(String entityTitle, String lang) throws UnknownHostException{
        
        HashSet hypernymsList = new HashSet();
        try {
            BasicDBObject queryObj = new BasicDBObject();
            //System.out.println("Searching hypernym for entity: " + entityTitle);
            queryObj.append("types", new BasicDBObject().append("$elemMatch", new BasicDBObject().append("origin", "dbpedia")));
            
            BasicDBList dbTypesList;
            switch (lang) {
                case "en":
                    String uriEn = "http://dbpedia.org/resource/"+URLEncoder.encode(entityTitle.replace(" ", "_"),"UTF-8");
                    queryObj.append("uri", uriEn);
                    DBCursor cursorEN = MongoDBClient.getDBInstance().getCollection("en_entities_dbpedia").find(queryObj);
                    
                    while( cursorEN.hasNext() ){
                        DBObject resObj = cursorEN.next();
                        if(resObj != null){
                            
                            dbTypesList = (BasicDBList)resObj.get("types");
                            
                            for(int i=0; i < dbTypesList.size(); i++){
                                DBObject obj = (DBObject)dbTypesList.get(i);
                                
                                Hypernym hypernym = new Hypernym();
                                hypernym.setEntity(entityTitle);
                                hypernym.setEntityURL(resObj.get("uri").toString());
                                hypernym.setType(obj.get("label").toString());
                                hypernym.setTypeURL(obj.get("uri").toString());
                                hypernym.setOrigin("dbpedia");
                                //System.out.println("TEST: " + obj.get("uri").toString());
                                if(obj.get("uri").toString().contains("http://dbpedia.org/ontology/")){
                                    hypernymsList.add(hypernym);
                                    //System.out.println("DBpedia ontology");
                                }
                            }
                        }
                    }
                    cursorEN.close();
                    
                    return hypernymsList;
                    
                case "de":
                    
                    String uriDe = "http://de.dbpedia.org/resource/"+URLEncoder.encode(entityTitle.replace(" ", "_"),"UTF-8");
                    queryObj.append("uri", uriDe);
                    DBCursor cursorDE = MongoDBClient.getDBInstance().getCollection("de_entities_dbpedia").find(queryObj);
                    
                    while(cursorDE.hasNext()) {
                        
                        DBObject resObj = cursorDE.next();
                        if(resObj != null){
                            dbTypesList = (BasicDBList)resObj.get("types");
                            
                            for(int i=0; i < dbTypesList.size(); i++){
                                
                                DBObject obj = (DBObject)dbTypesList.get(i);
                                
                                Hypernym hypernym = new Hypernym();
                                hypernym.setEntity(entityTitle);
                                hypernym.setEntityURL(resObj.get("uri").toString());
                                hypernym.setType(obj.get("label").toString());
                                hypernym.setTypeURL(obj.get("uri").toString());
                                hypernym.setOrigin("dbpedia");
                                //System.out.println("TEST: " + obj.get("uri").toString());
                                if(obj.get("uri").toString().contains("http://dbpedia.org/ontology/")){
                                    hypernymsList.add(hypernym);
                                    //System.out.println("DBpedia ontology");
                                }
                            }
                        }
                    }
                    cursorDE.close();
                    
                    return hypernymsList;
                    
                case "nl":
                    
                    String uriNl = "http://nl.dbpedia.org/resource/"+URLEncoder.encode(entityTitle.replace(" ", "_"),"UTF-8");
                    queryObj.append("uri", uriNl);
                    DBCursor cursorNL = MongoDBClient.getDBInstance().getCollection("nl_entities_dbpedia").find(queryObj);
                    
                    while(cursorNL.hasNext()){
                        DBObject resObj = cursorNL.next();
                        if(resObj != null){
                            dbTypesList = (BasicDBList)resObj.get("types");
                            
                            for(int i=0; i < dbTypesList.size(); i++){
                                
                                DBObject obj = (DBObject)dbTypesList.get(i);
                                
                                Hypernym hypernym = new Hypernym();
                                hypernym.setEntity(entityTitle);
                                hypernym.setEntityURL(resObj.get("uri").toString());
                                hypernym.setType(obj.get("label").toString());
                                hypernym.setTypeURL(obj.get("uri").toString());
                                hypernym.setOrigin("dbpedia");
                                //System.out.println("TEST: " + obj.get("uri").toString());
                                
                                if(obj.get("uri").toString().contains("http://dbpedia.org/ontology/")){
                                    hypernymsList.add(hypernym);
                                    //System.out.println("DBpedia ontology");
                                }
                            }
                        }
                    }
                    cursorNL.close();
                    
                    return hypernymsList;
                    
                default:
                    //System.out.println("Not supported language!");
                    return hypernymsList;
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(THDWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hypernymsList;
    }
    
    public HashSet getYAGOHypernyms(String entityTitle, String lang) throws UnknownHostException{
        
        HashSet hypernymsList = new HashSet();
        BasicDBObject queryObj = new BasicDBObject();
        
        switch(lang){

            case "en":

//                queryObj.append("labels.label", entityTitle).append("labels.lang", "en");
                queryObj.append("uri", "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_")).append("labels.lang", "en");
//                queryObj.append("uri", "http://dbpedia.org/resource/"+entityTitle.replaceAll(" ", "_")).append("labels.lang", "en");
                
                DBObject resObj = MongoDBClient.getDBInstance().getCollection("entities_yago").findOne(queryObj);
                
                if(resObj != null) {
                    
                    //System.out.println("before " + hypernymsList.size());
//                    hypernymsList.addAll(YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_"), "en", "yago"));
                    hypernymsList = YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_"), "en", "yago");
                    //System.out.println("after " + hypernymsList.size());
                }
                return hypernymsList;
//                break;
                
            case "de":
                
                queryObj = new BasicDBObject();
                queryObj.append("uri", "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_")).append("labels.lang", "de");
//                queryObj.append("labels.label", entityTitle).append("labels.lang", "de");
//                queryObj.append("uri", "http://yago-knowledge.org/resource/"+entityTitle.replaceAll(" ", "_"));
                
                resObj = MongoDBClient.getDBInstance().getCollection("entities_yago").findOne(queryObj);
                
                if(resObj != null){
                    
                    //System.out.println("before " + hypernymsList.size());
//                    hypernymsList.addAll(YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, resObj.get("uri").toString(), "de", "yago"));
                    hypernymsList = YagoOntologyManager.getInstance().getYagoHypernyms(entityTitle, resObj.get("uri").toString(), "de", "yago");
                    //System.out.println("after " + hypernymsList.size());
                }
                return hypernymsList;
//                break;
                
            case "nl":
                
                break;
        }
        return hypernymsList;
    }

    public DBpediaMapping getSubclassConfirmed(String res) throws UnknownHostException{
        
        BasicDBObject queryObj2 = new BasicDBObject();
        queryObj2.append("label", res);
        DBObject resObj2 = MongoDBClient.getDBInstance().getCollection("test_en_entities_superclass").findOne(queryObj2);
        
        if(resObj2 != null){
            //System.out.println(resObj);
            BasicDBList e = (BasicDBList)resObj2.get("types");
            DBObject type = (DBObject)e.get(0);
            //System.out.println("Superclass for "+ res + " is " + type.get("label"));
            
            DBpediaMapping result = new DBpediaMapping();
            result.setLabel(type.get("label").toString());
            result.setUri(type.get("uri").toString());
            
            return result;
        } else {
            return null;
        }        
    }
    
    public String getSuperclass(String uri, String lang) throws UnknownHostException{
        
        //System.out.println("Searching superclass for " + uri);
        BasicDBObject tempQueryObj = new BasicDBObject();
        tempQueryObj.append("uri", uri);
        tempQueryObj.append("types", new BasicDBObject().append("$elemMatch", new BasicDBObject().append("origin", "thd")));
        DBObject tempResObj = null;
        
        switch (lang) {
            case "en":
                tempResObj = MongoDBClient.getDBInstance().getCollection("test_en_entities").findOne(tempQueryObj);
            case "de":
                tempResObj = MongoDBClient.getDBInstance().getCollection("test_de_entities").findOne(tempQueryObj);           
        }
        
        if(tempResObj != null){
            BasicDBList typesList = (BasicDBList)tempResObj.get("types");
            DBObject tmpTypeObj = (DBObject) typesList.get(0);
            return tmpTypeObj.get("uri").toString();
        } else {
            return null;
        }
        
    }
    public ArrayList<Hypernym> removeDuplicates(List<Hypernym> l) {
//        System.out.println("STARTED: " + l.size());
        Set<Hypernym> s = new TreeSet<Hypernym>(new Comparator<Hypernym>() {

            @Override
            public int compare(Hypernym o1, Hypernym o2) {
//                System.out.println(o1.getEntity());
//                System.out.println(o2.getEntity());
//                System.out.println(o1.getEntityURL());
//                System.out.println(o2.getEntityURL());
//                System.out.println(o1.getType());
//                System.out.println(o2.getType());
//                System.out.println(o1.getTypeURL());
//                System.out.println(o2.getTypeURL());
                if(
//                        o1.getEntity().equals(o2.getEntity()) &&
                        o1.getEntityURL().equals(o2.getEntityURL()) &&
                        o1.getOrigin().equals(o2.getOrigin()) &&
//                        o1.getType().equals(o2.getType()) &&
                        o1.getTypeURL().equals(o2.getTypeURL())
                ) {
//                    System.out.println("0");
                    return 0;
                } else {
//                    System.out.println("-1");
                    return -1;
                }
            }
        });
        
        s.addAll(l);
        
//        System.out.println("END: " + s.size());
        return new ArrayList<Hypernym>(s);
   }
}